package com.manydesigns.portofino.liquibase;

import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.InputStreamList;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class VFSResourceAccessor extends AbstractResourceAccessor {

    protected final FileObject base;

    public VFSResourceAccessor(FileObject base) {
        this.base = base;
    }

    @Override
    public InputStreamList openStreams(String relativeTo, String path) throws IOException {
        FileObject file;

        if (relativeTo == null) {
            file = base.resolveFile(path);
        } else {
            file = base.resolveFile(relativeTo).getParent().resolveFile(path);
        }
        if(file.exists() && file.isFile()) {
            return new InputStreamList(file.getURI(), file.getContent().getInputStream());
        } else {
            return null;
        }
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        FileObject finalDir;

        if (relativeTo == null) {
            finalDir = base.resolveFile(path);
        } else {
            finalDir = base.resolveFile(relativeTo).getParent().resolveFile(path);
        }

        if (finalDir.getType() == FileType.FOLDER) {
            SortedSet<String> returnSet = new TreeSet<>();
            getContents(finalDir, recursive, includeFiles, includeDirectories, returnSet);
            return returnSet;
        }

        return null;
    }

    @Override
    public SortedSet<String> describeLocations() {
        TreeSet<String> locations = new TreeSet<>();
        locations.add(base.getPublicURIString());
        return locations;
    }

    protected void getContents(
            FileObject root, boolean recursive, boolean includeFiles, boolean includeDirectories,
            Set<String> returnSet) throws FileSystemException {
        FileObject[] files = root.getChildren();
        if (files == null) {
            return;
        }
        for (FileObject file : files) {
            if (file.getType() == FileType.FOLDER) {
                if (includeDirectories) {
                    returnSet.add(file.getName().getPath());
                }
                if (recursive) {
                    getContents(file, true, includeFiles, includeDirectories, returnSet);
                }
            } else {
                if (includeFiles) {
                    returnSet.add(file.getName().getPath());
                }
            }
        }
    }

}
