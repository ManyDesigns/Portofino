package com.manydesigns.portofino.model.io.xml;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.model.io.ModelIO;
import com.manydesigns.portofino.model.io.ModelParseException;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.PatternFileSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Properties;

public class XMLModel implements ModelIO {

    public static final String LEGACY_APP_MODEL_FILE = "portofino-model.xml";
    private static final Logger logger = LoggerFactory.getLogger(XMLModel.class);

    private final FileObject modelDirectory;

    public XMLModel(FileObject modelDirectory) {
        this.modelDirectory = modelDirectory;
    }

    @Override
    public Model load() throws IOException {
        try {
            Model model;
            JAXBContext jc = createModelJAXBContext();
            Unmarshaller um = jc.createUnmarshaller();
            FileObject appModelFile = getLegacyModelFile();
            if (appModelFile.exists()) {
                logger.info("Loading legacy xml model from file: {}", appModelFile.getName().getPath());
                try (InputStream inputStream = appModelFile.getContent().getInputStream()) {
                    model = (Model) um.unmarshal(inputStream);
                } catch (Exception e) {
                    throw new ModelParseException(e);
                }
            } else {
                logger.info("Loading model from directory: {}", getModelDirectory().getName().getPath());
                model = new Model();
            }
            FileObject modelDir = getModelDirectory();
            if (modelDir.exists()) {
                for (FileObject databaseDir : modelDir.getChildren()) {
                    loadXmlDatabase(um, model, databaseDir);
                }
            }
            return model;
        } catch (JAXBException e) {
            throw new IOException("Error unmarshalling model", e);
        }
    }

    public JAXBContext createModelJAXBContext() throws JAXBException {
        return JAXBContext.newInstance(Model.class, View.class);
    }

    protected void loadXmlDatabase(Unmarshaller um, Model model, FileObject databaseDir) throws IOException, JAXBException {
        if(!databaseDir.getType().equals(FileType.FOLDER)) {
            logger.error("Not a directory: " + databaseDir.getName().getPath());
            return;
        }
        String databaseName = databaseDir.getName().getBaseName();
        FileObject databaseFile = databaseDir.resolveFile("database.xml");

        Database database;
        if(databaseFile.exists()) {
            logger.info("Loading database connection from " + databaseFile.getName().getPath());
            try(InputStream inputStream = databaseFile.getContent().getInputStream()) {
                database = (Database) um.unmarshal(inputStream);
                database.setParent(model);
                if(!databaseName.equals(database.getDatabaseName())) {
                    logger.error("Database named {} defined in directory named {}, skipping", database.getDatabaseName(), databaseName);
                    return;
                }
                model.getDatabases().removeIf(d -> databaseName.equals(d.getDatabaseName()));
                model.getDatabases().add(database);
            }

            FileObject settingsFile = databaseDir.resolveFile("hibernate.properties");
            if(settingsFile.exists()) {
                try(InputStream inputStream = settingsFile.getContent().getInputStream()) {
                    Properties settings = new Properties();
                    settings.load(inputStream);
                    database.setSettings(settings);
                }
            }
        } else {
            database = DatabaseLogic.findDatabaseByName(model, databaseName);
            if(database != null) {
                logger.info("Using legacy database defined in portofino-model.xml: " + databaseName + "; it will be automatically migrated to database.xml upon save.");
            } else {
                logger.warn("No database defined in " + databaseDir.getName().getPath());
                return;
            }
        }

        for(Schema schema : database.getSchemas()) {
            FileObject schemaDir = databaseDir.resolveFile(schema.getSchemaName());
            if(schemaDir.getType() == FileType.FOLDER) {
                logger.debug("Schema directory {} exists", schemaDir);
                FileObject[] tableFiles = schemaDir.getChildren();
                for(FileObject tableFile : tableFiles) {
                    if(!tableFile.getName().getBaseName().endsWith(".table.xml")) {
                        continue;
                    }
                    try(InputStream tableInputStream = tableFile.getContent().getInputStream()) {
                        Table table = (Table) um.unmarshal(tableInputStream);
                        if (!tableFile.getName().getBaseName().equalsIgnoreCase(table.getTableName() + ".table.xml")) {
                            logger.error("Skipping table " + table.getTableName() + " defined in file " + tableFile);
                            continue;
                        }
                        table.setParent(schema);
                        schema.getTables().add(table);
                    }
                }
            } else {
                logger.debug("Schema directory {} does not exist", schemaDir);
            }
        }
    }

    public FileObject getLegacyModelFile() throws FileSystemException {
        return modelDirectory.getParent().resolveFile(LEGACY_APP_MODEL_FILE);
    }

    @Override
    public FileObject getModelDirectory() {
        return modelDirectory;
    }

