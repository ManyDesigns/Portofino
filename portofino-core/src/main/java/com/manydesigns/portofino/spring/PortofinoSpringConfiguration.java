package com.manydesigns.portofino.spring;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.blobs.HierarchicalBlobManager;
import com.manydesigns.elements.blobs.SimpleBlobManager;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.dispatcher.web.DispatcherInitializer;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.modules.ModuleRegistry;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.io.File;

@org.springframework.context.annotation.Configuration
public class PortofinoSpringConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PortofinoSpringConfiguration.class);
    public static final String PORTOFINO_CONFIGURATION = "portofinoConfiguration";
    public static final String APPLICATION_DIRECTORY = "applicationDirectory";
    public static final String TEMPORARY_BLOB_MANAGER = "temporaryBlobManager";
    public static final String DEFAULT_BLOB_MANAGER = "defaultBlobManager";

    protected ServletContext servletContext;

    @Bean(name = PORTOFINO_CONFIGURATION)
    public Configuration getPortofinoConfiguration() {
        return (Configuration) getServletContext().getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
    }

    @Bean(name = APPLICATION_DIRECTORY)
    public File getApplicationDirectory() {
        return (File) getServletContext().getAttribute(BaseModule.APPLICATION_DIRECTORY);
    }

    @Bean
    public CodeBase getCodeBase() {
        return (CodeBase) getServletContext().getAttribute(DispatcherInitializer.CODE_BASE_ATTRIBUTE);
    }

    @Bean
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Autowired
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Bean
    public ModuleRegistry getModuleRegistry(@Autowired Configuration configuration) {
        return new ModuleRegistry(configuration);
    }

    @Bean(name = TEMPORARY_BLOB_MANAGER)
    public BlobManager getTemporaryBlobManager(@Autowired ServletContext servletContext) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        String tmdDirName = servletContext.getContextPath().replace("/", "-");
        File tempBlobsDir = new File(tmpDir, "portofino-blobs" + tmdDirName);
        logger.info("Temporary blobs directory: " + tempBlobsDir.getAbsolutePath());
        String metaFilenamePattern = "blob-{0}.properties";
        String dataFilenamePattern = "blob-{0}.data";
        return new HierarchicalBlobManager(tempBlobsDir, metaFilenamePattern, dataFilenamePattern);
    }

    @Bean(name = DEFAULT_BLOB_MANAGER)
    public BlobManager getDefaultBlobManager(
            @Autowired @Qualifier(PORTOFINO_CONFIGURATION) Configuration configuration,
            @Autowired @Qualifier(APPLICATION_DIRECTORY) File applicationDirectory
    ) {
        File appBlobsDir;
        if(configuration.containsKey(PortofinoProperties.BLOBS_DIR_PATH)) {
            appBlobsDir = new File(configuration.getString(PortofinoProperties.BLOBS_DIR_PATH));
        } else {
            appBlobsDir = new File(applicationDirectory, "blobs");
        }
        logger.info("Blobs directory: " + appBlobsDir.getAbsolutePath());

        String metaFilenamePattern = "blob-{0}.properties";
        String dataFilenamePattern = "blob-{0}.data";
        File[] blobs = appBlobsDir.listFiles((dir, name) -> name.startsWith("blob-") && name.endsWith(".properties"));
        if(blobs == null || blobs.length == 0) { //Null if the directory does not exist yet
            logger.info("Using hierarchical blob manager");
            return new HierarchicalBlobManager(appBlobsDir, metaFilenamePattern, dataFilenamePattern);
        } else {
            logger.warn("Blobs found directly under the blobs directory; using old style (pre-4.1.1) flat file blob manager");
            return new SimpleBlobManager(appBlobsDir, metaFilenamePattern, dataFilenamePattern);
        }
    }

    @Bean
    public ModuleInitializer getModuleInitializer() {
        return new ModuleInitializer();
    }

    @Bean
    public ModuleDestroyer getModuleDestroyer() {
        return new ModuleDestroyer();
    }

    public static class ModuleInitializer extends ModuleEventListener<ContextRefreshedEvent> implements ApplicationContextAware {

        protected ApplicationContext applicationContext;

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            ElementsThreadLocals.setupDefaultElementsContext();
            ElementsThreadLocals.setServletContext(servletContext);
            try {
                logger.info("Loading modules...");
                moduleRegistry.getModules().addAll(applicationContext.getBeansOfType(Module.class).values());
                moduleRegistry.migrateAndInit(servletContext);
                logger.info("Starting modules...");
                moduleRegistry.start();
                logger.info("Modules started.");
            } finally {
                ElementsThreadLocals.removeElementsContext();
            }
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }

    public static class ModuleDestroyer extends ModuleEventListener<ContextClosedEvent> {

        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            logger.info("Stopping modules...");
            moduleRegistry.stop();
            logger.info("Destroying modules...");
            moduleRegistry.destroy();
            logger.info("Modules destroyed.");
        }
    }

}
