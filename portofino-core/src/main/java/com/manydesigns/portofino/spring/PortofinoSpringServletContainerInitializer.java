package com.manydesigns.portofino.spring;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import com.manydesigns.portofino.modules.Module;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.annotation.HandlesTypes;
import java.util.Set;
import java.util.stream.Collectors;

@HandlesTypes({Module.class, CodeBase.class, ResourceResolver.class})
public class PortofinoSpringServletContainerInitializer implements ServletContainerInitializer {

    protected ServletContext servletContext;

    @Override
    public void onStartup(Set<Class<?>> moduleClasses, ServletContext servletContext) {
        this.servletContext = servletContext;
        registerContextLoaderListener(servletContext, moduleClasses);
    }

    protected void registerContextLoaderListener(ServletContext servletContext, Set<Class<?>> classes) {
        Set<Class<?>> moduleClasses = classes.stream().filter(Module.class::isAssignableFrom).collect(Collectors.toSet());
        Set<Class<?>> codebaseClasses = classes.stream().filter(CodeBase.class::isAssignableFrom).collect(Collectors.toSet());
        Set<Class<?>> resresClasses = classes.stream().filter(ResourceResolver.class::isAssignableFrom).collect(Collectors.toSet());
        ContextLoaderListener listener = new PortofinoContextLoaderListener(moduleClasses, codebaseClasses, resresClasses);
        servletContext.addListener(listener);
    }

}
