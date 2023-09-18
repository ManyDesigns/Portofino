package com.manydesigns.portofino.shiro;

import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.resourceactions.Permissions;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.SecurityFacade;
import com.manydesigns.portofino.security.SecurityLogic;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.aop.AnnotationsAuthorizingMethodInterceptor;
import org.apache.shiro.subject.Subject;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static com.manydesigns.portofino.security.SecurityLogic.*;

public class ShiroSecurity extends SecurityFacade {

    public static final SecurityUtilsBean SECURITY_UTILS_BEAN = new SecurityUtilsBean();

    @Override
    public boolean hasPermissions(Configuration conf, Permissions configuration, AccessLevel level, String... permissions) {
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipal();
        if(principal != null) {
            if(isAdministrator(conf)) {
                return true;
            }
            ActionPermission actionPermission = new ActionPermission(configuration, level, permissions);
            return subject.isPermitted(actionPermission);
        } else {
            //Shiro does not check permissions for non authenticated users
            return hasAnonymousPermissions(conf, configuration, level, permissions);
        }
    }

    public static boolean hasAnonymousPermissions
            (Configuration conf, Permissions configuration, AccessLevel level, String... permissions) {
        ActionPermission actionPermission = new ActionPermission(configuration, level, permissions);
        List<String> groups = new ArrayList<>();
        groups.add(getAllGroup(conf));
        groups.add(getAnonymousGroup(conf));
        return new GroupPermission(groups).implies(actionPermission);
    }

    @Override
    public boolean isAdministrator(Configuration conf) {
        String administratorsGroup = getAdministratorsGroup(conf);
        Subject subject = SecurityUtils.getSubject();
        return subject.isAuthenticated() && subject.hasRole(administratorsGroup);
    }

    @Override
    public Object getSecurityUtilsBean() {
        return SECURITY_UTILS_BEAN;
    }

    @Override
    public Object getUserId() {
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipal();
        if (principal == null) {
            logger.debug("No user found");
        } else {
            try {
                return ShiroUtils.getUserId(subject);
            } catch (Exception e) {
                logger.warn("Could not retrieve user id. This usually happens if Security.groovy has been changed in an incompatible way.", e);
            }
        }
        return null;
    }

    @Override
    public Set<String> getGroups() {
        return ShiroUtils.getPortofinoRealm().getGroups();
    }

    @Override
    public Map getUsers() {
        return ShiroUtils.getPortofinoRealm().getUsers();
    }

    @Override
    public void setup(
            FileObject appDirectory, ConfigurationSource configuration, ModelService modelService,
            String adminGroupName, String encryptionAlgorithm
    ) throws IOException {
        Configuration properties = configuration.getProperties();
        SecurityLogic.setAdministratorsGroup(properties, adminGroupName);
        String[] algoAndEncoding = encryptionAlgorithm.split(":");
        properties.setProperty(AbstractPortofinoRealm.HASH_ALGORITHM, algoAndEncoding[0]);
        properties.setProperty(AbstractPortofinoRealm.HASH_FORMAT, algoAndEncoding[1]);
        try {
            configuration.save();
        } catch (ConfigurationException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isUserAuthenticated() {
        return SecurityUtils.getSubject().isAuthenticated();
    }

    @Override
    public void checkWebResourceIsAccessible(ContainerRequestContext requestContext, Object resource, Method handler) {
        try {
            AUTH_CHECKER.assertAuthorized(resource, handler);
            logger.debug("Standard Shiro security check passed.");
        } catch (UnauthenticatedException e) {
            logger.debug("Method required authentication", e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        } catch (AuthorizationException e) {
            logger.warn("Method invocation not authorized", e);
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    public static final class AuthChecker extends AnnotationsAuthorizingMethodInterceptor {

        public void assertAuthorized(final Object resource, final Method handler) throws AuthorizationException {
            super.assertAuthorized(new MethodInvocation() {
                @Override
                public Object proceed() {
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
}
