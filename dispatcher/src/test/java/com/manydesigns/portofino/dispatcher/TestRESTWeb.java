package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.dispatcher.security.ShiroResourceFilter;
import com.manydesigns.portofino.dispatcher.web.ApplicationRoot;
import com.manydesigns.portofino.dispatcher.web.WebDispatcherInitializer;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertTrue;

public class TestRESTWeb extends JerseyTestNg.ContainerPerClassTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(ApplicationRoot.class).register(JacksonFeature.class).register(ShiroResourceFilter.class);
    }
    
    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        try {
            String applicationRoot = VFS.getManager().resolveFile("res:java-sources").getParent().getURL().toString();
            return ServletDeploymentContext.forServlet(new ServletContainer(configure())).
                    contextParam("portofino.application.directory", applicationRoot).
                    addListener(WebDispatcherInitializer.class).
                    build();
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void resource() {
        String result = target("b/1").request().get(String.class);
        assertEquals("GET", result);
        result = target("b/1").request().post(null).readEntity(String.class);
        assertEquals("POST", result);
        result = target("b/2").request().get(String.class);
        assertEquals("2", result);
    }

}
