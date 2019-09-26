package com.manydesigns.portofino.spring;

import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.blobs.HierarchicalBlobManager;
import com.manydesigns.elements.blobs.SimpleBlobManager;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.cache.CacheResetListenerRegistry;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.dispatcher.web.DispatcherInitializer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import javax.servlet.ServletContext;
import java.io.File;

@org.springframework.context.annotation.Configuration
public class PortofinoSpringConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PortofinoSpringConfiguration.class);
    public static final String ACTIONS_DIRECTORY = "actionsDirectory";
    public static final String APPLICATION_DIRECTORY = "com.manydesigns.portofino.application.directory";
    public static final String DEFAULT_BLOB_MANAGER = "defaultBlobManager";
    public final static String PORTOFINO_CONFIGURATION = "com.manydesigns.portofino.portofinoConfiguration";

    protected ServletContext servletContext;

    @Bean(name = PORTOFINO_CONFIGURATION)
    public Configuration getPortofinoConfiguration() {
        return (Configuration) getServletContext().getAttribute(PORTOFINO_CONFIGURATION);
    }

    @Bean(name = APPLICATION_DIRECTORY)
    public FileObject getApplicationDirectory() {
        return (FileObject) getServletContext().getAttribute(APPLICATION_DIRECTORY);
    }

    @Bean(name = ACTIONS_DIRECTORY)
    public FileObject getApplicationDirectory(
            @Autowired @Qualifier(PORTOFINO_CONFIGURATION) Configuration configuration,
            @Autowired @Qualifier(APPLICATION_DIRECTORY) FileObject applicationDirectory) throws FileSystemException {
        String actionsDirectory = configuration.getString("portofino.actions.path", "actions");
        return applicationDirectory.resolveFile(actionsDirectory);
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

    @Bean(name = DEFAULT_BLOB_MANAGER)
    public BlobManager getDefaultBlobManager(
            @Autowired @Qualifier(PORTOFINO_CONFIGURATION) Configuration configuration,
            @Autowired @Qualifier(APPLICATION_DIRECTORY) FileObject applicationDirectory) {
        File appBlobsDir;
        if(configuration.containsKey(PortofinoProperties.BLOBS_DIR_PATH)) {
            appBlobsDir = new File(configuration.getString(PortofinoProperties.BLOBS_DIR_PATH));
        } else {
            appBlobsDir = new File(applicationDirectory.getName().getPath(), "blobs");
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
    public CacheResetListenerRegistry getCacheResetListenerRegistry() {
        return new CacheResetListenerRegistry();
    }


}
