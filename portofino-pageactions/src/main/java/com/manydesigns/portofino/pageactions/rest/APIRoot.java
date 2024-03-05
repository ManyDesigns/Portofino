/*
 * Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.rest;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.stripes.ElementsActionBeanContext;
import com.manydesigns.elements.util.MimeTypes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.di.Injections;
import com.manydesigns.portofino.dispatcher.*;
import com.manydesigns.portofino.i18n.TextProviderBean;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.PageactionsModule;
import com.manydesigns.portofino.navigation.Navigation;
import com.manydesigns.portofino.navigation.NavigationItem;
import com.manydesigns.portofino.pages.ChildPage;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.shiro.SecurityUtilsBean;
import ognl.OgnlContext;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.time.StopWatch;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Path("/")
public class APIRoot {

    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(APIRoot.class);

    public static final String PATH_PREFIX = "/api";

    @Context
    protected ServletContext servletContext;

    @Context
    protected HttpServletResponse response;

    @Context
    protected UriInfo uriInfo;

    @Path("{pathFragment}")
    public DispatchElement startDispatch(@PathParam("pathFragment") String pathFragment) throws Exception {
        logger.debug("Publishing securityUtils in OGNL context");
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        ognlContext.put("securityUtils", new SecurityUtilsBean());
        logger.debug("Publishing textProvider in OGNL context");
        ognlContext.put("textProvider", new TextProviderBean(ElementsThreadLocals.getTextProvider()));
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();

        String actionPath = uriInfo.getPath();
        if(!actionPath.startsWith("/")) {
            actionPath = "/" + actionPath;
        }
        if (request.getDispatcherType() == DispatcherType.REQUEST) {
            logger.debug("Starting page response timer");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            request.setAttribute(RequestAttributes.STOP_WATCH, stopWatch);
        }

        ElementsActionBeanContext context = new ElementsActionBeanContext();
        context.setServletContext(servletContext);
        context.setRequest(request);
        context.setResponse(response);
        context.setEventName("");
        context.setActionPath(actionPath);

        Configuration configuration = (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
        File pagesDirectory = (File) servletContext.getAttribute(PageactionsModule.PAGES_DIRECTORY);
        Page rootPage = DispatcherLogic.getPage(pagesDirectory);
        PageInstance pageInstance = new PageInstance(null, pagesDirectory, rootPage, null);

        try {
            PageAction subpage = DispatcherLogic.getSubpage(configuration, pageInstance, pathFragment);
            if(subpage == null) {
                logger.error("Page not found: {}", pathFragment);
                throw new WebApplicationException(404);
            }
            subpage.setContext(context);
            Injections.inject(subpage, servletContext, request);
            return subpage;
        } catch (PageNotActiveException e) {
            logger.error("Page not active", e);
            throw new WebApplicationException(404);
        }
    }

    @Path(":pages")
    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    public NavigationItem getPages() {
        Configuration configuration = (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
        String landingPage = configuration.getString(PortofinoProperties.LANDING_PAGE);
        if(landingPage != null) {
            HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
            return new Navigation(
                    configuration, DispatcherUtil.get(request).getDispatch(landingPage), SecurityUtils.getSubject(), false).
                    getRootNavigationItem();
        } else {
            return null;
        }
    }

    /**
     * Returns a fixed description simulating AbstractPageAction#getPageDescription.
     * @since 4.2.2
     * @return the description as JSON.
     */
    @Path(":page")
    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    public Map<String, Object> getPageDescription() {
        Map<String, Object> description = new HashMap<String, Object>();
        description.put("javaClass", APIRoot.class.getName());
        description.put("groovyClass", null);
        description.put("page", new Page());
        return description;
    }

    /**
     * Returns a description of the root.
     * @since 5.0
     * @return the page's description as JSON.
     */
    @Path(":description")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @RequiresPermissions(level = AccessLevel.NONE)
    public Map<String, Object> getJSONDescription() {
        File pagesDirectory = (File) servletContext.getAttribute(PageactionsModule.PAGES_DIRECTORY);
        Page rootPage = DispatcherLogic.getPage(pagesDirectory);
        Configuration configuration = (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
        List<ChildPage> childPages = rootPage.getLayout().getChildPages();
        List<String> subResources = new ArrayList<>(childPages.size());
        for(ChildPage p : childPages) {
            subResources.add(p.getName());
        }
        Map<String, Object> description = new HashMap<String, Object>();
        description.put("superclass", getClass().getSuperclass().getName());
        description.put("class", getClass().getName());
        description.put("page", rootPage);
        description.put("path", "");
        description.put("children", subResources);
        description.put("loginPath", configuration.getString("login.page"));
        return description;
    }

    /**
     * Returns a summary of system status check.
     * @since 4.2.5
     * @return the system status as JSON.
     */
    @GET
    @Path(":status")
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    @RequiresUser
    public Response getSystemStatus() {
        Map<String, Object> systemStatus = new HashMap<String, Object>();
        int mb = 1024 * 1024;
        Map<String,Map> dirs = new HashMap<>();
        systemStatus.put("fileSystem",dirs);
        for (java.nio.file.Path root : FileSystems.getDefault().getRootDirectories()) {
            Map<String,Long> space =  new HashMap<String,Long>();
            System.out.print(root + ": ");
            dirs.put(root.toAbsolutePath().toString(),space);
            try {
                FileStore store = Files.getFileStore(root);
                space.put("available",store.getUsableSpace()/mb);
                space.put("total",store.getTotalSpace()/mb);
            } catch (IOException e) {
                logger.error("error querying space: " + e.getMessage(),e);
                return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        Runtime instance = Runtime.getRuntime();
        Map<String,Long> memory = new HashMap<>();
        systemStatus.put("memory",memory);

        memory.put("total",instance.totalMemory() / mb);
        memory.put("free",instance.freeMemory() / mb);
        memory.put("used",(instance.totalMemory() - instance.freeMemory()) / mb);
        memory.put("max",instance.maxMemory() / mb);

        systemStatus.put("unit","MB");
        systemStatus.put("javaVersion",System.getProperty("java.version"));
        systemStatus.put("os",System.getProperty("os.name"));

        //TODO check db connection status
        //TODO return error to simplify monitor bot periodic check
        //TODO set limits in configurations

        Response.ResponseBuilder responseBuilder = Response.ok(systemStatus);
        return responseBuilder.build();
    }
}
