package com.manydesigns.portofino.spring;

import com.manydesigns.portofino.modules.BaseModule;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;

@Component
public class DefaultSpringConfiguration {

    protected ServletContext servletContext;

    @Bean
    public Configuration getPortofinoConfiguration() {
        return (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
    }

    @Bean
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Autowired
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
