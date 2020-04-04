package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.Database;
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

public class DefaultModel implements ModelIO {

    private static final Logger logger = LoggerFactory.getLogger(DefaultModel.class);

    private final FileObject modelDirectory;

    public DefaultModel(FileObject modelDirectory) {
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

            loadDatabasePersistence(modelDir, model);
        }
        return model;
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

    }

    @Override
    public FileObject getModelDirectory() throws FileSystemException {
        return modelDirectory;
    }
}
