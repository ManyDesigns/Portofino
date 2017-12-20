/*
 * Copyright (C) 2005-2017 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.shiro.SecurityUtilsBean;
import ognl.OgnlContext;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.time.StopWatch;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.util.HashMap;
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
            "Copyright (C) 2005-2017 ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(APIRoot.class);

    public static final String PATH_PREFIX = "";

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

        PageActionContext context = new PageActionContext();
        context.setServletContext(servletContext);
        context.setRequest(request);
        context.setResponse(response);
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
    
}
