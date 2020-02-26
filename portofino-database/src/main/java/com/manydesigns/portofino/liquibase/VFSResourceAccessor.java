package com.manydesigns.portofino.liquibase;

import liquibase.resource.AbstractResourceAccessor;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class VFSResourceAccessor extends AbstractResourceAccessor {

    protected final FileObject base;

    public VFSResourceAccessor(FileObject base) {
        this.base = base;
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        return Collections.singleton(base.resolveFile(path).getContent().getInputStream());
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        FileObject finalDir;

        if (relativeTo == null) {
            finalDir = base.resolveFile(path);
        } else {
            finalDir = base.resolveFile(relativeTo).getParent().resolveFile(path);
        }

        if (finalDir.getType() == FileType.FOLDER) {
            Set<String> returnSet = new HashSet<>();
            getContents(finalDir, recursive, includeFiles, includeDirectories, path, returnSet);

            SortedSet<String> rootPaths = new TreeSet<>((o1, o2) -> {
                int i = -1 * Integer.compare(o1.length(), o2.length());
                if (i == 0) {
                    i = o1.compareTo(o2);
                }
                return i;
            });

            for (String rootPath : getRootPaths()) {
                if (rootPath.matches("file:/[A-Za-z]:/.*")) {
                    rootPath = rootPath.replaceFirst("file:/", "");
                } else {
                    rootPath = rootPath.replaceFirst("file:", "");
                }
                rootPaths.add(rootPath.replace("\\", "/"));
            }

            Set<String> finalReturnSet = new LinkedHashSet<>();
            for (String returnPath : returnSet) {
                returnPath = returnPath.replace("\\", "/");
                for (String rootPath : rootPaths) {
                    boolean matches;
                    if (isCaseSensitive()) {
                        matches = returnPath.startsWith(rootPath);
                    } else {
                        matches = returnPath.toLowerCase().startsWith(rootPath.toLowerCase());
                    }
                    if (matches) {
                        returnPath = returnPath.substring(rootPath.length());
                        break;
                    }
                }
                finalReturnSet.add(returnPath);
            }
            return finalReturnSet;
        }

        return null;
    }

    protected void getContents(
            FileObject root, boolean recursive, boolean includeFiles, boolean includeDirectories,
            String basePath, Set<String> returnSet) throws FileSystemException {
        FileObject[] files = root.getChildren();
        if (files == null) {
            return;
        }
        for (FileObject file : files) {
            if (file.getType() == FileType.FOLDER) {
                if (includeDirectories) {
                    returnSet.add(convertToPath(file.getName().getPath()));
                }
                if (recursive) {
                    getContents(file, true, includeFiles, includeDirectories, basePath, returnSet);
                }
            } else {
                if (includeFiles) {
                    returnSet.add(convertToPath(file.getName().getPath()));
                }
            }
        }
    }

    @Override
    public ClassLoader toClassLoader() {
        return new URLClassLoader(new URL[0]);
    }
}
