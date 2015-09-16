package com.manydesigns.portofino.pageactions.rest;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.stripes.ElementsActionBeanContext;
import com.manydesigns.portofino.buttons.ButtonsLogic;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageAction;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.shiro.ShiroUtils;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.BeforeAfterMethodInterceptor;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.controller.StripesConstants;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.aop.AnnotationsAuthorizingMethodInterceptor;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Provider
@ConstrainedTo(RuntimeType.SERVER)
public class PortofinoFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(PortofinoFilter.class);

    protected final BeforeAfterMethodInterceptor beforeAfterMethodInterceptor = new BeforeAfterMethodInterceptor();

    @Context
    protected ResourceInfo resourceInfo;

    @Context
    protected HttpServletRequest request;

    @Context
    protected HttpServletResponse response;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UriInfo uriInfo = requestContext.getUriInfo();
        if(uriInfo.getMatchedResources().isEmpty()) {
            return;
        }
        Object resource = uriInfo.getMatchedResources().get(0);
        if(resourceInfo == null || resourceInfo.getResourceClass() == null) {
            return;
        }
        if(resource.getClass() != resourceInfo.getResourceClass()) {
            throw new RuntimeException("Inconsistency: matched resource is not of the right type, " + resourceInfo.getResourceClass());
        }

        fillMDC();
        checkAuthorizations(requestContext, resource);
        preparePage(requestContext, resource);
        runStripesInterceptors(requestContext, resource, true);
    }

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        UriInfo uriInfo = requestContext.getUriInfo();
        if(uriInfo.getMatchedResources().isEmpty()) {
            return;
        }
        Object resource = uriInfo.getMatchedResources().get(0);
        try {
            if(resourceInfo == null || resourceInfo.getResourceClass() == null) {
                return;
            }
        } catch (Exception e) {
            logger.debug("Could not get resourceInfo (can happen under RestEasy)", e);
            return;
        }
        if(resource.getClass() != resourceInfo.getResourceClass()) {
            throw new RuntimeException("Inconsistency: matched resource is not of the right type, " + resourceInfo.getResourceClass());
        }

        runStripesInterceptors(requestContext, resource, false);
    }

    protected void runStripesInterceptors(ContainerRequestContext requestContext, Object resource, boolean before) {
        if(resource instanceof ActionBean) {
            BridgeExecutionContext executionContext = new BridgeExecutionContext(before);
            ActionBean actionBean = (ActionBean) resource;
            executionContext.setActionBean(actionBean);
            executionContext.setActionBeanContext(actionBean.getContext());
            executionContext.setHandler(resourceInfo.getResourceMethod());
            executionContext.setLifecycleStage(LifecycleStage.EventHandling);
            try {
                Resolution resolution = beforeAfterMethodInterceptor.intercept(executionContext);
                if(resolution != null) {
                    requestContext.abortWith(Response.ok(resolution).build());
                }
            } catch (Exception e) {
                logger.error("Exception applying before/after method interceptor", e);
                requestContext.abortWith(Response.serverError().entity(e).build());
            }
        }
    }

    protected void preparePage(ContainerRequestContext requestContext, Object resource) {
        if(resource instanceof PageAction) {
            PageAction pageAction = (PageAction) resource;
            request.setAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN, pageAction);
            if(!pageAction.getPageInstance().isPrepared()) {
                ElementsActionBeanContext context = new ElementsActionBeanContext();
                context.setRequest(request);
                context.setResponse(response);
                context.setServletContext(request.getServletContext());
                context.setEventName("");
                String path = requestContext.getUriInfo().getPath();
                if(!path.startsWith("/")) {
                    path = "/" + path;
                }
                context.setActionPath(path); //TODO
                pageAction.setContext(context);
                Resolution resolution = pageAction.preparePage();
                if(resolution != null) {
                    requestContext.abortWith(Response.serverError().entity(resolution).build());
                }
            }
        }
    }

    protected void checkAuthorizations(ContainerRequestContext requestContext, Object resource) {
        try {
            Method handler = resourceInfo.getResourceMethod();
            AUTH_CHECKER.assertAuthorized(resource, handler);
            logger.debug("Standard Shiro security check passed.");
            if(resource instanceof PageAction) {
                checkActionBeanInvocation(requestContext, (PageAction) resource);
            }
        } catch (UnauthenticatedException e) {
            logger.debug("Method required authentication", e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        } catch (AuthorizationException e) {
            logger.warn("Method invocation not authorized", e);
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    protected void fillMDC() {
        logger.debug("Retrieving user");
        Serializable userId = null;
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipal();
        if (principal == null) {
            logger.debug("No user found");
        } else {
            userId = ShiroUtils.getUserId(subject);
            logger.debug("Retrieved userId={}", userId);
        }

        logger.debug("Setting up logging MDC");
        MDC.clear();
        if(userId != null) { //Issue #755
            MDC.put("userId", userId.toString());
        }
        if(request != null) {
            MDC.put("req.requestURI", request.getRequestURI());
        }
    }

    protected void checkActionBeanInvocation(ContainerRequestContext requestContext, PageAction pageAction) {
        Method handler = resourceInfo.getResourceMethod();
        List<PageInstance> pageInstancePath = new ArrayList<PageInstance>();
        PageInstance last = pageAction.getPageInstance();
        while(last != null) {
            pageInstancePath.add(0, last);
            last = last.getParent();
        }
        Dispatch dispatch = new Dispatch(pageInstancePath.toArray(new PageInstance[pageInstancePath.size()]));
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        if(!SecurityLogic.isAllowed(request, dispatch, pageAction, handler)) {
            Response.Status status =
                    SecurityUtils.getSubject().isAuthenticated() ?
                            Response.Status.FORBIDDEN :
                            Response.Status.UNAUTHORIZED;
            requestContext.abortWith(Response.status(status).build());
        } else if(!ButtonsLogic.doGuardsPass(pageAction, handler)) {
            requestContext.abortWith(
                    Response.status(Response.Status.CONFLICT)
                            .entity("The action couldn't be invoked, a guard did not pass")
                            .build());
        } else {
            logger.debug("Portofino-specific security check passed");
        }
    }

    public static final class AuthChecker extends AnnotationsAuthorizingMethodInterceptor {

        public void assertAuthorized(final Object resource, final Method handler) throws AuthorizationException {
            super.assertAuthorized(new MethodInvocation() {
                @Override
                public Object proceed() throws Throwable {
                    return null;
                }

                @Override
                public Method getMethod() {
                    return handler;
                }

                @Override
                public Object[] getArguments() {
                    return new Object[0];
                }

                @Override
                public Object getThis() {
                    return resource;
                }
            });
        }
    }

    protected static final AuthChecker AUTH_CHECKER = new AuthChecker();

    public static final class BridgeExecutionContext extends ExecutionContext {

        private final boolean before;
        private boolean proceedCalled;

        public BridgeExecutionContext(boolean before) {
            this.before = before;
        }

        @Override
        public Resolution proceed() throws Exception {
            proceedCalled = true;
            return null;
        }

        @Override
        public ActionBean getActionBean() {
            //BeforeAfterMethodInterceptor conditionalizes on context.getActionBean() != null, so trick it into
            //not executing @Before/@After methods in the appropriate phase
            if((before && !proceedCalled) || (!before && proceedCalled)) {
                return super.getActionBean();
            } else {
                return null;
            }
        }
    }
}
