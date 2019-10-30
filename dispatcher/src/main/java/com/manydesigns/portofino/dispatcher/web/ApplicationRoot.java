package com.manydesigns.portofino.dispatcher.web;

import com.manydesigns.portofino.dispatcher.Resource;
import com.manydesigns.portofino.dispatcher.swagger.DocumentedApiRoot;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;

/**
 * Created by alessio on 4/28/16.
 */
@Path("/")
public class ApplicationRoot extends DocumentedApiRoot {

    protected static final Logger logger = LoggerFactory.getLogger(ApplicationRoot.class);
    public static final String PORTOFINO_CONFIGURATION_ATTRIBUTE = "portofino.configuration";

    @Context
    protected ServletContext servletContext;
    
    @Context
    protected ResourceContext resourceContext;

    protected Configuration getConfiguration() {
        return (Configuration) servletContext.getAttribute(PORTOFINO_CONFIGURATION_ATTRIBUTE);
    }

    @Path("")
    public Object start() throws Exception {
        Resource root = rootFactory.createRoot();
        resourceContext.initResource(root);
        return root.init();
    }

}
