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
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ResourceResolvers implements ResourceResolver {
    
    public static final String COPYRIGHT = "Copyright (c) 2005-2016, ManyDesigns srl";
    protected static final Logger logger = LoggerFactory.getLogger(ResourceResolvers.class);
    
    public final List<ResourceResolver> resourceResolvers = new ArrayList<>();

    @Override
    public boolean supports(Class<?> type) {
        for(ResourceResolver resourceResolver : resourceResolvers) {
            if(resourceResolver.supports(type)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean supports(String extension) {
        for(ResourceResolver resourceResolver : resourceResolvers) {
            if(resourceResolver.supports(extension)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> T resolve(FileObject location, Class<T> type) {
        for(ResourceResolver resourceResolver : resourceResolvers) {
            try {
                if(supports(resourceResolver, type, location)) {
                    T resource = resourceResolver.resolve(location, type);
                    if(resource != null) {
                        return resource;
                    }
                }
            } catch (Exception e) {
                logger.warn("Resource resolution failed (resolver: " + resourceResolver + ", resource: " + location + ")", e);
            }
        }
        return null;
    }

    @Override
    public <T> T resolve(FileObject location, String name, Class<T> type) throws Exception {
        for(ResourceResolver resourceResolver : resourceResolvers) {
            try {
                if(supports(resourceResolver, type, location)) {
                    T resource = resourceResolver.resolve(location, name, type);
                    if(resource != null) {
                        return resource;
                    }
                }
            } catch (Exception e) {
                logger.warn("Resource resolution failed (resolver: " + resourceResolver + ", resource: " + location + ")", e);
            }
        }
        return null;
    }

    protected <T> boolean supports(
            ResourceResolver resourceResolver, Class<T> type, FileObject location) throws FileSystemException {
        return resourceResolver.supports(type) &&
               (location.getType() == FileType.FOLDER || resourceResolver.supports(location.getName().getExtension()));
    }

    @Override
    public FileObject resolve(FileObject location) throws FileSystemException {
        return resolve(location, (String) null);
    }

    @Override
    public FileObject resolve(FileObject location, String name) throws FileSystemException {
        for(ResourceResolver resourceResolver : resourceResolvers) {
            try {
                FileObject resource = resourceResolver.resolve(location, name);
                if(resource != null) {
                    return resource;
                }
            } catch (FileSystemException e) {
                logger.warn("Resource resolution failed (resolver: " + resourceResolver + ", resource: " + location + ")", e);
            }
        }
        return null;
    }
}
