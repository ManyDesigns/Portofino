package com.manydesigns.elements.blobs;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DefaultBlobManagerFactory implements BlobManagerFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBlobManagerFactory.class);

    protected Configuration configuration;
    protected FileObject applicationDirectory;

    public static final String BLOBS_DIR_PATH = "blobs.dir.path";

    public DefaultBlobManagerFactory(Configuration configuration, FileObject applicationDirectory) {
        this.configuration = configuration;
        this.applicationDirectory = applicationDirectory;
    }

    @Override
    public BlobManager getBlobManager() {
        File appBlobsDir;
        if(configuration.containsKey(BLOBS_DIR_PATH)) {
            appBlobsDir = new File(configuration.getString(BLOBS_DIR_PATH));
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
            logger.warn("Blobs found directly under the blobs directory: using old style (pre-4.1.1) flat file blob manager");
            return new SimpleBlobManager(appBlobsDir, metaFilenamePattern, dataFilenamePattern);
        }
    }

    @Override
    public boolean accept(String type) {
        return "standard".equals(type);
    }
}
