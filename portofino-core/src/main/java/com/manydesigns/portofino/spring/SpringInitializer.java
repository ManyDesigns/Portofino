package com.manydesigns.portofino.spring;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.rest.PortofinoApplicationRoot;
import com.manydesigns.portofino.servlets.PortofinoListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.web.context.AbstractContextLoaderInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;

public class SpringInitializer extends AbstractContextLoaderInitializer {

    protected ServletContext servletContext;
    private static final Logger logger = LoggerFactory.getLogger(SpringInitializer.class);

    @Override
    public void onStartup(@NotNull ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);
        this.servletContext = servletContext;
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        return new AnnotationConfigWebApplicationContext();
    }

    @Override
    protected ApplicationContextInitializer<?>[] getRootApplicationContextInitializers() {
        return new ApplicationContextInitializer[] {
                applicationContext -> {
                    AnnotationConfigRegistry annotationConfig = (AnnotationConfigRegistry) applicationContext;
                    annotationConfig.register(DefaultSpringConfiguration.class);
                    CodeBase codeBase = (CodeBase) servletContext.getAttribute(PortofinoListener.CODE_BASE_ATTRIBUTE);
                    try {
                        annotationConfig.register(codeBase.loadClass("SpringConfiguration"));
                    } catch (Exception e) {
                        logger.debug("User-defined Spring configuration not found", e);
                    }
                }
        };
    }
}
