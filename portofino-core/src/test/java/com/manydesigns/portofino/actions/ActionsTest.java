package com.manydesigns.portofino.actions;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;

public class ActionsTest {

    @Test
    public void testMountPath() throws FileSystemException {
        FileObject actionsDirectory = VFS.getManager().toFileObject(new File(""));
        String loginPath = actionsDirectory.getName().getPath() + "/login";
        FileObject fileObject = VFS.getManager().resolveFile(loginPath);
        assertEquals(fileObject.getName().getBaseName(), "login");
    }

}
