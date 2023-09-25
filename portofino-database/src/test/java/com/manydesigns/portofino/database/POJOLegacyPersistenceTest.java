package com.manydesigns.portofino.database;

import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.hibernate.EntityMode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
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
        persistence.getDatabases().forEach(d -> {
            d.setEntityMode(EntityMode.POJO.name());
        });
        persistence.initModel();
    }

    protected void configure(Persistence persistence) {
        persistence.setConvertLegacyModel(false);
    }

    @Override
    @Ignore
    public void testDisabledDatabasesAreSkipped() {}

}
