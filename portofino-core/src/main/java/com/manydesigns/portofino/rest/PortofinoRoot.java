package com.manydesigns.portofino.rest;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.di.Injections;
import com.manydesigns.portofino.dispatcher.*;
import com.manydesigns.portofino.i18n.TextProviderBean;
import com.manydesigns.portofino.modules.PageactionsModule;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.shiro.SecurityUtilsBean;
import ognl.OgnlContext;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.vfs2.FileObject;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;

public class PortofinoRoot extends Root {

    @Context
    protected ServletContext servletContext;

    @Context
    protected HttpServletResponse response;

    @Context
    protected UriInfo uriInfo;

    protected PageActionContext context;
    protected PageInstance pageInstance;

    protected PortofinoRoot(FileObject location, ResourceResolver resourceResolver) {
        super(location, resourceResolver);
    }

    public static PortofinoRoot get(FileObject location, ResourceResolver resourceResolver) throws Exception {
        Root root = Root.get(location, resourceResolver);
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

        Object page = super.consumePathSegment(pathSegment);
        if(page instanceof PageAction) {
            PageAction pageAction = (PageAction) page;
            pageAction.setContext(context);
            File pagesDirectory = (File) servletContext.getAttribute(PageactionsModule.PAGES_DIRECTORY);
            File pageDirectory = new File(pagesDirectory, pathSegment);
            Page subpage = DispatcherLogic.getPage(pageDirectory);
            PageInstance pageInstance = new PageInstance(
                    getPageInstance(), pageDirectory, subpage, (Class<? extends PageAction>) page.getClass());
            pageInstance.setActionBean(pageAction);
            DispatcherLogic.configurePageAction(pageAction, pageInstance);
        }
        Injections.inject(page, servletContext, request);
        return page;
    }

    //@Override
    public Response preparePage() {
        return null;
    }

    @Override
    public void init() {
        super.init();
        File pagesDirectory = (File) servletContext.getAttribute(PageactionsModule.PAGES_DIRECTORY);
        Page rootPage = DispatcherLogic.getPage(pagesDirectory);
        PageInstance pageInstance = new PageInstance(null, pagesDirectory, rootPage, null);
        setPageInstance(pageInstance);
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        PageActionContext context = new PageActionContext();
        context.setServletContext(servletContext);
        context.setRequest(request);
        context.setResponse(response);
        context.setActionPath("/");
        setContext(context);
    }

    //@Override
    public PageActionContext getContext() {
        return context;
    }

    //@Override
    public void setContext(PageActionContext context) {
        this.context = context;
    }

    //@Override
    public PageInstance getPageInstance() {
        return pageInstance;
    }

    //@Override
    public void setPageInstance(PageInstance pageInstance) {
        this.pageInstance = pageInstance;
    }

    //@Override
    public DispatchElement consumePathFragment(String pathFragment) {
        return (DispatchElement) consumePathSegment(pathFragment);
    }
}
