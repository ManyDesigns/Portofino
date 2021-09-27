package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.*;
import com.manydesigns.portofino.model.database.annotations.KeyMappings;
import com.manydesigns.portofino.model.database.annotations.Id;
import com.manydesigns.portofino.model.io.ModelIO;
import com.manydesigns.portofino.model.language.ModelLexer;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.PatternFileSelector;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultModelIO implements ModelIO {

    private static final Logger logger = LoggerFactory.getLogger(DefaultModelIO.class);

    private final FileObject modelDirectory;
    protected final ResourceSet resourceSet = new ResourceSetImpl();

    public DefaultModelIO(FileObject modelDirectory) {
        this.modelDirectory = modelDirectory;
        resourceSet.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put("json", new JsonResourceFactory());
        resourceSet.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put("xmi", new XMIResourceFactoryImpl());
        resourceSet.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put("ecore", new EcoreResourceFactoryImpl());
        resourceSet.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put("entity", new EntityResource.Factory());
        resourceSet.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put("domain", new DomainResource.Factory());
        resourceSet.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put("*", new XMLResourceFactoryImpl());
    }

    @Override
    public Model load() throws IOException {
        logger.info("Loading model from directory: {}", getModelDirectory().getName().getPath());
        Model model = new Model();
        FileObject modelDir = getModelDirectory();
        if(!modelDirectory.exists() || !modelDirectory.getType().equals(FileType.FOLDER)) {
            throw new IOException("Not a directory: " + modelDirectory.getName().getPath());
        }
        loadEntities(modelDir, model);
        return model;
    }

    protected void loadEntities(FileObject directory, Model model) throws IOException {
        for(FileObject domainDir : directory.getChildren()) {
            if(domainDir.isFolder()) {
                loadDomain(model, null, domainDir);
            }
        }
    }

    protected void loadDomain(Model model, EPackage parent, FileObject domainDir) throws IOException {
        String domainName = domainDir.getName().getBaseName();
        EPackage domain;
        if(parent != null) {
            domain = parent.getESubpackages().stream().filter(p -> p.getName().equals(domainName)).findFirst().orElseGet(() -> {
                EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
                ePackage.setName(domainName);
                parent.getESubpackages().add(ePackage);
                return ePackage;
            });
        } else {
            domain = model.ensureDomain(domainName);
        }
        for (FileObject child : domainDir.getChildren()) {
            String baseName = child.getName().getBaseName();
            if(child.isFile() && !baseName.endsWith(".changelog.xml") && !baseName.endsWith(".properties")) {
                loadResource(domain, child);
            } else if(child.isFolder()) {
                loadDomain(model, domain, child);
            }
        }
    }

    protected void loadResource(EPackage domain, FileObject entityFile) throws IOException {
        try(InputStream inputStream = entityFile.getContent().getInputStream()) {
            Resource resource = resourceSet.createResource(URI.createURI(entityFile.getName().getURI()));
            resource.load(inputStream, null);
            EList<EObject> contents = resource.getContents();
            contents.forEach(o -> {
                if(o instanceof EClass) {
                    domain.getEClassifiers().add((EClassifier) o);
                } else if(o instanceof EPackage) {
                    EPackage pkg = (EPackage) o;
                    if(pkg.getName().equals(domain.getName())) {
                        domain.getEClassifiers().addAll(pkg.getEClassifiers());
                        domain.getEAnnotations().addAll(pkg.getEAnnotations());
                    } else {
                        logger.error("Invalid domain, expected " + domain.getName() + ", got " + pkg.getName());
                    }
                }
            });
        } catch (IOException e) {
            logger.error("Could not load resource: " + entityFile.getName().getURI(), e);
        }
    }

    protected void loadDomainDefinition(Model model, EPackage parent, FileObject domainDefFile) throws IOException {
        try(InputStream inputStream = domainDefFile.getContent().getInputStream()) {
            ModelLexer lexer = new ModelLexer(CharStreams.fromStream(inputStream));
            ModelParser parser = new ModelParser(new CommonTokenStream(lexer));
            ModelParser.StandaloneDomainContext parseTree = parser.standaloneDomain();
            if(parser.getNumberOfSyntaxErrors() == 0) {
                new EntityModelVisitor(parent).visit(parseTree);
            } else {
                logger.error("Could not parse domain definition file " + domainDefFile.getName().getPath()); //TODO properly report errors
            }
        }
    }

    @Override
    public void save(Model model) throws IOException {
        logger.info("Saving model into directory: {}", getModelDirectory().getName().getPath());
        if(!modelDirectory.exists()) {
            modelDirectory.createFolder();
        }
        if(!modelDirectory.getType().equals(FileType.FOLDER)) {
            throw new IOException("Not a directory: " + modelDirectory.getName().getPath());
        }
        for(EPackage domain : model.getDomains()) {
            saveDomain(domain, modelDirectory);
        }
        deleteUnusedDomainDirectories(modelDirectory, model.getDomains());
    }

    @NotNull
    protected OutputStreamWriter fileWriter(FileObject file) throws FileSystemException {
        return new OutputStreamWriter(file.getContent().getOutputStream(), StandardCharsets.UTF_8);
    }

    protected void saveDomain(EPackage domain, FileObject directory) throws IOException {
        FileObject domainDir = directory.resolveFile(domain.getName());
        domainDir.createFolder();
        FileObject domainDefFile = domainDir.resolveFile(domain.getName() + ".domain");
        if(domain.getEAnnotations().isEmpty()) {
            domainDefFile.delete();
        } else {
            try(OutputStreamWriter os = fileWriter(domainDefFile)) {
                writeAnnotations(domain, os, "");
                os.write("domain " + domain.getName() + ";");
            }
        }
        for(EClassifier entity : domain.getEClassifiers().stream().filter(c -> c instanceof EClass).collect(Collectors.toList())) {
            saveEntity((EClass) entity, domainDir);
        }
        deleteUnusedEntityFiles(domainDir, domain.getEClassifiers());
        //TODO imports
        for(EPackage subdomain : domain.getESubpackages()) {
            saveDomain(subdomain, domainDir);
        }
        deleteUnusedDomainDirectories(domainDir, domain.getESubpackages());
    }

    /**
     * Delete the directories of the domains that are no longer present in the model
     *
     * @throws FileSystemException if the subdomain directories cannot be listed.
     */
    protected void deleteUnusedDomainDirectories(FileObject baseDir, List<EPackage> domains) throws FileSystemException {
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
            writeAnnotations(entity, os, "");
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
                        writeProperty((EAttribute) property, os, "\t\t");
                    } finally {
                        property.getEAnnotations().add(ann);
                    }
                }
                os.write("\t}" + System.lineSeparator());
            }
            for(EStructuralFeature property : entity.getEStructuralFeatures()) {
                if(property instanceof EAttribute) {
                    if(!id.contains(property)) {
                        writeProperty((EAttribute) property, os, "\t");
                    }
                } else if(property instanceof EReference && !property.isDerived()) {
                    writeReference((EReference) property, os, "\t");
                }
            }
            os.write("}");
        }
    }

    /**
     * Delete the directories of the domains that are no longer present in the model
     *
     * @throws FileSystemException if the subdomain directories cannot be listed.
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

    protected void writeProperty(EAttribute property, Writer writer, String indent) throws IOException {
        writeAnnotations(property, writer, indent);
        writer.write(indent + property.getName());
        EClassifier type = property.getEType();
        if(!type.equals(EcorePackage.eINSTANCE.getEString())) {
            String name = type.getName();
            String alias = EntityModelVisitor.getDefaultTypeAliases().inverse().get(name);
            writer.write(": " + (alias != null ? alias : name));
        }
        writer.write(System.lineSeparator());
    }

    protected void writeReference(EReference reference, Writer writer, String indent) throws IOException {
        EAnnotation mappings = reference.getEAnnotation(KeyMappings.class.getName());
        try {
            if(mappings != null) {
                reference.getEAnnotations().remove(mappings);
            }
            writeAnnotations(reference, writer, indent);
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

    protected void writeAnnotations(EModelElement annotated, Writer writer, String indent) throws IOException {
        for(EAnnotation annotation : annotated.getEAnnotations()) {
            writeAnnotation(annotation, writer, indent);
        }
    }

    protected void writeAnnotation(EAnnotation annotation, Writer writer, String indent) throws IOException {
        writer.write(indent + "@" + annotation.getSource());
        if(!annotation.getDetails().isEmpty()) {
            writer.write("(");
            if(annotation.getDetails().size() == 1 && annotation.getDetails().containsKey("value")) {
                writeAnnotationPropertyValue(annotation, "value", writer);
            } else {
                boolean first = true;
                for(Map.Entry<String, String> property : annotation.getDetails().entrySet()) {
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

    protected void writeAnnotationPropertyValue(EAnnotation annotation, String name, Writer writer) throws IOException {
        String value = annotation.getDetails().get(name);
        try {
            Annotation ann = new Annotation(annotation);
            ann.init(null, null);
            Class<?> type = ann.getJavaAnnotationClass().getMethod(name).getReturnType();
            if(type == String.class || type == Class.class || Enum.class.isAssignableFrom(type)) {
                value = "\"" + StringEscapeUtils.escapeJava(value) + "\"";
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Invalid annotation " + annotation, e); //TODO
        }
        writer.write(value);
    }

    @Override
    public FileObject getModelDirectory() {
        return modelDirectory;
    }

    @Override
    public void delete() throws IOException {
        getModelDirectory().delete(new PatternFileSelector(".*[.](domain|entity)"));
    }
}
