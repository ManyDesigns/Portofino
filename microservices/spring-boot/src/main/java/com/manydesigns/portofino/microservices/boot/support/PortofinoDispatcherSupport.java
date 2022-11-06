package com.manydesigns.portofino.microservices.boot.support;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.JacksonResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.JavaResourceResolver;
import com.manydesigns.portofino.servlets.PortofinoDispatcherInitializer;
import org.apache.commons.vfs2.FileObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.util.HashSet;
import java.util.Set;

import static com.manydesigns.portofino.spring.PortofinoSpringConfiguration.APPLICATION_DIRECTORY;

@Configuration
public class PortofinoDispatcherSupport {

    @Autowired
    protected ServletContext servletContext;

    @Autowired
    @Qualifier(APPLICATION_DIRECTORY)
    protected FileObject applicationDirectory;

    @PostConstruct
    public void initDispatcher() {
        Set<Class<? extends CodeBase>> cbClasses = new HashSet<>();
        Set<Class<? extends ResourceResolver>> resres = new HashSet<>();
        PortofinoDispatcherInitializer initializer = new PortofinoDispatcherInitializer() {
            @Override
            protected String getApplicationDirectoryPath() {
                return applicationDirectory.getName().getURI();
            }
        };
        initializer.initWithServletContext(servletContext);
    }

}
