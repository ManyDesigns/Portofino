/*
 * Copyright (C) 2016 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.dispatcher.resolvers;

import com.manydesigns.portofino.dispatcher.ResourceResolver;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

/**
 * Created by alessio on 18/07/16.
 */
public abstract class AbstractResourceResolver implements ResourceResolver {
    
    public FileObject resolve(FileObject location) throws FileSystemException {
        return resolve(this, location);
    }
    
    public FileObject resolve(FileObject location, String name) throws FileSystemException {
        return resolve(this, location, name);
    }

    @Override
    public <T> T resolve(FileObject location, String name, Class<T> type) throws Exception {
        return resolve(resolve(location, name), type);
    }

    public static FileObject resolve(ResourceResolver resolver, FileObject location) throws FileSystemException {
        return resolve(resolver, location, null);
    }
    
    public static FileObject resolve(ResourceResolver resolver, FileObject location, String name) throws FileSystemException {
        if(location == null) {
            return null;
        }
        if(location.getType() == FileType.FILE) {
            if(isSupportedResource(resolver, location, name)) {
                return location;
            } else {
                return null;
            }
        } else {
            FileObject resource = null;
            for(FileObject candidate : location.getChildren()) {
                if(isSupportedResource(resolver, candidate, name)) {
                    if(resource == null) {
                        resource = candidate;
                    } else {
                        throw new RuntimeException("Multiple candidate resources: " + resource + ", " + candidate);
                    }
                }
            }
            return resource;
        }
    }

    public static boolean isSupportedResource(
            ResourceResolver resolver, FileObject location, String name) {
        String nameWithoutExtension = FilenameUtils.getBaseName(location.getName().getBaseName());
        return (name == null || nameWithoutExtension.equals(name)) &&
                resolver.supports(location.getName().getExtension());
    }

}
