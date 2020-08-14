package com.manydesigns.portofino.resteasy;

import com.manydesigns.mail.rest.SendMailAction;
import com.manydesigns.portofino.rest.PortofinoApplicationRoot;
import com.manydesigns.portofino.rest.PortofinoFilter;
import com.manydesigns.portofino.rest.messagebodywriters.FormMessageBodyWriter;
import com.manydesigns.portofino.rest.messagebodywriters.XhtmlFragmentMessageBodyWriter;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class PortofinoApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(OpenApiResource.class);
        classes.add(PortofinoApplicationRoot.class);
        classes.add(PortofinoFilter.class);
        classes.add(FormMessageBodyWriter.class);
        classes.add(XhtmlFragmentMessageBodyWriter.class);
        classes.add(SendMailAction.class);
        //TODO discovery?
        //TODO configure user classes
        return classes;
    }

}
