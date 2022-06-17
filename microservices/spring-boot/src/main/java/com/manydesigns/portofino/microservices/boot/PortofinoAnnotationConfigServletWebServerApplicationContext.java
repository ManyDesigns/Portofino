package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.code.AggregateCodeBase;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.servlets.PortofinoDispatcherInitializer;
import com.manydesigns.portofino.spring.PortofinoContextLoaderListener;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import com.manydesigns.portofino.spring.PortofinoWebSpringConfiguration;
import org.apache.commons.vfs2.FileObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;
import java.util.LinkedHashSet;
import java.util.Set;

public class PortofinoAnnotationConfigServletWebServerApplicationContext extends AnnotationConfigServletWebServerApplicationContext {

    protected final FileObject applicationDirectory;
    private static final Logger logger = LoggerFactory.getLogger(PortofinoAnnotationConfigServletWebServerApplicationContext.class);

    public PortofinoAnnotationConfigServletWebServerApplicationContext(FileObject applicationDirectory) {
        this.applicationDirectory = applicationDirectory;
    }

    @Override
    protected void prepareWebApplicationContext(ServletContext servletContext) {
        super.prepareWebApplicationContext(servletContext);
        ElementsThreadLocals.setupDefaultElementsContext();
        ElementsThreadLocals.setServletContext(servletContext);

        Set<Class<? extends CodeBase>> cbClasses = findImplementations(CodeBase.class);
        Set<Class<? extends ResourceResolver>> resres = findImplementations(ResourceResolver.class);
        PortofinoDispatcherInitializer initializer = new PortofinoDispatcherInitializer(cbClasses, resres) {
            @Override
            protected String getApplicationDirectoryPath() {
                return applicationDirectory.getName().getURI();
            }
        };
        initializer.initWithServletContext(servletContext);

        ApplicationContext grandParent = PortofinoContextLoaderListener.setupGrandParentContext(initializer);

        AnnotationConfigWebApplicationContext parent = new AnnotationConfigWebApplicationContext();
        parent.setParent(grandParent);
        parent.setServletContext(servletContext);

        //Modules
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false, getEnvironment());
        scanner.addIncludeFilter(new AssignableTypeFilter(Module.class));
        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents("");
        candidateComponents.forEach(c -> {
            try {
                parent.register(initializer.getCodeBase().loadClass(c.getBeanClassName()));
            } catch (Exception e) {
                logger.error("Could not load module class", e);
            }
        });

        parent.register(PortofinoWebSpringConfiguration.class);
        parent.register(PortofinoSpringConfiguration.class);

        //User-defined beans
        try {
            Class<?> userConfigurationClass = initializer.getCodeBase().loadClass("SpringConfiguration");
            parent.setClassLoader(initializer.getCodeBase().asClassLoader());
            parent.register(userConfigurationClass);
        } catch (Exception e) {
            logger.info("User-defined Spring configuration not found");
            logger.debug("Additional info", e);
        }

        parent.refresh();
        setParent(parent);
    }

    @NotNull
    protected  <T> Set<Class<? extends T>> findImplementations(Class<T> target) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false, getEnvironment());
        scanner.addIncludeFilter(new AssignableTypeFilter(target));
        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents("");
        Set<Class<? extends T>> implementations = new LinkedHashSet<>();
        candidateComponents.forEach(c -> {
            try {
                Class<?> aClass = Class.forName(c.getBeanClassName());
                if (aClass.isAssignableFrom(target)) {
                    implementations.add((Class<? extends T>) aClass);
                } else {
                    logger.error(aClass + " is not a " + target.getSimpleName() + " implementation");
                }
            } catch (Exception e) {
                logger.error("Could not load module class", e);
            }
        });
        return implementations;
    }
}
