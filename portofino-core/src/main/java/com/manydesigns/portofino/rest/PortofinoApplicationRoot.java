package com.manydesigns.portofino.rest;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.dispatcher.web.ApplicationRoot;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceContext;

@Path("/")
public class PortofinoApplicationRoot extends ApplicationRoot {

    @Override
    protected ResourceContext getResourceContext() {
        ServletContext servletContext = ElementsThreadLocals.getServletContext();
        final WebApplicationContext applicationContext =
                WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        return new DummyResourceContext() {
            @Override
            public <T> T initResource(T resource) {
                super.initResource(resource);
                applicationContext.getAutowireCapableBeanFactory().autowireBean(resource);
                return resource;
            }
        };
    }
}
