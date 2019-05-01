package com.manydesigns.portofino.resteasy;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class PortofinoApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(OpenApiResource.class);
        classes.add(ResteasyApplicationRoot.class);
        //TODO discovery?
        //TODO configure user classes
        return classes;
    }

}
