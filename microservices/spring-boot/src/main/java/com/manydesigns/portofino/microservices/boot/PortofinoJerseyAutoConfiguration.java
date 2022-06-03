package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.portofino.jersey.PortofinoApplication;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(value = PortofinoApplication.class)
@AutoConfigureAfter(PortofinoDispatcherAutoConfiguration.class)
public class PortofinoJerseyAutoConfiguration {

    private String basePath;
    private static final Logger logger = LoggerFactory.getLogger(PortofinoJerseyAutoConfiguration.class);

    @Bean
    public ResourceConfig getJerseyApplication() {
        return new PortofinoApplication().property(ServletProperties.FILTER_FORWARD_ON_404, true);
    }

    @Value("${spring.jersey.application-path}")
    public void setBasePath(String basePath) {
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }
        while (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        basePath = basePath + "/";
        this.basePath = basePath;
    }

    @PostConstruct
    public void announceApiPath() {
        logger.info("API path: " + basePath);
    }

}
