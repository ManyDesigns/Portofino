package com.manydesigns.portofino.modules;

import com.manydesigns.elements.blobs.S3BlobManagerFactory;
import com.manydesigns.portofino.config.ConfigurationSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class S3Module implements Module {

    private ModuleStatus moduleStatus = ModuleStatus.STARTED;

    @Autowired
    public ConfigurationSource configuration;

    @Bean
    public S3BlobManagerFactory getS3BlobManagerFactory() {
        return new S3BlobManagerFactory(configuration.getProperties());
    }

    @PostConstruct
    public void init() {
        moduleStatus = ModuleStatus.STARTED;
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
