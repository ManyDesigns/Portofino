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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.dispatcher.security.ResourcePermissions;
import com.manydesigns.portofino.dispatcher.security.SecureResource;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class AbstractResource implements SecureResource {
    
    protected static final Logger logger = LoggerFactory.getLogger(AbstractResource.class);
    
    protected FileObject location;
    protected Resource parent;
    protected String segment;

    @Context
    protected ResourceContext resourceContext;

    @Override
    public FileObject getLocation() {
        return location;
    }

    @Override
    public void setLocation(FileObject location) {
        this.location = location;
    }

    @Override
    public ResourceResolver getResourceResolver() {
        return getParent().getResourceResolver();
    }

    @Override
    public Resource getParent() {
        return parent;
    }

    @Override
    public void setParent(Resource parent) {
        this.parent = parent;
    }

    public String getPath() {
        return parent.getPath() + getSegment() + "/";
    }

    public void setResourceContext(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

    /**
     * Visits a path segment
     * @param pathSegment the path segment
     * @return a sub resource (which can possibly continue dispatch if it is itself an Resource) or null.
     */
    @Path("{pathSegment}")
    public Object consumePathSegment(@PathParam("pathSegment") String pathSegment) {
        try {
            FileObject child = getChildLocation(pathSegment);
            return consumePathSegment(pathSegment, child, getResourceResolver());
        } catch (FileSystemException e) {
            logger.error("Could not access child: " + pathSegment, e);
            throw new WebApplicationException(404);
        }
    }

    protected FileObject getChildLocation(String pathSegment) throws FileSystemException {
        FileObject child = getChildrenLocation().getChild(pathSegment);
        checkChildLocation(pathSegment, child);
        return child;
    }

    protected void checkChildLocation(String pathSegment, FileObject child) throws FileSystemException {
        if(child == null) {
            return;
        }
        if(!child.getParent().equals(getChildrenLocation())) {
            throw new IllegalArgumentException(
                    "Path segment " + pathSegment + " results in a path that is not a child of the current node and " +
                    "for security reasons this is forbidden.");
        }
    }

    protected Object consumePathSegment(String pathSegment, FileObject resourceLocation, ResourceResolver resourceResolver) {
        if(resourceLocation != null) {
            Object subResource;
            try {
                subResource = getSubResource(resourceLocation, pathSegment, resourceResolver);
            } catch (Exception e) {
                logger.error("Could not resolve sub resource", e);
                throw new WebApplicationException(500);
            }
            if(subResource == null) {
                throw new WebApplicationException(404);
            }
            return subResource;
        } else {
            logger.debug("Child file not found: {} {}", location, pathSegment);
            throw new WebApplicationException(404);
        }
    }
    
    public Object getSubResource(String subResourceName) throws Exception {
        FileObject subResourceLocation = getChildLocation(subResourceName);
        return getSubResource(subResourceLocation, subResourceName, getResourceResolver());
    }
    
    public Object getSubResource(FileObject resourceLocation, String segment, ResourceResolver resourceResolver) throws Exception {
        Class<?> subResourceClass = resourceResolver.resolve(resourceLocation, Class.class);
        if(subResourceClass == null) {
            logger.debug("Subresource could not be resolved");
            return null;
        } else {
            return createSubResource(subResourceClass, resourceLocation, segment);
        }
    }

    protected Object createSubResource(
            Class<?> subResourceClass, FileObject location, String segment)
            throws IllegalAccessException, InstantiationException {
        return initSubResource(subResourceClass.newInstance(), location, segment);
    }

    protected Object initSubResource(Object resource, FileObject location, String segment) {
        if(resource instanceof Resource) {
            Resource subResource = (Resource) resource;
            subResource.setParent(this);
            subResource.setLocation(location);
            subResource.setSegment(segment);
            initGenericResource(resource);
            initSubResource(subResource);
            return subResource.init();
        } else {
            initGenericResource(resource);
            return resource;
        }
    }

    protected void initSubResource(Resource resource) {
        initSubResource((Object) resource);
    }

    protected void initSubResource(Object resource) {
        //By default, do nothing
    }

    protected void initGenericResource(Object resource) {
        resourceContext.initResource(resource);
    }

    public Map<String, Object> describe() {
        Map<String, Object> description = new HashMap<>();
        description.put("superclass", getClass().getSuperclass().getName());
        description.put("class", getClass().getName());
        description.put("path", getPath());
        description.put("children", getSubResources());
        describe(description);
        return description;
    }

    /**
     * Customize the description of this node. The default implementation does nothing.
     * @param description the default description.
     */
    protected void describe(Map<String, Object> description) {
        //The default implementation does nothing
    }
    
    @Path(":description")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Map<String, Object> getJSONDescription() {
        return describe();
    }

    /**
     * Returns the directory where this resource's children resources are defined.
     * @since 5.0.0-SNAPSHOT
     */
    public FileObject getChildrenLocation() throws FileSystemException {
        return location;
    }

    @Override
    public Collection<String> getSubResources() {
        try {
            List<String> subResources = new ArrayList<>();
            FileObject[] children = getChildrenLocation().getChildren();
            for (FileObject child : children) {
                if (child.getType() == FileType.FOLDER) {
                    subResources.add(child.getName().getBaseName());
                }
            }
            return subResources;
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    @Override
    public ResourcePermissions getPermissions() {
        try {
            Map permissions = getResourceResolver().resolve(location, "permissions", Map.class);
            if(permissions != null) {
                Map<String, List<String>> allow = (Map<String, List<String>>) permissions.get("allow");
                Map<String, List<String>> deny = (Map<String, List<String>>) permissions.get("deny");
                return new ResourcePermissions(this, translatePermissions(allow), translatePermissions(deny));
            } else {
                return getEmptyResourcePermissions();
            }
        } catch (Exception e) {
            logger.error("Could not load permissions for " + location, e);
            return getEmptyResourcePermissions();
        }
    }

    protected Map<String, List<Permission>> translatePermissions(Map<String, List<String>> stringMap) {
        if(stringMap == null) {
            return null;
        }
        Map<String, List<Permission>> permissionMap = new HashMap<>();
        for(Map.Entry<String, List<String>> entry : stringMap.entrySet()) {
            List<Permission> permissionList = new ArrayList<>(entry.getValue().size());
            for(String perm : entry.getValue()) {
                permissionList.add(new WildcardPermission(perm));
            }
            permissionMap.put(entry.getKey(), permissionList);
        }
        return permissionMap;
    }

    protected ResourcePermissions getEmptyResourcePermissions() {
        Map<String, List<Permission>> emptyMap = Collections.emptyMap();
        return new ResourcePermissions(this, emptyMap, emptyMap);
    }

    @Override
    public Object init() {
        //The default implementation does nothing, it is only an extension hook
        return this;
    }

}
