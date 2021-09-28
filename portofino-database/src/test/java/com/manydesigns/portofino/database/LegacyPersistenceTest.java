package com.manydesigns.portofino.database;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class LegacyPersistenceTest extends PersistenceTest {

    @BeforeMethod
    public void setup() throws Exception {
        FileObject appDir = VFS.getManager().resolveFile("res:com/manydesigns/portofino/database/model/legacy");
        setup(appDir);
    }

}
