package com.manydesigns.elements.blobs;

import com.manydesigns.portofino.PortofinoProperties;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3BlobManagerFactory implements BlobManagerFactory {

    private static final Logger logger = LoggerFactory.getLogger(S3BlobManagerFactory.class);

    private Configuration configuration;

    public S3BlobManagerFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public BlobManager getBlobManager() {
        String region = configuration.getString(PortofinoProperties.AWS_REGION);
        String bucketName = configuration.getString(PortofinoProperties.AWS_S3_BUCKET);
        String location = configuration.getString(PortofinoProperties.AWS_S3_LOCATION);
        logger.info("Using S3 blob manager");
        boolean inError = false;

        if (StringUtils.trimToNull(region) == null) {
            logger.error(PortofinoProperties.AWS_REGION + " property not found");
            inError = true;
        }
        if (StringUtils.trimToNull(bucketName) == null) {
            logger.error(PortofinoProperties.AWS_S3_BUCKET + " property not found");
            inError = true;
        }
        if (inError) {
            throw new RuntimeException("Error while creating a S3 blob manager");
        }

        if (StringUtils.trimToNull(location) == null)
            return new S3BlobManager(region, bucketName);
        else
            return new S3BlobManager(region, bucketName, location);
    }

    @Override
    public boolean accept(String type) {
        return "s3".equals(type);
    }
}