    @Override
    public void save(Model model, FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile)
            throws IOException, ConfigurationException {
        try {
            JAXBContext jc = createModelJAXBContext();
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            FileObject modelDir = getModelDirectory();
            modelDir.createFolder();
            for (Database database : model.getDatabases()) {
                FileObject databaseDir = modelDir.resolveFile(database.getDatabaseName());
                FileObject databaseFile = databaseDir.resolveFile("database.xml");
                databaseFile.createFile();
                try (OutputStream outputStream = databaseFile.getContent().getOutputStream()) {
                    m.marshal(database, outputStream);
                }
                for (Schema schema : database.getSchemas()) {
                    FileObject schemaDir = databaseDir.resolveFile(schema.getSchemaName());
                    if (!schemaDir.exists()) {
                        logger.debug("Schema directory {} does not exist", schemaDir);
                        schemaDir.createFolder();
                    }
                    FileObject[] tableFiles = schemaDir.getChildren();
                    for (FileObject tableFile : tableFiles) {
                        if (tableFile.getName().getBaseName().endsWith(".table.xml")) {
                            if (!tableFile.delete()) {
                                logger.warn("Could not delete table file {}", tableFile.getName().getPath());
                            }
                        }
                    }
                    for (Table table : schema.getTables()) {
                        FileObject tableFile = schemaDir.resolveFile(table.getTableName() + ".table.xml");
                        try (OutputStream outputStream = tableFile.getContent().getOutputStream()) {
                            m.marshal(table, outputStream);
                        }
                    }
                }
                deleteUnusedSchemaDirectories(database, databaseDir);
            }
            deleteUnusedDatabaseDirectories(model);
            logger.info("Saved xml model to directory: {}", modelDir.getName().getPath());
            if (configurationFile != null) {
                configurationFile.save();
                logger.info("Saved configuration file {}", configurationFile.getFileHandler().getFile().getAbsolutePath());
            }

            FileObject appModelFile = getLegacyModelFile();
            if (appModelFile.exists()) {
                appModelFile.delete();
                logger.info("Deleted legacy portofino-model.xml file: {}", appModelFile.getName().getPath());
            }
        } catch (JAXBException e) {
            throw new IOException("Error marshalling model", e);
        }
    }

    /**
     * Delete the directories of the databases that are no longer present in the model
     *
     * @throws FileSystemException if the schema directories cannot be listed.
     */
    protected void deleteUnusedDatabaseDirectories(Model model) throws FileSystemException {
        Arrays.stream(getModelDirectory().getChildren()).forEach(dbDir -> {
            String dbDirPath = dbDir.getName().getPath();
            try {
                if(dbDir.getType() == FileType.FOLDER) {
                    String dirName = dbDir.getName().getBaseName();
                    if (model.getDatabases().stream().noneMatch(db -> db.getDatabaseName().equals(dirName))) {
                        logger.info("Deleting unused database directory {}", dbDirPath);
                        try {
                            dbDir.deleteAll();
                        } catch (FileSystemException e) {
                            logger.warn("Could not delete unused database dir " + dbDirPath, e);
                        }
                    }
                }
            } catch (FileSystemException e) {
                logger.error("Unexpected filesystem error when trying to delete schema directory " + dbDirPath, e);
            }
        });
    }

    /**
     * Delete the directories of the schemas that are no longer present in the model
     *
     * @param database the parent database containing the schemas
     * @param databaseDir the database directory
     * @throws FileSystemException if the schema directories cannot be listed.
     */
    protected void deleteUnusedSchemaDirectories(Database database, FileObject databaseDir) throws FileSystemException {
        Arrays.stream(databaseDir.getChildren()).forEach(schemaDir -> {
            String schemaDirPath = schemaDir.getName().getPath();
            try {
                if(schemaDir.getType() == FileType.FOLDER) {
                    String dirName = schemaDir.getName().getBaseName();
                    if (database.getSchemas().stream().noneMatch(schema -> schema.getSchemaName().equals(dirName))) {
                        logger.info("Deleting unused schema directory {}", schemaDirPath);
                        try {
                            schemaDir.deleteAll();
                        } catch (FileSystemException e) {
                            logger.warn("Could not delete unused schema dir " + schemaDirPath, e);
                        }
                    }
                }
            } catch (FileSystemException e) {
                logger.error("Unexpected filesystem error when trying to delete schema directory " + schemaDirPath, e);
            }
        });
    }

    @Override
    public void delete() throws IOException {
        FileObject appModelFile = getLegacyModelFile();
        if (appModelFile.exists()) {
            appModelFile.delete();
        }
        getModelDirectory().delete(new PatternFileSelector("(database|.*[.]table)[.]xml"));
    }
}
