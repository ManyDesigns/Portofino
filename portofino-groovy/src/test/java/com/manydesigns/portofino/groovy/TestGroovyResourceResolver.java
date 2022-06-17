package com.manydesigns.portofino.groovy;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Field;

import static org.testng.AssertJUnit.*;

public class TestGroovyResourceResolver {

    @Test
    public void smokeTest() throws Exception {
        FileObject root = getRoot();
        assertTrue(root.exists());
        assertTrue(root.resolveFile("A.groovy").exists());
    }

    public FileObject getRoot() throws FileSystemException {
        FileObject groovyRoot = VFS.getManager().resolveFile("res:groovy-sources");
        assertTrue(groovyRoot.exists());
        return groovyRoot;
    }

    protected ResourceResolver getResourceResolver() {
        return new GroovyResourceResolver();
    }

    @Test
    public void reload() throws Exception {
        FileObject root = getRoot();

        ResourceResolver resourceResolver = getResourceResolver();
        Class<?> c1 = resourceResolver.resolve(root, Class.class);
        assertNotNull(c1);
        Class<?> c2 = resourceResolver.resolve(root, Class.class);
        assertEquals(c1, c2);
        Thread.sleep(1000);

        //Cheat - we know it is a regular file underneath
        File file = new File(root.resolveFile("A.groovy").getURL().getFile());
        assertTrue(file.setLastModified(System.currentTimeMillis()));
        Class<?> c3 = resourceResolver.resolve(root, Class.class);
        assertFalse(c1.equals(c3));
    }

    @Test
    public void simpleScenario() throws Exception {
        FileObject root = getRoot();
        ResourceResolver javaResourceResolver = getResourceResolver();

        Class<?> c = javaResourceResolver.resolve(root, Class.class); //A.java
        assertEquals("A", c.getName());
        Field string = c.getField("string");
        Object a = c.getConstructor().newInstance();
        assertEquals("class A", string.get(a));

        c = javaResourceResolver.resolve(root.resolveFile("b"), Class.class); //B.java
        assertEquals("B", c.getName());
        string = c.getField("string");
        Object b = c.getConstructor().newInstance();
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

    protected ResourceResolver getResourceResolver(CodeBase codeBase) {
        return new GroovyResourceResolver(codeBase);
    }

    @Test
    public void error() throws Exception {
        FileObject root = getRoot();
        try {
            getResourceResolver().resolve(root.resolveFile("d.error"), Class.class); //A.java
            fail("Should have thrown");
        } catch (Exception e) {
            // Pass
        }
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
