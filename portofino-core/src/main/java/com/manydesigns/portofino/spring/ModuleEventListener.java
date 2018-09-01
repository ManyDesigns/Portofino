package com.manydesigns.portofino.spring;

import com.manydesigns.portofino.modules.ModuleRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;

public abstract class ModuleEventListener<E extends ApplicationEvent> implements ApplicationListener<E> {

    protected ModuleRegistry moduleRegistry;
    protected ServletContext servletContext;

    public ModuleRegistry getModuleRegistry() {
        return moduleRegistry;
    }

    @Autowired
    public void setModuleRegistry(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    @Autowired
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
