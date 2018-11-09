package com.manydesigns.portofino.dispatcher;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.VirtualFileSystem;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class TestVirtualFileSystem extends TestJavaResourceResolver {

    @Override
    public FileObject getRoot() throws FileSystemException {
        FileObject root = super.getRoot();
        VirtualFileSystem vfs = (VirtualFileSystem) VFS.getManager().createVirtualFileSystem((String) null).getFileSystem();
        FileObject junction = VFS.getManager().resolveFile("res:java-sources/p");
        vfs.addJunction("/q", junction);
        vfs.addJunction("/p/sub", junction.getChild("sub"));
        vfs.addJunction("/p", junction);
        vfs.addJunction("/b/bus", junction.getChild("sub"));
        FileObject b = root.getChild("b");
        for(FileObject child : b.getChildren()) {
            vfs.addJunction("/b/" + child.getName().getBaseName(), child);
        }
        vfs.addJunction("/", root);
        return vfs.getRoot();
    }

    @Test
    public void testJunction() throws Exception {
        FileObject root = getRoot();
        ResourceResolver javaResourceResolver = getResourceResolver();

        FileObject q = root.resolveFile("q");
        assertEquals(root, q.getParent());
        Class<?> c = javaResourceResolver.resolve(q, Class.class);
        assertEquals("Params", c.getName());
        FileObject sub = q.getChild("sub");
        assertEquals(q, sub.getParent());
        c = javaResourceResolver.resolve(sub, Class.class);
        assertEquals("Sub", c.getName());

        FileObject b = root.resolveFile("b");
        assertEquals(root, b.getParent());
        c = javaResourceResolver.resolve(b, Class.class);
        assertEquals("B", c.getName());
        sub = b.getChild("1");
        assertEquals(b, sub.getParent());
        c = javaResourceResolver.resolve(sub, Class.class);
        assertEquals("REST", c.getName());
        sub = b.getChild("bus");
        assertEquals(b, sub.getParent());
        //Unfortunately, this does not currently work
        //c = javaResourceResolver.resolve(sub, Class.class);
        //assertEquals("Sub", c.getName());
    }

}
