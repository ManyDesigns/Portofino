package com.manydesigns.portofino.resteasy;

import com.manydesigns.portofino.rest.PortofinoApplicationRoot;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;

import javax.servlet.ServletConfig;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class PortofinoApplication extends Application {

    @Context
    protected ServletConfig config;

    public PortofinoApplication() throws OpenApiConfigurationException {
        new JaxrsOpenApiContextBuilder()
                .servletConfig(config)
                .application(this)
                .resourceClasses(Collections.singleton(PortofinoApplicationRoot.class.getName()))
                .buildContext(true);
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(OpenApiResource.class);
        classes.add(PortofinoApplicationRoot.class);
        //TODO discovery?
        //TODO configure user classes
        return classes;
    }

}
