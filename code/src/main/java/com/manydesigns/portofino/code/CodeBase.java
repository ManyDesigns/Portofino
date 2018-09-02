package com.manydesigns.portofino.code;

import org.apache.commons.vfs2.FileObject;

import java.io.IOException;

/**
 * Created by alessio on 28/03/17.
 */
public interface CodeBase {
    Class loadClass(String className) throws IOException, ClassNotFoundException;

    ClassLoader getClassLoader();

    FileObject getRoot();

    void close();
}
