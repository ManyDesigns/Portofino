package com.manydesigns.portofino.code;

import java.io.IOException;

/**
 * Created by alessio on 28/03/17.
 */
public interface CodeBase {
    Class loadClass(String className) throws IOException, ClassNotFoundException;

    ClassLoader getClassLoader();

    void close();
}
