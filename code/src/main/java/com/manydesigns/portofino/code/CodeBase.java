package com.manydesigns.portofino.code;

import io.reactivex.subjects.Subject;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;
import java.net.URL;

/**
 * Created by alessio on 28/03/17.
 */
public interface CodeBase {
    void setParent(CodeBase parent) throws Exception;

    Class loadClass(String className, SearchScope searchScope) throws IOException, ClassNotFoundException;

    default Class loadClass(String className) throws IOException, ClassNotFoundException {
        return loadClass(className, SearchScope.LOCAL_PARENT_CLASSLOADER);
    }

    ClassLoader getClassLoader();

    FileObject getRoot();

    void close();

    default ClassLoader asClassLoader() {
        return new CodeBaseClassLoader(this);
    }

    default void clear() throws Exception {
        clear(true);
    }

    void clear(boolean recursively) throws Exception;

    CodeBase getParent();

    Subject<Class> getReloads();

    URL findResource(String name) throws IOException;

    enum SearchScope {
        LOCAL, LOCAL_PARENT, LOCAL_PARENT_CLASSLOADER;
    }
}
