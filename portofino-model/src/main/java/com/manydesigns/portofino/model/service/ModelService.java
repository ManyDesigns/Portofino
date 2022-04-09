package com.manydesigns.portofino.model.service;

import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.io.ModelIO;
import com.manydesigns.portofino.model.io.dsl.DefaultModelIO;
import io.reactivex.subjects.PublishSubject;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ModelService {

    protected Model model = new Model();
    public final PublishSubject<EventType> modelEvents = PublishSubject.create();
    public static final String APP_MODEL_DIRECTORY = "portofino-model";
    protected final FileObject applicationDirectory;
    protected final Configuration configuration;
    protected final FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile;
    protected final List<Domain> builtInDomains = new CopyOnWriteArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(ModelService.class);

    public ModelService(
            FileObject applicationDirectory, Configuration configuration,
            FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile) {
        this.applicationDirectory = applicationDirectory;
        this.configuration = configuration;
        this.configurationFile = configurationFile;
    }

    public enum EventType {
        LOADED, SAVED
    }

    public Model getModel() {
        return model;
    }

    public synchronized Model loadModel(ModelIO modelIO) throws IOException {
        Model loaded = modelIO.load();
        if(loaded != null) {
            loaded.getDomains().addAll(builtInDomains);
            loaded.init();
            model = loaded;
            modelEvents.onNext(EventType.LOADED);
            return model;
        } else {
            return null;
        }
    }

    public synchronized void loadModel() throws IOException {
        loadModel(new DefaultModelIO(getModelDirectory()));
    }

    public FileObject getModelDirectory() throws FileSystemException {
        return applicationDirectory.resolveFile(APP_MODEL_DIRECTORY);
    }

    public FileObject getApplicationDirectory() {
        return applicationDirectory;
    }

    public synchronized void saveModel() throws IOException, ConfigurationException {
        model.init();
        Model toSave = new Model();
        toSave.getDomains().addAll(model.getDomains());
        toSave.getDomains().removeAll(builtInDomains);
        new DefaultModelIO(getModelDirectory()).save(toSave);
        if (configurationFile != null) {
            configurationFile.save();
            logger.info("Saved configuration file {}", configurationFile.getFileHandler().getFile().getAbsolutePath());
        }
        modelEvents.onNext(EventType.SAVED);
    }

    public void addBuiltInDomain(Domain domain, String path) {
        String[] components = path.split("[.]");
        List<Domain> collection = model.getDomains();
        for (String component : components) {
            Domain subd = Model.ensureDomain(component, collection);
            collection = subd.getSubdomains();
        }
        collection.add(domain);
        domain.freeze();
    }
}
