package com.manydesigns.portofino.jersey;

import com.manydesigns.portofino.rest.PortofinoApplicationRoot;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;
import java.util.Collections;

@ApplicationPath("/")
public class PortofinoApplication extends ResourceConfig {

    @Context
    protected ServletConfig config;

    public PortofinoApplication() throws OpenApiConfigurationException {
        packages("com.manydesigns.portofino.rest"); //TODO configure user packages
        new JaxrsOpenApiContextBuilder()
                .servletConfig(config)
                .application(this)
                .resourceClasses(Collections.singleton(PortofinoApplicationRoot.class.getName()))
                .buildContext(true);
        register(OpenApiResource.class);
        register(JacksonFeature.class);
    }

}
