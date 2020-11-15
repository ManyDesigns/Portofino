package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.*;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.model.io.ModelIO;
import com.manydesigns.portofino.model.java.JavaTypesDomain;
import com.manydesigns.portofino.model.language.ModelLexer;
import com.manydesigns.portofino.model.language.ModelParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class DefaultModelIO implements ModelIO {

    private static final Logger logger = LoggerFactory.getLogger(DefaultModelIO.class);

    private final FileObject modelDirectory;

    public DefaultModelIO(FileObject modelDirectory) {
        this.modelDirectory = modelDirectory;
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
        loadDatabasePersistence(modelDir, model);
        return model;
    }

    protected void loadEntities(FileObject directory, Model model) throws IOException {
        for(FileObject domainDir : directory.getChildren()) {
            if(domainDir.isFolder()) {
                loadDomain(model, null, domainDir);
            }
        }
    }

    protected void loadDomain(Model model, Domain parent, FileObject domainDir) throws IOException {
        String domainName = domainDir.getName().getBaseName();
        FileObject domainDefFile = domainDir.resolveFile(domainName + ".domain");
        if(domainDefFile.exists()) {
            loadDomainDefinition(model, parent, domainDefFile);
        }
        Domain domain;
        if(parent != null) {
            domain = parent.ensureSubdomain(domainName);
        } else {
            domain = model.ensureDomain(domainName);
        }
        for (FileObject child : domainDir.getChildren()) {
            if(child.isFile() && child.getName().getBaseName().endsWith(".entity")) {
                loadEntity(model, domain, child);
            } else if(child.isFolder()) {
                loadDomain(model, domain, child);
            }
        }
    }

    protected void loadEntity(Model model, Domain domain, FileObject entityFile) throws IOException {
        try(InputStream inputStream = entityFile.getContent().getInputStream()) {
            ModelLexer lexer = new ModelLexer(CharStreams.fromStream(inputStream));
            ModelParser parser = new ModelParser(new CommonTokenStream(lexer));
            ModelParser.StandaloneEntityContext parseTree = parser.standaloneEntity();
            if (parser.getNumberOfSyntaxErrors() == 0) {
                new EntityModelVisitor(model, domain).visit(parseTree);
            } else {
                logger.error("Could not parse entity definition " + entityFile.getName().getPath()); //TODO properly report errors
            }
        }
    }

    protected void loadDomainDefinition(Model model, Domain parent, FileObject domainDefFile) throws IOException {
        try(InputStream inputStream = domainDefFile.getContent().getInputStream()) {
            ModelLexer lexer = new ModelLexer(CharStreams.fromStream(inputStream));
            ModelParser parser = new ModelParser(new CommonTokenStream(lexer));
            ModelParser.StandaloneDomainContext parseTree = parser.standaloneDomain();
            if(parser.getNumberOfSyntaxErrors() == 0) {
                new EntityModelVisitor(model, parent).visit(parseTree);
            } else {
                logger.error("Could not parse domain definition file " + domainDefFile.getName().getPath()); //TODO properly report errors
            }
        }
    }

    protected void loadDatabasePersistence(FileObject modelDirectory, Model model) throws IOException {
        FileObject persistenceFile = modelDirectory.resolveFile("persistence.database");
        if(persistenceFile.exists()) {
            logger.info("Loading database connections from " + persistenceFile.getName().getPath());
            try(InputStream inputStream = persistenceFile.getContent().getInputStream()) {
                ModelLexer lexer = new ModelLexer(CharStreams.fromStream(inputStream));
                ModelParser parser = new ModelParser(new CommonTokenStream(lexer));
                ModelParser.DatabasePersistenceContext parseTree = parser.databasePersistence();
                if(parser.getNumberOfSyntaxErrors() == 0) {
                    new DatabasePersistenceVisitor(model).visit(parseTree);
                } else {
                    logger.error("Could not parse database connections file"); //TODO properly report errors
                }
            }
        } else {
            logger.info("No database persistence defined in " + modelDirectory.getName().getPath());
        }
    }

    @Override
    public void save(Model model, FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile) throws IOException, ConfigurationException {
        logger.info("Saving model into directory: {}", getModelDirectory().getName().getPath());
        FileObject modelDir = getModelDirectory();
        if(!modelDirectory.exists()) {
            modelDirectory.createFolder();
        }
        if(!modelDirectory.getType().equals(FileType.FOLDER)) {
            throw new IOException("Not a directory: " + modelDirectory.getName().getPath());
        }
        saveEntities(modelDir, model);
        saveDatabasePersistence(modelDir, model);
        if (configurationFile != null) {
            configurationFile.save();
            logger.info("Saved configuration file {}", configurationFile.getFileHandler().getFile().getAbsolutePath());
        }
    }

    protected void saveDatabasePersistence(FileObject modelDir, Model model) throws IOException {
        FileObject persistenceFile = modelDir.resolveFile("persistence.database");
        persistenceFile.createFile();
        if(model.getDatabases().isEmpty()) {
            persistenceFile.delete();
        }
        try(OutputStreamWriter os = fileWriter(persistenceFile)) {
            for(Database db : model.getDatabases()) {
                os.write("database " + db.getName() + " (");
                ConnectionProvider cp = db.getConnectionProvider();
                if(cp instanceof JdbcConnectionProvider) {
                    os.write("type = \"jdbc\"");
                } else if(cp instanceof JndiConnectionProvider) {
                    os.write("type = \"jndi\"");
                } else {
                    throw new IllegalStateException("Unknown connection provider type: " + cp.getClass()); //TODO should check earlier to avoid leaving a broken file
                }
                os.write(") {" + System.lineSeparator());
                for(Schema schema : db.getSchemas()) {
                    os.write("\tschema " + schema.getSchemaName());
                    if(!schema.getActualSchemaName().equals(schema.getSchemaName())) {
                        os.write(" (" + schema.getActualSchemaName() + ")");
                    }
                    os.write(System.lineSeparator());
                }
                os.write("}");
            }
        }
    }

    @NotNull
    protected OutputStreamWriter fileWriter(FileObject file) throws FileSystemException {
        return new OutputStreamWriter(file.getContent().getOutputStream(), StandardCharsets.UTF_8);
    }

    protected void saveEntities(FileObject modelDir, Model model) throws IOException {
        for(Domain domain : model.getDomains()) {
            saveDomain(domain, modelDir);
        }
    }

    protected void saveDomain(Domain domain, FileObject directory) throws IOException {
        FileObject domainDir = directory.resolveFile(domain.getName());
        domainDir.createFolder();
        FileObject domainDefFile = domainDir.resolveFile(domain.getName() + ".domain");
        domainDefFile.delete(); //TODO
        for(Entity entity : domain.getEntities()) {
            saveEntity(entity, domainDir);
        }
        //TODO imports
        for(Domain subdomain : domain.getSubdomains()) {
            saveDomain(subdomain, domainDir);
        }
    }

    protected void saveEntity(Entity entity, FileObject domainDir) throws IOException {
        FileObject entityFile = domainDir.resolveFile(entity.getName() + ".entity");
        try(OutputStreamWriter os = fileWriter(entityFile)) {
            for(Annotation annotation : entity.getAnnotations()) {
                writeAnnotation(annotation, os);
            }
            os.write("entity " + entity.getName() + " {" + System.lineSeparator());
            for(Property property : entity.getProperties()) {
                os.write("\t" + property.getName());
                if(property.getType() != entity.getDomain().getDefaultType()) {
                    os.write(": " + property.getType().getName());
                }
                os.write(System.lineSeparator());
            }
            os.write("}");
        }
    }

    protected void writeAnnotation(Annotation annotation, OutputStreamWriter os) throws IOException {
        os.write("@" + annotation.getType());
        if(!annotation.getProperties().isEmpty()) {
            os.write("(");
            if(annotation.getProperties().size() == 1 && annotation.getProperties().get(0).getName().equals("value")) {
                writeAnnotationPropertyValue(annotation, annotation.getProperty("value"), os);
            } else {
                boolean first = true;
                for(AnnotationProperty property : annotation.getProperties()) {
                    if(first) {
                        first = false;
                    } else {
                        os.write(", ");
                    }
                    os.write(property.getName() + " = ");
                    writeAnnotationPropertyValue(annotation, property, os);
                }
            }
            os.write(")");
        }
        os.write(System.lineSeparator());
    }

    protected void writeAnnotationPropertyValue(Annotation annotation, AnnotationProperty property, OutputStreamWriter os) throws IOException {
        String value = property.getValue();
        try {
            Class<?> type = annotation.getJavaAnnotationClass().getMethod(property.getName()).getReturnType();
            if(type == String.class || type == Class.class || Enum.class.isAssignableFrom(type)) {
                value = "\"" + value + "\""; //TODO escape
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Invalid annotation " + annotation, e); //TODO
        }
        os.write(value);
    }

    @Override
    public FileObject getModelDirectory() throws FileSystemException {
        return modelDirectory;
    }

    @Override
    public void delete() throws IOException {
        //TODO
    }
}
