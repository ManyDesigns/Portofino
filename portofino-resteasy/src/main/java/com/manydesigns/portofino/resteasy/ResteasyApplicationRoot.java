package com.manydesigns.portofino.resteasy;

import com.manydesigns.portofino.rest.PortofinoApplicationRoot;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.plugins.spring.SpringContextLoaderSupport;
import org.jboss.resteasy.plugins.spring.i18n.Messages;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.PostConstruct;
import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

@Path("/")
public class ResteasyApplicationRoot extends PortofinoApplicationRoot {

    @Context
    UriInfo uriInfo;

    @PostConstruct
    public void init() {
        ConfigurableWebApplicationContext applicationContext =
                (ConfigurableWebApplicationContext)
                        WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        Map deployments = (Map) servletContext.getAttribute("resteasy.deployments");
        String path = uriInfo.getBaseUri().getPath();
        String contextPath = servletContext.getContextPath();
        path = path.substring(contextPath.length());
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        String[] segments = path.split("/");
        SpringBeanProcessor processor = new SpringBeanProcessor((ResteasyDeployment) deployments.get("/" + segments[0]));
        applicationContext.addBeanFactoryPostProcessor(processor);
        applicationContext.addApplicationListener(processor);
    }

}
