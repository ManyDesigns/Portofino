package com.manydesigns.portofino.spring;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.servlets.PortofinoListener;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

@HandlesTypes(Module.class)
public class PortofinoSpringServletContainerInitializer implements ServletContainerInitializer {

    private static final Logger logger = LoggerFactory.getLogger(PortofinoSpringServletContainerInitializer.class);

    protected ServletContext servletContext;
    protected final Set<Class<? extends Module>> moduleClasses = new HashSet<>();

    @Override
    public void onStartup(Set<Class<?>> moduleClasses, ServletContext servletContext) throws ServletException {
        this.servletContext = servletContext;
        for(Class<?> candidate: moduleClasses) {
            if(!candidate.isInterface() && !Modifier.isAbstract(candidate.getModifiers()) &&
               Module.class.isAssignableFrom(candidate)) {
               this.moduleClasses.add(candidate.asSubclass(Module.class));
            }
        }
        registerContextLoaderListener(servletContext);
    }

    protected void registerContextLoaderListener(ServletContext servletContext) {
        WebApplicationContext rootAppContext = createRootApplicationContext();
        ContextLoaderListener listener = new ContextLoaderListener(rootAppContext);
        listener.setContextInitializers(getRootApplicationContextInitializers());
        servletContext.addListener(listener);
    }

    protected WebApplicationContext createRootApplicationContext() {
        return new AnnotationConfigWebApplicationContext();
    }

    protected ApplicationContextInitializer<?>[] getRootApplicationContextInitializers() {
        return new ApplicationContextInitializer[] {
                applicationContext -> {
                    ElementsThreadLocals.setupDefaultElementsContext();
                    MutablePropertySources sources = applicationContext.getEnvironment().getPropertySources();
                    Configuration configuration =
                            (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
                    sources.addFirst(
                            new ConfigurationPropertySource("portofino.properties", configuration));
                    AnnotationConfigRegistry annotationConfig = (AnnotationConfigRegistry) applicationContext;
                    for(Class<?> moduleClass : moduleClasses) {
                        annotationConfig.register(moduleClass);
                    }
                    annotationConfig.register(PortofinoSpringConfiguration.class);
                    CodeBase codeBase = (CodeBase) servletContext.getAttribute(PortofinoListener.CODE_BASE_ATTRIBUTE);
                    ((DefaultResourceLoader) applicationContext).setClassLoader(codeBase.asClassLoader());
                    try {
                        annotationConfig.register(codeBase.loadClass("SpringConfiguration"));
                    } catch (Exception e) {
                        logger.debug("User-defined Spring configuration not found", e);
                    }
                }
        };
    }
}