package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.dispatcher.resolvers.CachingResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.JacksonResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.JavaResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.ResourceResolvers;
import com.manydesigns.portofino.dispatcher.security.RealmWrapper;
import com.manydesigns.portofino.dispatcher.security.ShiroResourceFilter;
import org.apache.commons.vfs2.FileObject;
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
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertTrue;

public class TestRESTInMemory extends JerseyTestNg.ContainerPerClassTest {

    private static final Logger logger = LoggerFactory.getLogger(TestRESTInMemory.class);

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(TestResource.class).register(JacksonFeature.class).register(ShiroResourceFilter.class);
    }
    
    @Override
    protected TestContainerFactory getTestContainerFactory() {
        //Must be in-memory so that REST API calls are handled in the test thread to propagate Shiro subject state
        return new InMemoryTestContainerFactory();
    }

    @Path("test")
    public static class TestResource {
        public final ResourceResolvers resourceResolver = new ResourceResolvers();
        public final Resource root;
        
        @Context
        protected ResourceContext resourceContext;

        public TestResource() throws Exception {
            resourceResolver.resourceResolvers.add(new JavaResourceResolver());
            resourceResolver.resourceResolvers.add(new CachingResourceResolver(new JacksonResourceResolver()));
            FileObject javaRoot = VFS.getManager().resolveFile("res:java-sources");
            assertTrue(javaRoot.exists());
            root = Root.get(javaRoot, resourceResolver);
        }
        
        @PostConstruct
        public void init() {
            root.setResourceContext(resourceContext);
            root.init();
        }

        @GET
        public String getHello() {
            return "Hello World!";
        }

        @Path("{pathSegment}")
        public Object start(@PathParam("pathSegment") String pathSegment) throws Exception {
            return root.consumePathSegment(pathSegment);
        }
    }

    @Test
    public void smokeTest() {
        String hello = target("test").request().get(String.class);
        assertEquals("Hello World!", hello);
    }

    @Test
    public void resource() {
        String result = target("test/b/1").request().get(String.class);
        assertEquals("GET", result);
        result = target("test/b/1").request().post(null).readEntity(String.class);
        assertEquals("POST", result);
        result = target("test/b/2").request().get(String.class);
        assertEquals("2", result);
    }
    
    @Test
    public void parameters() {
        try {
            String result = target("test/p").request().get(String.class);
            fail("Should have thrown, not return " + result);
        } catch (Exception e) {
            logger.debug("Thrown exception", e);
        }
        String result = target("test/p/1").request().get(String.class);
        assertEquals(result, "[\"1\"]");
        result = target("test/p/1/2").request().get(String.class);
        assertEquals(result, "[\"1\",\"2\"]");
        try {
            target("test/p/1/2/3").request().get(String.class);
            fail("Should have thrown");
        } catch (Exception e) {
            logger.debug("Thrown exception", e);
        }
    }
    
    @Test
    public void description() {
        Map result = target("test/p/1/2/:description").request().get(Map.class);
        assertEquals(result.get("path"), "p/1/2/");
        assertTrue(((List) result.get("children")).contains("sub"));
        assertEquals(result.get("superclass"), AbstractResourceWithParameters.class.getName());
        assertEquals(result.get("class"), "Params");
    }

    @Test
    public void secure1NoWrapper() {
        IniSecurityManagerFactory iniSecurityManagerFactory = new IniSecurityManagerFactory("classpath:test/shiro.ini");
        RealmSecurityManager securityManager = (RealmSecurityManager) iniSecurityManagerFactory.createInstance();
        SecurityUtils.setSecurityManager(securityManager);
        DelegatingSubject subject = new DelegatingSubject(securityManager);
        SubjectThreadState subjectThreadState = new SubjectThreadState(subject);
        subjectThreadState.bind();

        String result;
        //Test not authenticated subject
        assertFalse(subject.isAuthenticated());
        result = target("test/b/secure1").request().get(String.class);
        assertEquals(result, "secure");
        try {
            target("test/b/1/secure1").request().get(String.class);
            fail("Should have thrown NotAuthorizedException");
        } catch (NotAuthorizedException e) {
            //Ok
        }

        //Test authenticated subject w/o RolesPermission and w/o any wildcard permission
        subject.login(new UsernamePasswordToken("me", "me"));
        assertTrue(subject.isAuthenticated());
        try {
            target("test/b/secure1").request().get(String.class);
            fail("Should have thrown NotAuthorizedException");
        } catch (ForbiddenException e) {
            //Ok
        }
        try {
            target("test/b/1/secure1").request().get(String.class);
            fail("Should have thrown NotAuthorizedException");
        } catch (ForbiddenException e) {
            //Ok
        }
        subject.logout();

        //Test authenticated subject w/o RolesPermission and with "secure1" wildcard permission
        subject.login(new UsernamePasswordToken("you", "you"));
        assertTrue(subject.isAuthenticated());
        result = target("test/b/secure1").request().get(String.class);
        assertEquals(result, "secure");
        result = target("test/b/1/secure1").request().get(String.class);
        assertEquals(result, "secure");
        subject.logout();

        subjectThreadState.clear();
    }
    
    @Test
    public void secure1Wrapper() {
        IniSecurityManagerFactory iniSecurityManagerFactory = new IniSecurityManagerFactory("classpath:test/shiro.ini");
        RealmSecurityManager securityManager = (RealmSecurityManager) iniSecurityManagerFactory.createInstance();
        RealmWrapper realm = new RealmWrapper((AuthorizingRealm) securityManager.getRealms().iterator().next());
        securityManager.setRealm(realm);
        SecurityUtils.setSecurityManager(securityManager);
        DelegatingSubject subject = new DelegatingSubject(securityManager);
        SubjectThreadState subjectThreadState = new SubjectThreadState(subject);
        subjectThreadState.bind();

        String result;
        //Test not authenticated subject
        assertFalse(subject.isAuthenticated());
        result = target("test/b/secure1").request().get(String.class);
        assertEquals(result, "secure");
        try {
            target("test/b/1/secure1").request().get(String.class);
            fail("Should have thrown NotAuthorizedException");
        } catch (NotAuthorizedException e) {
            //Ok
        }

        //Test authenticated subject with RolesPermission
        subject.login(new UsernamePasswordToken("me", "me"));
        assertTrue(subject.isAuthenticated());
        result = target("test/b/secure1").request().get(String.class);
        assertEquals(result, "secure");
        try {
            target("test/b/1/secure1").request().get(String.class);
            fail("Should have thrown NotAuthorizedException");
        } catch (ForbiddenException e) {
            //Ok
        }
        subject.logout();

        //Test authenticated subject with RolesPermission and with "secure1" wildcard permission
        subject.login(new UsernamePasswordToken("you", "you"));
        assertTrue(subject.isAuthenticated());
        result = target("test/b/secure1").request().get(String.class);
        assertEquals(result, "secure");
        result = target("test/b/1/secure1").request().get(String.class);
        assertEquals(result, "secure");
        subject.logout();

        subjectThreadState.clear();
    }

    @Test
    public void mountPoint() {
        String result = target("test/p/someParam/alias/2").request().get(String.class);
        assertEquals("2", result);

        result = target("test/p/someParam/alias/1").request().get(String.class);
        assertEquals("GET", result);
    }

}
