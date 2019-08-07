package com.manydesigns.portofino.code;

import io.reactivex.subjects.Subject;
import org.apache.commons.vfs2.FileObject;

import java.io.IOException;

/**
 * Created by alessio on 28/03/17.
 */
public interface CodeBase {
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

    void clear() throws Exception;

    CodeBase getParent();

    Subject<Class> getReloads();

    enum SearchScope {
        LOCAL, LOCAL_PARENT, LOCAL_PARENT_CLASSLOADER;
    }
}
