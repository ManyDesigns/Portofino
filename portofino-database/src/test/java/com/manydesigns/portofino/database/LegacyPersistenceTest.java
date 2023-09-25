package com.manydesigns.portofino.database;

import com.manydesigns.portofino.persistence.Persistence;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Test
public class LegacyPersistenceTest extends PersistenceTest {

    @BeforeMethod
    public void setup() throws Exception {
        FileObject appDir = VFS.getManager().resolveFile("res:com/manydesigns/portofino/database/model/legacy");
        setup(appDir);
    }

    protected void configure(Persistence persistence) {
        persistence.setConvertLegacyModel(false);
    }

    @Override
    @Ignore
    public void testDisabledDatabasesAreSkipped() {}
}
