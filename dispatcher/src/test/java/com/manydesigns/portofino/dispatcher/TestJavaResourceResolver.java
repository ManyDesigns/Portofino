package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.dispatcher.resolvers.JavaResourceResolver;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.testng.AssertJUnit.*;

public class TestJavaResourceResolver {

    private static final Logger logger = LoggerFactory.getLogger(TestJavaResourceResolver.class);

    @Test
    public void smokeTest() throws Exception {
        FileObject root = getRoot();
        assertTrue(root.exists());
        assertTrue(root.resolveFile("A.java").exists());
    }

    public FileObject getRoot() throws FileSystemException {
        FileObject zipFile = VFS.getManager().resolveFile("res:java-sources.zip");
        assertTrue(zipFile.exists());
        FileObject zip = VFS.getManager().createFileSystem("zip", zipFile);
        assertTrue(zip.exists());
        return zip.resolveFile("java-sources");
    }

    @Test
    public void simpleScenario() throws Exception {
        FileObject root = getRoot();
        ResourceResolver javaResourceResolver = getResourceResolver();

        Class<?> c = javaResourceResolver.resolve(root, Class.class); //A.java
        assertEquals("A", c.getName());
        Field string = c.getField("string");
        Object a = c.newInstance();
        assertEquals("class A", string.get(a));

        c = javaResourceResolver.resolve(root.resolveFile("b"), Class.class); //B.java
        assertEquals("B", c.getName());
        string = c.getField("string");
        Object b = c.newInstance();
        assertEquals("class B", string.get(b));
    }

    @Test
    public void classWithPackage() throws Exception {
        FileObject root = getRoot();
        ResourceResolver resourceResolver = getResourceResolver();

        Class<?> c = resourceResolver.resolve(root.resolveFile("pkg1"), Class.class);
        assertEquals("pkg1.Cls", c.getName());

        c = resourceResolver.resolve(root.resolveFile("pkg2"), Class.class);
        assertEquals("some.pkg.Cls", c.getName());
    }
    
    protected ResourceResolver getResourceResolver() {
        return new JavaResourceResolver();
    }
    
    protected ResourceResolver getResourceResolver(CodeBase codeBase) {
            return new JavaResourceResolver(codeBase);
        }

    @Test
    public void error() throws Exception {
        FileObject root = getRoot();
        try {
            getResourceResolver().resolve(root.resolveFile("d.error"), Class.class); //A.java
            fail("Should have thrown");
        } catch (Exception e) {
            logger.debug("Thrown exception", e);
        }
    }

    @Test
    public void reload() throws Exception {
        FileObject root = getRoot();

        JavaResourceResolver javaResourceResolver = (JavaResourceResolver) getResourceResolver();
        Class<?> c1 = javaResourceResolver.resolve(root, Class.class);
        assertNotNull(c1);
        Class<?> c2 = javaResourceResolver.resolve(root, Class.class);
        assertEquals(c1, c2);

        javaResourceResolver.clearCache(0); //TODO ideally we should update the file's last modified time but it's not possible since for this test it's a Classloader resource
        Class<?> c3 = javaResourceResolver.resolve(root, Class.class);
        assertFalse(c1.equals(c3));
    }
    
    @Test
    public void testCodebase() throws Exception {
        ResourceResolver resourceResolver = getResourceResolver(new JavaCodeBase(VFS.getManager().resolveFile("res:java-codebase")));
        FileObject root = getRoot();

        Class<?> c = resourceResolver.resolve(root.resolveFile("codebase"), Class.class); //A.java
        assertEquals("Test", c.getName());
        Field string = c.getField("string");
        Object a = c.newInstance();
        assertEquals("it works!", string.get(a));
    }

    @Test
    public void testSubResource() throws Exception {
        FileObject root = getRoot();
        ResourceResolver javaResourceResolver = getResourceResolver();

        FileObject p = root.resolveFile("p");
        Class<?> c = javaResourceResolver.resolve(p, Class.class);
        assertEquals("Params", c.getName());
        c = javaResourceResolver.resolve(p.getChild("sub"), Class.class);
        assertEquals("Sub", c.getName());
    }

}
