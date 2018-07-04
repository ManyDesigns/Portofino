package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.dispatcher.resolvers.CachingResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.JacksonResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.JavaResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.ResourceResolvers;
import com.manydesigns.portofino.dispatcher.security.RealmWrapper;
import com.manydesigns.portofino.dispatcher.security.ShiroResourceFilter;
import com.manydesigns.portofino.dispatcher.web.ApplicationRoot;
import com.manydesigns.portofino.dispatcher.web.Listener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.support.DelegatingSubject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.testng.annotations.Test;

import javax.ws.rs.*;
import java.util.Arrays;
import java.util.Map;

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
                    addListener(Listener.class).
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
