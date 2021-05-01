package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.servlets.PortofinoDispatcherInitializer;
import com.manydesigns.portofino.spring.PortofinoContextLoaderListener;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import com.manydesigns.portofino.spring.PortofinoWebSpringConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.Set;

public class PortofinoAnnotationConfigServletWebServerApplicationContext extends AnnotationConfigServletWebServerApplicationContext {

    protected final String applicationDirectoryPath;

    public PortofinoAnnotationConfigServletWebServerApplicationContext(String applicationDirectoryPath) {
        this.applicationDirectoryPath = applicationDirectoryPath;
    }

    @Override
    protected void prepareWebApplicationContext(ServletContext servletContext) {
        super.prepareWebApplicationContext(servletContext);
        ElementsThreadLocals.setupDefaultElementsContext();
        ElementsThreadLocals.setServletContext(servletContext);
        PortofinoDispatcherInitializer initializer = new PortofinoDispatcherInitializer() {
            @Override
            protected String getApplicationDirectoryPath() {
                return applicationDirectoryPath;
            }
        };
        initializer.initWithServletContext(servletContext);

        ApplicationContext grandParent = PortofinoContextLoaderListener.setupGrandParentContext(initializer);

        AnnotationConfigWebApplicationContext parent = new AnnotationConfigWebApplicationContext();
        parent.setParent(grandParent);
        parent.setServletContext(servletContext);

        //Modules
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false, getEnvironment());
        scanner.addIncludeFilter(new AssignableTypeFilter(Module.class));
        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents("");
        candidateComponents.forEach(c -> {
            try {
                parent.register(initializer.getCodeBase().loadClass(c.getBeanClassName()));
            } catch (Exception e) {
                PortofinoBootApplication.logger.error("Could not load module class", e);
            }
        });

        parent.register(PortofinoWebSpringConfiguration.class);
        parent.register(PortofinoSpringConfiguration.class);
        parent.refresh();
        setParent(parent);
    }
}
