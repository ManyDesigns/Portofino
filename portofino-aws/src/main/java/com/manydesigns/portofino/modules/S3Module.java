package com.manydesigns.portofino.modules;

import com.manydesigns.elements.blobs.BlobManagerFactory;
import com.manydesigns.elements.blobs.S3BlobManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class S3Module implements Module {

    private ModuleStatus moduleStatus = ModuleStatus.STARTED;

    @Autowired
    @Qualifier("com.manydesigns.portofino.portofinoConfiguration")
    org.apache.commons.configuration2.Configuration configuration;

    @Bean
    public BlobManagerFactory s3BlobManagerFactory() {
        return new S3BlobManagerFactory(configuration);
    }

    @PostConstruct
    public void init() {
        moduleStatus = ModuleStatus.ACTIVE;
    }

    @PreDestroy
    public void destroy() {
        moduleStatus = ModuleStatus.DESTROYED;
    }

    @Override
    public String getModuleVersion() {
        return "1.0";
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public ModuleStatus getStatus() {
        return moduleStatus;
    }
}
