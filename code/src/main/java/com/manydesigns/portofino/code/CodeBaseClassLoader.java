package com.manydesigns.portofino.code;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

public class CodeBaseClassLoader extends ClassLoader {
    protected final CodeBase codeBase;

    public CodeBaseClassLoader(CodeBase codeBase) {
        super(codeBase.getClassLoader());
        this.codeBase = codeBase;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return codeBase.loadClass(name);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    @Override
    protected URL findResource(String name) {
        try {
            FileObject fileObject = codeBase.getRoot().resolveFile(name);
            if(fileObject.exists()) {
                return fileObject.getURL();
            } else {
                return null;
            }
        } catch (FileSystemException e) {
            return null;
        }
    }

    @Override
    protected Enumeration<URL> findResources(String name) {
        URL resource = findResource(name);
        if(resource != null) {
            return Collections.enumeration(Collections.singletonList(resource));
        } else {
            return Collections.emptyEnumeration();
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream inputStream = super.getResourceAsStream(name);
        if(inputStream != null) {
            return inputStream;
        }
        try {
            FileObject fileObject = codeBase.getRoot().resolveFile(name);
            if(fileObject.exists()) {
                return fileObject.getContent().getInputStream();
            } else {
                return null;
            }
        } catch (FileSystemException e) {
            return null;
        }
    }
}
