package com.manydesigns.portofino.rest;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.di.Injections;
import com.manydesigns.portofino.dispatcher.*;
import com.manydesigns.portofino.i18n.TextProviderBean;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pages.PageLogic;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.shiro.SecurityUtilsBean;
import ognl.OgnlContext;
import org.apache.commons.vfs2.FileObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class PortofinoRoot extends Root implements PageAction {

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
        return super.consumePathSegment(pathSegment);
    }

    @Override
    protected void initSubResource(Object resource) {
        super.initSubResource(resource);
        //TODO use resourcecontext?
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        Injections.inject(resource, request.getServletContext(), request);
    }

    @Override
    protected void initSubResource(Resource resource) {
        super.initSubResource(resource);
        if(resource instanceof PageAction) {
            AbstractPageAction.initPageAction(resource, getPageInstance(), uriInfo);
        }
    }

    @Override
    public void init() {
        super.init();
        Page rootPage = PageLogic.getPage(location);
        PageInstance pageInstance = new PageInstance(null, location, rootPage, null);
        setPageInstance(pageInstance);
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        PageActionContext context = new PageActionContext();
        context.setServletContext(servletContext);
        context.setRequest(request);
        context.setResponse(response);
        context.setActionPath("/");
        setContext(context);
    }

    @Override
    public PageActionContext getContext() {
        return context;
    }

    @Override
    public void setContext(PageActionContext context) {
        this.context = context;
    }

    @Override
    public PageInstance getPageInstance() {
        return pageInstance;
    }

    @Override
    public void setPageInstance(PageInstance pageInstance) {
        this.pageInstance = pageInstance;
    }

    @Override
    public void prepareForExecution() {}

    @Override
    public PageAction getParent() {
        return null;
    }
}
