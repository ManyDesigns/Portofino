package com.manydesigns.portofino.jersey;

import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class PortofinoApplication extends ResourceConfig {

    public PortofinoApplication() {
        packages("com.manydesigns.portofino.rest"); //TODO configure user packages
        register(ApiListingResource.class);
        register(SwaggerSerializers.class);
        register(JacksonFeature.class);
    }

}
