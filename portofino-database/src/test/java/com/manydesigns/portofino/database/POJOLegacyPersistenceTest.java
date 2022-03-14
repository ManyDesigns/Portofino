package com.manydesigns.portofino.database;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.hibernate.EntityMode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Test
public class POJOLegacyPersistenceTest extends POJOPersistenceTest {

    @BeforeMethod
    @Override
    public void setup() throws Exception {
        FileObject appDir = VFS.getManager().resolveFile("res:com/manydesigns/portofino/database/model/legacy");
        setup(appDir);
        persistence.getModel().getDatabases().forEach(d -> {
            d.setEntityMode(EntityMode.POJO.getExternalName());
        });
        persistence.initModel();
    }

    @Override
    @Ignore
    public void testDisabledDatabasesAreSkipped() {}

}
