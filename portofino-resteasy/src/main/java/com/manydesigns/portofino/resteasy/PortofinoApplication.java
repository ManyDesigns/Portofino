package com.manydesigns.portofino.resteasy;

import com.manydesigns.portofino.rest.PortofinoApplicationRoot;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class PortofinoApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(ApiListingResource.class);
        classes.add(SwaggerSerializers.class);
        classes.add(PortofinoApplicationRoot.class);
        //TODO discovery?
        //TODO configure user classes
        return classes;
    }

}
