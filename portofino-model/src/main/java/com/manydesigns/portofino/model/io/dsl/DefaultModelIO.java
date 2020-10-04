package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.io.ModelIO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
        if(!modelDirectory.getType().equals(FileType.FOLDER)) {
            logger.error("Not a directory: " + modelDirectory.getName().getPath());
            return model;
        }
        if (modelDir.exists()) {
            loadEntities(modelDir, model.getDomains());
            loadDatabasePersistence(modelDir, model);
        }
        return model;
    }

    protected void loadEntities(FileObject directory, List<Domain> domains) throws IOException {
        for(FileObject dir : directory.getChildren()) {
            String domainName = dir.getName().getBaseName();
            FileObject domainDefFile = dir.resolveFile(domainName + "domain");
            if(domainDefFile.exists()) {
                try(InputStream inputStream = domainDefFile.getContent().getInputStream()) {
                    ModelLexer lexer = new ModelLexer(CharStreams.fromStream(inputStream));
                    ModelParser parser = new ModelParser(new CommonTokenStream(lexer));
                    ModelParser.StandaloneDomainContext parseTree = parser.standaloneDomain();
                    if(parser.getNumberOfSyntaxErrors() > 0) {
                        logger.error("Could not parse domain definition file " + domainDefFile.getName().getPath()); //TODO properly report errors
                        continue;
                    }

                }
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
                if(parser.getNumberOfSyntaxErrors() > 0) {
                    logger.error("Could not parse database connections file"); //TODO properly report errors
                    return;
                }
                new DatabasePersistenceVisitor(model).visit(parseTree);
            }
        } else {
            logger.info("No database persistence defined in " + modelDirectory.getName().getPath());
        }
    }

    @Override
    public void save(Model model, FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile) throws IOException, ConfigurationException {
        logger.info("Saving model into directory: {}", getModelDirectory().getName().getPath());
        FileObject modelDir = getModelDirectory();
        if(!modelDirectory.getType().equals(FileType.FOLDER)) {
            throw new IOException("Not a directory: " + modelDirectory.getName().getPath());
        }
        if (modelDir.exists()) {
            saveEntities(modelDir, model.getDomains());
            saveDatabasePersistence(modelDir, model);
        }
    }

    protected void saveDatabasePersistence(FileObject modelDir, Model model) {
        //TODO
    }

    protected void saveEntities(FileObject modelDir, List<Domain> domains) {
        //TODO
    }

    @Override
    public FileObject getModelDirectory() throws FileSystemException {
        return modelDirectory;
    }
}
