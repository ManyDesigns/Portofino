package com.manydesigns.portofino.model.io;

import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.annotations.Id;
import com.manydesigns.portofino.model.annotations.KeyMappings;
import com.manydesigns.portofino.model.issues.Issue;
import com.manydesigns.portofino.model.language.ModelLexer;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.PatternFileSelector;
import org.eclipse.emf.ecore.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ModelIO {

    private static final Logger logger = LoggerFactory.getLogger(ModelIO.class);

    private final FileObject modelDirectory;
    private final List<ToLink> toLinkQueue = new ArrayList<>();

    private static class ToLink {
        final ParseTree parseTree;
        final Domain domain;

        private ToLink(ParseTree parseTree, Domain domain) {
            this.parseTree = parseTree;
            this.domain = domain;
        }
    }

    public ModelIO(FileObject modelDirectory) {
        this.modelDirectory = modelDirectory;
    }

    public Domain load(String name, Model model) throws IOException {
        if (modelDirectory.exists() && modelDirectory.getType().equals(FileType.FOLDER)) {
            FileObject domainDir = modelDirectory.getChild(name);
            if (domainDir != null && domainDir.exists()) {
                return loadDomainDirectory(model, null, domainDir);
            } else {
                return null;
            }
        } else {
            throw new IOException("Not a directory: " + modelDirectory.getName().getPath());
        }
    }

    public synchronized Model load() throws IOException {
        logger.info("Loading model from directory: {}", getModelDirectory().getName().getPath());
        Model model = new Model();
        FileObject modelDir = getModelDirectory();
        if(!modelDirectory.exists()) {
            return model;
        } else if(!modelDirectory.getType().equals(FileType.FOLDER)) {
            throw new IOException("Not a directory: " + modelDirectory.getName().getPath());
        }
        try {
            for (FileObject domainDir : modelDir.getChildren()) {
                if (domainDir.isFolder()) {
                    loadDomainDirectory(model, null, domainDir);
                }
            }
            for (ToLink toLink : toLinkQueue) {
                new EntityModelLinkerVisitor(model, toLink.domain).visit(toLink.parseTree);
            }
            return model;
        } finally {
            toLinkQueue.clear();
        }
    }

    protected Domain loadDomainDirectory(Model model, Domain parent, FileObject domainDir) throws IOException {
        String domainName = domainDir.getName().getBaseName();
        Domain domain;
        if(parent != null) {
            domain = parent.getSubdomains().stream().filter(p -> p.getName().equals(domainName)).findFirst().orElseGet(() -> {
                Domain newDomain = new Domain();
                newDomain.setName(domainName);
                parent.getSubdomains().add(newDomain);
                return newDomain;
            });
        } else {
            domain = model.ensureDomain(domainName);
        }
        for (FileObject child : domainDir.getChildren()) {
            String baseName = child.getName().getBaseName();
            if(child.isFile() && !baseName.endsWith(".changelog.xml") && !baseName.endsWith(".properties")) {
                if(baseName.endsWith(".domain")) {
                    loadDomainFile(model, domain, child);
                } else if(baseName.endsWith(".entity")) {
                    loadEntity(model, domain, child);
                } else if(baseName.endsWith(".object")) {
                    loadObject(model, domain, child);
                } else {
                    if (child.getName().getBaseName().equalsIgnoreCase("database.xml")) {
                        logger.debug("Ignoring legacy database.xml file");
                    } else {
                        logger.warn("Unknown file ignored when loading model: " + child.getName().getPath());
                    }
                }
            } else if(child.isFolder()) {
                loadDomainDirectory(model, domain, child);
            }
        }
        return domain;
    }

    private void loadDomainFile(Model model, Domain domain, FileObject file) throws IOException {
        try (InputStream inputStream = file.getContent().getInputStream()) {
            String path = Domain.getQualifiedName(domain);
            ModelParser parser = getParser(model, domain, path, inputStream);
            ModelParser.StandaloneDomainContext parseTree = parser.standaloneDomain();
            if (parser.getNumberOfSyntaxErrors() == 0) {
                EObject candidate = new EntityModelBuilderVisitor().visit(parseTree);
                if(candidate instanceof Domain) {
                    Domain subd = (Domain) candidate;
                    if(subd.getName().equals(domain.getName())) {
                        domain.getEClassifiers().addAll(subd.getEClassifiers());
                        domain.getEAnnotations().addAll(subd.getEAnnotations());
                        domain.getESubpackages().addAll(subd.getESubpackages());
                        toLinkQueue.add(new ToLink(parseTree, domain));
                    } else {
                        String msg = "Invalid domain, expected " + domain.getName() + ", got " + subd.getName() + " in " + file.getName().getPath();
                        model.getIssues().add(new Issue(
                                Issue.Severity.ERROR, domain, msg, path, null, null));
                        logger.error(msg);
                    }
                } else {
                    String msg = "Not a domain: " + candidate + " in " + file.getName().getPath();
                    model.getIssues().add(new Issue(Issue.Severity.ERROR, domain, msg, path, null, null));
                    logger.error(msg);
                }
            }
        }
    }

    protected void loadEntity(Model model, Domain domain, FileObject file) throws IOException {
        String path = Domain.getQualifiedName(domain) + "." + file.getName().getBaseName();
        try(InputStream inputStream = file.getContent().getInputStream()) {
            ModelParser parser = getParser(model, domain, path, inputStream);
            ModelParser.StandaloneEntityContext parseTree = parser.standaloneEntity();
            if (parser.getNumberOfSyntaxErrors() == 0) {
                EObject candidate = new EntityModelBuilderVisitor().visit(parseTree);
                if(candidate instanceof EClass) {
                    domain.getEClassifiers().add((EClassifier) candidate);
                    toLinkQueue.add(new ToLink(parseTree, domain));
                } else {
                    String msg = "Not an entity: " + candidate + " in " + file.getName().getPath();
                    model.getIssues().add(new Issue(Issue.Severity.ERROR, domain, msg, path, null, null));
                    logger.error(msg);
                }
            }
        } catch (Exception e) {
            String msg = "Could not load resource: " + file.getName().getURI();
            model.getIssues().add(new Issue(
                    Issue.Severity.ERROR, domain, msg + " – " + e, path, null, null));
            logger.error(msg, e);
        }
    }

    protected void loadObject(Model model, Domain domain, FileObject file) throws IOException {
        String path = Domain.getQualifiedName(domain);
        try(InputStream inputStream = file.getContent().getInputStream()) {
            ModelParser parser = getParser(model, domain, path, inputStream);
            ModelParser.StandaloneObjectContext parseTree = parser.standaloneObject();
            if (parser.getNumberOfSyntaxErrors() == 0) {
                EObject object =
                        new ModelObjectBuilderVisitor(model, domain).visitStandaloneObject(parseTree);
                domain.putObject(parseTree.object().name.getText(), object);
            }
        } catch (Exception e) {
            String msg = "Could not load resource: " + file.getName().getURI();
            model.getIssues().add(new Issue(
                    Issue.Severity.ERROR, domain, msg + " – " + e, path, null, null));
            logger.error(msg, e);
        }
    }

    @NotNull
    private ModelParser getParser(
            Model model, EObject parentObject, String path, InputStream inputStream) throws IOException {
        ModelLexer lexer = new ModelLexer(CharStreams.fromStream(inputStream));
        ModelParser parser = new ModelParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new ModelIssueErrorListener(model, parentObject, path));
        return parser;
    }

    public void save(Model model) throws IOException {
        logger.info("Saving model into directory: {}", getModelDirectory().getName().getPath());
        if(!modelDirectory.exists()) {
            modelDirectory.createFolder();
        }
        if(!modelDirectory.getType().equals(FileType.FOLDER)) {
            throw new IOException("Not a directory: " + modelDirectory.getName().getPath());
        }
        for(Domain domain : model.getDomains()) {
            save(domain);
        }
        deleteUnusedDomainDirectories(modelDirectory, model.getDomains());
    }

    public void save(Domain domain) throws IOException {
        FileObject domainDir = getDomainDirectory(domain);
        domainDir.createFolder();
        FileObject domainDefFile = domainDir.resolveFile(domain.getName() + ".domain");
        if(domain.getEAnnotations().isEmpty()) {
            domainDefFile.delete();
        } else {
            try(OutputStreamWriter os = fileWriter(domainDefFile)) {
                Map<String, String> imports = writeImports(domain, os);
                Map<String, String> typeAliases = MapUtils.invertMap(imports);
                writeAnnotations(domain, os, typeAliases,"");
                os.write("domain " + domain.getName() + ";");
            }
        }
        for (EClassifier entity :
                domain.getEClassifiers().stream().filter(c -> c instanceof EClass).collect(Collectors.toList())) {
            saveEntity((EClass) entity, domainDir);
        }
        deleteUnusedEntityFiles(domainDir, domain.getEClassifiers());
        for (Map.Entry<String, EObject> entry : domain.getObjects()) {
            saveObject(entry.getKey(), entry.getValue(), domain, domainDir);
        }
        deleteUnusedObjectFiles(domainDir, domain.getObjects().keySet());
        for(Domain subdomain : domain.getSubdomains()) {
            save(subdomain);
        }
        deleteUnusedDomainDirectories(domainDir, domain.getSubdomains());
    }

    public void save(EClass entity) throws IOException {
        EObject pkg = entity.eContainer();
        if (!(pkg instanceof Domain)) {
            throw new UnsupportedOperationException(entity + " does not belong to a domain");
        }
        saveEntity(entity, getDomainDirectory((Domain) pkg));
    }

    public FileObject getDomainDirectory(Domain domain) throws FileSystemException {
        FileObject parent;
        if (domain.eContainer() instanceof Domain) {
            parent = getDomainDirectory((Domain) domain.eContainer());
        } else {
            parent = modelDirectory;
        }
        return parent.resolveFile(domain.getName());
    }

    @NotNull
    protected OutputStreamWriter fileWriter(FileObject file) throws FileSystemException {
        return new OutputStreamWriter(file.getContent().getOutputStream(), StandardCharsets.UTF_8);
    }

    /**
     * Delete the directories of the domains that are no longer present in the model
     *
     * @throws FileSystemException if the subdomain directories cannot be listed.
     */
    protected void deleteUnusedDomainDirectories(FileObject baseDir, List<Domain> domains) throws FileSystemException {
        Arrays.stream(baseDir.getChildren()).forEach(dir -> {
            String dirPath = dir.getName().getPath();
            try {
                if(dir.getType() == FileType.FOLDER) {
                    String dirName = dir.getName().getBaseName();
                    if (domains.stream().noneMatch(d -> d.getName().equals(dirName))) {
                        logger.info("Deleting unused domain directory {}", dirPath);
                        try {
                            dir.deleteAll();
                        } catch (FileSystemException e) {
                            logger.warn("Could not delete unused domain directory " + dirPath, e);
                        }
                    }
                }
            } catch (FileSystemException e) {
                logger.error("Unexpected filesystem error when trying to delete domain directory " + dirPath, e);
            }
        });
    }

    protected void saveEntity(EClass entity, FileObject domainDir) throws IOException {
        FileObject entityFile = domainDir.resolveFile(entity.getName() + ".entity");
        try(OutputStreamWriter os = fileWriter(entityFile)) {
            Map<String, String> imports = writeImports(entity, os);
            Map<String, String> typeAliases = MapUtils.invertMap(imports);
            writeAnnotations(entity, os, typeAliases, "");
            os.write("entity " + entity.getName() + " {" + System.lineSeparator());
            List<EStructuralFeature> id = entity.getEStructuralFeatures().stream()
                    .filter(a -> a instanceof EAttribute && a.getEAnnotation(Id.class.getName()) != null)
                    .collect(Collectors.toList());
            if(!id.isEmpty()) {
                os.write("\tid {" + System.lineSeparator());
                for (EStructuralFeature property : id) {
                    EAnnotation ann = property.getEAnnotation(Id.class.getName());
                    try {
                        property.getEAnnotations().remove(ann);
                        writeProperty((EAttribute) property, os, typeAliases, "\t\t");
                    } finally {
                        property.getEAnnotations().add(ann);
                    }
                }
                os.write("\t}" + System.lineSeparator());
            }
            for(EStructuralFeature property : entity.getEStructuralFeatures()) {
                if(property instanceof EAttribute) {
                    if(!id.contains(property)) {
                        writeProperty((EAttribute) property, os, typeAliases, "\t");
                    }
                } else if(property instanceof EReference && !property.isDerived()) {
                    writeReference((EReference) property, os, typeAliases, "\t");
                }
            }
            os.write("}");
        }
    }

    public void saveObject(Domain domain, String name) throws IOException {
        saveObject(name, domain.getObjects().get(name), domain, getDomainDirectory(domain));
    }

    protected void saveObject(String name, EObject object, Domain domain, FileObject domainDir)
            throws IOException {
        FileObject objectFile = domainDir.resolveFile(name + ".object");
        try(OutputStreamWriter os = fileWriter(objectFile)) {
            Map<String, String> imports = new HashMap<>();
            EClass type = object.eClass();
            collectImports(type, domain, imports);
            addImport(type, domain, imports);
            writeImports(imports, os);
            os.write("object " + name + " : ");
            writeObjectBody(object, os, "");
        }
    }

    private void collectImports(EClass type, Domain domain, Map<String, String> imports) {
        type.getEStructuralFeatures().forEach(feature -> {
            EClassifier fType = feature.getEType();
            if (addImport(fType, domain, imports) && fType instanceof EClass) {
                collectImports((EClass) fType, domain, imports);
            }
        });
    }

    protected void writeObjectBody(EObject object, Writer writer, String indent) throws IOException {
        writer.write(object.eClass().getName());
        writer.write(" {" + System.lineSeparator());
        for(EStructuralFeature property : object.eClass().getEAllStructuralFeatures()) {
            Object value = object.eGet(property);
            if (value != null) {
                writer.write(indent + "\t" + property.getName() + " = ");
                writePropertyValue(value, writer, indent + "\t");
                writer.write(System.lineSeparator());
            }
        }
        writer.write(indent + "}");
    }

    private Map<String, String> writeImports(Domain domain, Writer writer) throws IOException {
        Map<String, String> imports = collectImports(domain);
        writeImports(imports, writer);
        return imports;
    }

    private Map<String, String> writeImports(EClass entity, Writer writer) throws IOException {
        Map<String, String> imports = collectImports(entity);
        writeImports(imports, writer);
        return imports;
    }

    private void writeImports(Map<String, String> imports, Writer writer) throws IOException {
        List<Map.Entry<String, String>> sortedImports =
                imports.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toList());
        for (Map.Entry<String, String> e : sortedImports) {
            writer.write("import " + e.getValue() + ";" + System.lineSeparator());
        }
        if (!imports.isEmpty()) {
            writer.write(System.lineSeparator());
        }
    }

    private Map<String, String> collectImports(EModelElement element) {
        Map<String, String> imports = new HashMap<>();
        element.getEAnnotations().stream()
                .filter(ModelIO::isNotTransient)
                .forEach(ann -> addImport(ann.getSource(), imports));
        return imports;
    }

    private Map<String, String> collectImports(EClass entity) {
        Map<String, String> imports = collectImports((EModelElement) entity);
        entity.getESuperTypes().forEach(t -> addImport(t, entity.getEPackage(), imports));
        entity.getEStructuralFeatures().forEach(f -> imports.putAll(collectImports(f, entity.getEPackage())));
        String fullEntityName = entity.getName();
        if(entity.getEPackage() != null) {
            fullEntityName = entity.getEPackage().getName() + "." + fullEntityName;
        }
        imports.remove(entity.getName(), fullEntityName);
        return imports;
    }

    private Map<String, String> collectImports(EStructuralFeature feature, EPackage domain) {
        Map<String, String> imports = collectImports(feature);
        EClassifier type = feature.getEType();
        addImport(type, domain, imports);
        return imports;
    }

    private static boolean isNotTransient(EAnnotation a) {
        return !Id.class.getName().equals(a.getSource()) && !KeyMappings.class.getName().equals(a.getSource());
    }

    private boolean addImport(EClassifier type, EPackage domain, Map<String, String> imports) {
        if(type == null) {
            return false;
        }
        //TODO handle homonyms
        String fullTypeName = type.getName();
        EPackage ePackage = type.getEPackage();
        if(ePackage != null) {
            if(ePackage == EcorePackage.eINSTANCE || ePackage == domain) {
                return false;
            }
            fullTypeName = Domain.getQualifiedName(ePackage) + "." + fullTypeName;
        }
        return addImport(fullTypeName, imports);
    }

    private boolean addImport(String name, Map<String, String> imports) {
        String shortName = name.substring(name.lastIndexOf('.') + 1);
        if(!shortName.equals(name)) {
            // putIfAbsent returns null when the previous value was null1
            return imports.putIfAbsent(shortName, name) == null;
        } else {
            return false;
        }
    }

    /**
     * Deletes the files of the entities that are no longer present in the model
     *
     * @throws FileSystemException if the entity files cannot be listed.
     */
    protected void deleteUnusedEntityFiles(FileObject baseDir, List<EClassifier> entities) throws FileSystemException {
        Arrays.stream(baseDir.getChildren()).forEach(file -> {
            String filePath = file.getName().getPath();
            try {
                String fileName = file.getName().getBaseName();
                if(file.getType() == FileType.FILE && fileName.endsWith(".entity")) {
                    if (entities.stream().noneMatch(e -> (e.getName() + ".entity").equals(fileName))) {
                        logger.info("Deleting unused entity file {}", filePath);
                        try {
                            file.deleteAll();
                        } catch (FileSystemException e) {
                            logger.warn("Could not delete unused entity file " + filePath, e);
                        }
                    }
                }
            } catch (FileSystemException e) {
                logger.error("Unexpected filesystem error when trying to delete entity file " + filePath, e);
            }
        });
    }

    /**
     * Delete the files of the objects that are no longer present in the model
     *
     * @throws FileSystemException if the object files cannot be listed.
     */
    protected void deleteUnusedObjectFiles(FileObject baseDir, Collection<String> names) throws FileSystemException {
        Arrays.stream(baseDir.getChildren()).forEach(file -> {
            String filePath = file.getName().getPath();
            try {
                String fileName = file.getName().getBaseName();
                if(file.getType() == FileType.FILE && fileName.endsWith(".object")) {
                    if (names.stream().noneMatch(n -> (n + ".object").equals(fileName))) {
                        logger.info("Deleting unused object file {}", filePath);
                        try {
                            file.deleteAll();
                        } catch (FileSystemException e) {
                            logger.warn("Could not delete unused object file " + filePath, e);
                        }
                    }
                }
            } catch (FileSystemException e) {
                logger.error("Unexpected filesystem error when trying to delete object file " + filePath, e);
            }
        });
    }


    protected void writeProperty(
            EAttribute property, Writer writer, Map<String, String> typeAliases, String indent) throws IOException {
        writeAnnotations(property, writer, typeAliases, indent);
        writer.write(indent + property.getName());
        EClassifier type = property.getEType();
        if(type != null) {
            String name = type.getName();
            String alias = ModelObjectBaseVisitor.getDefaultTypeAliases().inverse().get(name);
            writer.write(": " + (alias != null ? alias : name));
        }
        writer.write(System.lineSeparator());
    }

    protected void writeReference(
            EReference reference, Writer writer, Map<String, String> typeAliases, String indent) throws IOException {
        EAnnotation mappings = reference.getEAnnotation(KeyMappings.class.getName());
        try {
            if(mappings != null) {
                reference.getEAnnotations().remove(mappings);
            }
            writeAnnotations(reference, writer, typeAliases, indent);
        } finally {
            if(mappings != null) {
                reference.getEAnnotations().add(mappings);
            }
        }
        writer.write(indent + reference.getName());
        writer.write(" --> ");
        writer.write(reference.getEType().getName());
        if(mappings != null) {
            writer.write("(");
            boolean first = true;
            for(Map.Entry<String, String> e : mappings.getDetails()) {
                if(first) {
                    first = false;
                } else {
                    writer.write(", ");
                }
                writer.write(e.getValue());
                writer.write("=");
                writer.write(e.getKey());
            }
            writer.write(")");
        }
        if(reference.getUpperBound() > 1) {
            writer.write(" " + reference.getLowerBound() + ".." + reference.getUpperBound());
        } else if(reference.getLowerBound() > 0) {
            writer.write(" " + reference.getLowerBound() + "..*");
        }
        writer.write(System.lineSeparator());
    }

    public void writeAnnotations(
            EModelElement annotated, Writer writer, Map<String, String> typeAliases, String indent) throws IOException {
        for(EAnnotation annotation : annotated.getEAnnotations()) {
            if (isNotTransient(annotation)) {
                writeAnnotation(annotation, writer, typeAliases, indent);
            }
        }
    }

    public void writeAnnotation(
            EAnnotation annotation, Writer writer, Map<String, String> typeAliases, String indent) throws IOException {
        writer.write(indent + "@" + typeAliases.getOrDefault(annotation.getSource(), annotation.getSource()));
        if(!annotation.getDetails().isEmpty()) {
            writer.write("(");
            if(annotation.getDetails().size() == 1 && annotation.getDetails().containsKey("value")) {
                writeAnnotationPropertyValue(annotation, "value", writer);
            } else {
                boolean first = true;
                for(Map.Entry<String, String> property : new LinkedHashSet<>(annotation.getDetails().entrySet())) {
                    if(first) {
                        first = false;
                    } else {
                        writer.write(", ");
                    }
                    writer.write(property.getKey() + " = ");
                    writeAnnotationPropertyValue(annotation, property.getKey(), writer);
                }
            }
            writer.write(")");
        }
        writer.write(System.lineSeparator());
    }

    public void writeAnnotationPropertyValue(EAnnotation annotation, String name, Writer writer) throws IOException {
        String value = annotation.getDetails().get(name);
        try {
            Annotation ann = new Annotation(annotation);
            ann.init(null, null);
            Class<?> type = ann.getJavaAnnotationClass().getMethod(name).getReturnType();
            if(type.isArray()) {
                Class<?> componentType = type.getComponentType();
                for (String v : value.split(",")) {
                    writePropertyValue(v.trim(), componentType, writer);
                }
            } else {
                writePropertyValue(value, type, writer);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Invalid annotation " + annotation, e); //TODO
        }
    }

    protected void writePropertyValue(String value, Class<?> type, Writer writer) throws IOException {
        if(type == String.class || type == Class.class || Enum.class.isAssignableFrom(type)) {
            value = "\"" + value.replace("\"", "\\\"") + "\"";
        }
        writer.write(value);
    }

    protected void writePropertyValue(Object value, Writer writer, String indent) throws IOException {
        if (value instanceof String) {
            writer.write("\"" + value.toString().replace("\"", "\\\"") + "\"");
        } else if (value instanceof EEnumLiteral) {
            writer.write(((EEnumLiteral) value).getName());
        } else if (value instanceof EObject) {
            writeObjectBody((EObject) value, writer, indent);
        } else if (value instanceof Iterable) {
            writer.write("[");
            boolean first = true;
            for (Object elem : (Iterable) value) {
                if (!first) {
                    writer.write(",");
                }
                writer.write(System.lineSeparator());
                writer.write(indent + "\t");
                writePropertyValue(elem, writer, indent + "\t");
                first = false;
            }
            if(!first) {
                writer.write(System.lineSeparator());
                writer.write(indent);
            }
            writer.write("]");
        } else {
            writer.write(value.toString());
        }
    }

    public FileObject getModelDirectory() {
        return modelDirectory;
    }

    public void delete() throws IOException {
        getModelDirectory().delete(new PatternFileSelector(".*[.](domain|entity)"));
    }
}
