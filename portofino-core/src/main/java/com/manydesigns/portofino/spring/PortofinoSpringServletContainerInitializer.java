package com.manydesigns.portofino.spring;

import com.manydesigns.portofino.modules.Module;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.annotation.HandlesTypes;
import java.util.Set;

@HandlesTypes(Module.class)
public class PortofinoSpringServletContainerInitializer implements ServletContainerInitializer {

    protected ServletContext servletContext;

    @Override
    public void onStartup(Set<Class<?>> moduleClasses, ServletContext servletContext) {
        this.servletContext = servletContext;
        registerContextLoaderListener(servletContext, moduleClasses);
    }

    protected void registerContextLoaderListener(ServletContext servletContext, Set<Class<?>> moduleClasses) {
        ContextLoaderListener listener = new PortofinoContextLoaderListener(moduleClasses);
        servletContext.addListener(listener);
    }

}
