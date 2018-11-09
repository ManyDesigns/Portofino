package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.dispatcher.resolvers.CachingResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.JacksonResourceResolver;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class TestJacksonResourceResolver {

    @Test
    public void smokeTest() throws Exception {
        FileObject root = getRoot();
        assertTrue(root.exists());
        assertTrue(root.resolveFile("test/person.json").exists());
    }

    public FileObject getRoot() throws FileSystemException {
        return VFS.getManager().resolveFile("res:json");
    }

    @Test
    public void person() throws Exception {
        FileObject root = getRoot();
        ResourceResolver resourceResolver = new CachingResourceResolver(new JacksonResourceResolver());

        Map personMap = resourceResolver.resolve(root.resolveFile("test/person.json"), Map.class);
        assertEquals("person", personMap.get("title"));
        
        Entity personEntity = resourceResolver.resolve(root.resolveFile("test/person.json"), Entity.class);
        assertEquals("person", personEntity.title);

        personMap = resourceResolver.resolve(root.resolveFile("test"), "person", Map.class);
        assertEquals("person", personMap.get("title"));

        personEntity = resourceResolver.resolve(root.resolveFile("test"), "person", Entity.class);
        assertEquals("person", personEntity.title);
    }
    
    public static class Entity {
        public String title;
        public String namespace;
        public List annotations;
        public Map properties;
    }

}
