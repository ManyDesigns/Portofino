package com.manydesigns.portofino.pageactions.rest;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.stripes.ElementsActionBeanContext;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.di.Injections;
import com.manydesigns.portofino.dispatcher.*;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.PageactionsModule;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.shiro.SecurityUtilsBean;
import ognl.OgnlContext;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.File;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Path("/")
public class APIRoot {

    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(APIRoot.class);

    public static final String PATH_PREFIX = "/api";

    @Context
    protected ServletContext servletContext;

    @Context
    protected HttpServletRequest request;

    @Context
    protected HttpServletResponse response;

    @Context
    protected UriInfo uriInfo;

    @Path("{pathFragment}")
    public DispatchElement startDispatch(@PathParam("pathFragment") String pathFragment) throws Exception {
        logger.debug("Publishing securityUtils in OGNL context");
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        ognlContext.put("securityUtils", new SecurityUtilsBean());

        String actionPath = "/" + uriInfo.getPath();
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
}
