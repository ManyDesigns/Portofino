package com.manydesigns.elements.blobs;

import com.manydesigns.portofino.PortofinoProperties;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3BlobManagerFactory extends DefaultBlobManagerFactory {

    private static final Logger logger = LoggerFactory.getLogger(S3BlobManagerFactory.class);

    public S3BlobManagerFactory(Configuration configuration, FileObject applicationDirectory) {
        super(configuration, applicationDirectory);
    }

    @Override
    public BlobManager getBlobManager() {
        if (configuration.getString(PortofinoProperties.BLOB_MANAGER_TYPE, "standard").equalsIgnoreCase("s3")) {
            String region = configuration.getString(PortofinoProperties.AWS_REGION);
            String bucketName = configuration.getString(PortofinoProperties.AWS_S3_BUCKET);
            String location = configuration.getString(PortofinoProperties.AWS_S3_LOCATION);
            logger.info("Using S3 blob manager");
            if (StringUtils.trimToNull(region) == null) {
                logger.error(PortofinoProperties.AWS_REGION + " property not found");
            }
            if (StringUtils.trimToNull(bucketName) == null) {
                logger.error(PortofinoProperties.AWS_S3_BUCKET + " property not found");
            }
            if (StringUtils.trimToNull(location) == null)
                return new S3BlobManager(region, bucketName);
            else
                return new S3BlobManager(region, bucketName, location);
        }

        return super.getBlobManager();
    }
}
