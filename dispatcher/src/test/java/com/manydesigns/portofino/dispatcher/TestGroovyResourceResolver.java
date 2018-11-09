package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.dispatcher.resolvers.GroovyResourceResolver;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.AssertJUnit.*;

public class TestGroovyResourceResolver extends TestJavaResourceResolver {

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

    @Override
    protected ResourceResolver getResourceResolver() {
        return new GroovyResourceResolver();
    }

    @Override
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

    @Override
    public void testCodebase() throws Exception {
        //pass
    }
}
