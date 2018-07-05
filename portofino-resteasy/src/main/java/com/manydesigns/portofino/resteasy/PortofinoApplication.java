package com.manydesigns.portofino.resteasy;

import com.manydesigns.portofino.pageactions.rest.APIRoot;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath(APIRoot.PATH_PREFIX)
public class PortofinoApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(APIRoot.class);
        //TODO configure user classes
        return classes;
    }

}
