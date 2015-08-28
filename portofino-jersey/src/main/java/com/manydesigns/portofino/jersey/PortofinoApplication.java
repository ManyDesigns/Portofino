package com.manydesigns.portofino.jersey;

import com.manydesigns.portofino.pageactions.rest.APIRoot;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath(APIRoot.PATH_PREFIX)
public class PortofinoApplication extends ResourceConfig {

    public PortofinoApplication() {
        packages("com.manydesigns");
        register(JacksonFeature.class);
    }

}
