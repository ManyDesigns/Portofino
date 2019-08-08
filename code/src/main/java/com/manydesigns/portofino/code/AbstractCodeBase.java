package com.manydesigns.portofino.code;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.apache.commons.vfs2.FileObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractCodeBase implements CodeBase {

    protected CodeBase parent;
    protected final ConcurrentMap<String, WeakReference<Class>> knownClasses = new ConcurrentHashMap<>();
    protected final Subject<Class> reloads = PublishSubject.create();
    protected FileObject root;
    protected ClassLoader classLoader;

    public AbstractCodeBase(FileObject root) {
        this.root = root;
    }

    public AbstractCodeBase(FileObject root, CodeBase parent, ClassLoader classLoader) {
        this(root);
        this.parent = parent;
        this.classLoader = classLoader;
        if(parent != null) {
            //noinspection ResultOfMethodCallIgnored - subscriptions are disposed on close
            parent.getReloads().subscribe(c -> {
                parentClassReloaded(c);
                reloads.onNext(c);
            });
        }
    }

    protected void parentClassReloaded(Class c) throws Exception {
        clear();
    }

    @Override
    public Class loadClass(String className, SearchScope searchScope) throws IOException, ClassNotFoundException {
        Class localClass = loadLocalClass(className);
        if(localClass != null) {
            WeakReference<Class> oldClass = knownClasses.get(className);
            boolean shouldSignalReload = oldClass != null && oldClass.get() != localClass;
            knownClasses.put(className, new WeakReference<>(localClass));
            if(shouldSignalReload) {
                reloads.onNext(localClass);
            }
            return localClass;
        }
        if(searchScope == SearchScope.LOCAL) {
            throw new ClassNotFoundException(className);
        }
        if(parent != null) {
            return parent.loadClass(className, searchScope);
        }
        if(searchScope == SearchScope.LOCAL_PARENT) {
            throw new ClassNotFoundException(className);
        }
        return getClassLoader().loadClass(className);
    }

    protected abstract Class loadLocalClass(String className) throws IOException, ClassNotFoundException;

    @Override
    public CodeBase getParent() {
        return parent;
    }

    @Override
    public Subject<Class> getReloads() {
        return reloads;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
    }

    @Override
    public void clear() throws Exception {
        if(parent != null) {
            parent.clear();
        }
    }

    @Override
    public void close() {
        try {
            if(parent != null) {
                parent.close();
            }
        } finally {
            reloads.onComplete();
        }
    }

    @Override
    public FileObject getRoot() {
        return root;
    }
}
