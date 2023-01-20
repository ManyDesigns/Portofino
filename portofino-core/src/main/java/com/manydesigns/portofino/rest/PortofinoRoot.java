/*
 * Copyright (C) 2005-2023 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.rest;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.dispatcher.Resource;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import com.manydesigns.portofino.dispatcher.Root;
import com.manydesigns.portofino.i18n.TextProviderBean;
import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.resourceactions.ActionContext;
import com.manydesigns.portofino.resourceactions.ActionInstance;
import com.manydesigns.portofino.resourceactions.ResourceAction;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import ognl.OgnlContext;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The root of the application's REST APIs managed by Portofino.
 */
public class PortofinoRoot extends AbstractResourceAction {

    private static final Logger logger = LoggerFactory.getLogger(PortofinoRoot.class);

    @Context
    public ServletContext servletContext;
    @Context
    public HttpServletResponse response;
    @Context
    public HttpServletRequest request;

    protected ResourceResolver resourceResolver;

    protected PortofinoRoot(FileObject location, ResourceResolver resourceResolver) {
        setLocation(location);
        this.resourceResolver = resourceResolver;
    }

    public static PortofinoRoot get(FileObject location, ResourceResolver resourceResolver) throws Exception {
        Resource root = Root.get(location, resourceResolver);
        if (!(root instanceof PortofinoRoot)) {
            if(!root.getClass().equals(Root.class)) {
                logger.warn(root + " defined in " + location + " does not extend PortofinoRoot, ignoring");
            }
            root = new PortofinoRoot(location, resourceResolver);
        }
        return (PortofinoRoot) root;
    }

    @Override
    @Path("{pathSegment}")
    public Object consumePathSegment(@PathParam("pathSegment") String pathSegment) {
        logger.debug("Publishing securityUtils in OGNL context");
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        ognlContext.put("securityUtils", security.getSecurityUtilsBean());
        logger.debug("Publishing textProvider in OGNL context");
        ognlContext.put("textProvider", new TextProviderBean(ElementsThreadLocals.getTextProvider()));
        return super.consumePathSegment(pathSegment);
    }

    @Override
    public PortofinoRoot init() {
        if (applicationContext == null) {
            applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            if (applicationContext != null) {
                autowire(this);
            }
        }
        ActionInstance actionInstance = new ActionInstance(null, location, getClass());
        setActionInstance(actionInstance);
        ActionContext context = new ActionContext();
        context.setServletContext(servletContext);
        context.setRequest(request);
        context.setResponse(response);
        context.setActionPath("/");
        try {
            loadConfiguration();
        } catch (Exception e) {
            throw new RuntimeException("Initialization failed", e);
        }
        setContext(context);
        return this;
    }

    @Override
    public ResourceAction getParent() {
        return null;
    }

    /**
     * Returns a description of the root.
     * @since 5.0
     * @return the action's description as JSON.
     */
    @Path(":description")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @RequiresPermissions(level = AccessLevel.NONE)
    public Map<String, Object> getJSONDescription() {
        Map<String, Object> description = new HashMap<>();
        description.put("superclass", getClass().getSuperclass().getName());
        description.put("class", getClass().getName());
        description.put("path", getPath());
        description.put("children", getSubResources());
        description.put("loginPath", "/:auth"); //For legacy clients
        return description;
    }

    @Override
    public Domain getConfigurationDomain() {
        return actionsDomain;
    }

    @Override
    public void setParent(Resource parent) {
        throw new UnsupportedOperationException("Cannot set the parent of the root");
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response openAPIJSON() throws URISyntaxException {
        return Response.temporaryRedirect(new URI("openapi.json")).build();
    }

    @GET
    @Produces("application/yaml")
    public Response openAPIYAML() throws URISyntaxException {
        return Response.temporaryRedirect(new URI("openapi.yaml")).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response welcomePage() throws IOException {
        URL resource = getClass().getResource("/com/manydesigns/portofino/actions/welcome.en.html");
        String welcomePage = IOUtils.toString(resource, StandardCharsets.UTF_8);
        return Response.ok(welcomePage).build();
    }

}
