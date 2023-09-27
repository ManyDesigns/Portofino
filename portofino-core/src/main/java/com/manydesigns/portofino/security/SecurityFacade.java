package com.manydesigns.portofino.security;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.resourceactions.Permissions;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.operations.Operation;
import com.manydesigns.portofino.resourceactions.ActionInstance;
import com.manydesigns.portofino.resourceactions.ResourceAction;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.FileObject;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static com.manydesigns.portofino.security.SecurityLogic.*;

public abstract class SecurityFacade {

    public abstract boolean hasPermissions(Configuration conf, Permissions configuration, AccessLevel accessLevel, String... permissions);

    public boolean hasPermissions(Configuration configuration, Permissions permissions, RequiresPermissions requiresPermissions) {
        return hasPermissions(configuration, permissions, requiresPermissions.level(), requiresPermissions.permissions());
    }

    public boolean hasPermissions
            (Configuration conf, ActionInstance instance, AccessLevel accessLevel, String... permissions) {
        Permissions configuration = calculateActualPermissions(instance);
        return hasPermissions(conf, configuration, accessLevel, permissions);
    }

    public boolean hasPermissions
            (Configuration conf, Permissions permissions, Method handler, Class<?> theClass) {
        logger.debug("Checking action permissions");
        RequiresPermissions requiresPermissions = getRequiresPermissionsAnnotation(handler, theClass);
        if(requiresPermissions != null) {
            return hasPermissions(conf, permissions, requiresPermissions);
        } else {
            return true;
        }
    }

    public boolean hasPermissions
            (Configuration conf, Method method, Class fallbackClass, ActionInstance actionInstance) {
        RequiresPermissions requiresPermissions =
                SecurityLogic.getRequiresPermissionsAnnotation(method, fallbackClass);
        if(requiresPermissions != null) {
            Permissions permissions = SecurityLogic.calculateActualPermissions(actionInstance);
            return hasPermissions(conf, permissions, requiresPermissions);
        } else {
            return true;
        }
    }

    public boolean hasPermissions(Configuration conf, ActionInstance instance, Method handler) {
        logger.debug("Checking action permissions");
        Class<?> theClass = instance.getActionClass();
        RequiresPermissions requiresPermissions = getRequiresPermissionsAnnotation(handler, theClass);
        if(requiresPermissions != null) {
            AccessLevel accessLevel = requiresPermissions.level();
            String[] permissions = requiresPermissions.permissions();
            return hasPermissions(conf, instance, accessLevel, permissions);
        }
        return true;
    }

    public boolean isOperationAllowed(ResourceAction action, Configuration configuration, HttpServletRequest request, Operation operation, Method handler) {
        boolean isAdmin = isAdministrator(request);
        return isAdmin ||
                ((action.getActionInstance() == null || hasPermissions(
                        configuration, operation.getMethod(), action.getClass(), action.getActionInstance())) &&
                        satisfiesRequiresAdministrator(action, handler, false));
    }

    public boolean isOperationAllowed(HttpServletRequest request, ActionInstance actionInstance, ResourceAction resourceAction, Method handler) {
        if (!satisfiesRequiresAdministrator(resourceAction, handler, isAdministrator(request))) {
            return false;
        }

        logger.debug("Checking actionDescriptor permissions");
        boolean isNotAdmin = !isAdministrator(request);
        if (isNotAdmin) {
            ServletContext servletContext = request.getServletContext();
            ConfigurationSource configuration =
                    (ConfigurationSource) servletContext.getAttribute(PortofinoSpringConfiguration.CONFIGURATION_SOURCE);
            Permissions permissions;
            String resource;
            boolean allowed;
            if(actionInstance != null) {
                logger.debug("The protected resource is a actionDescriptor action");
                resource = resourceAction.getPath();
                allowed = hasPermissions(configuration.getProperties(), actionInstance, handler);
            } else {
                logger.debug("The protected resource is a regular JAX-RS resource");
                resource = request.getRequestURI();
                permissions = new Permissions();
                allowed = hasPermissions(configuration.getProperties(), permissions, handler, resourceAction.getClass());
            }
            if(!allowed) {
                logger.info("Access to {} is forbidden", resource);
                return false;
            }
        }
        return true;
    }

    public boolean isAdministrator(ServletRequest request) {
        ServletContext servletContext = ElementsThreadLocals.getServletContext();
        ConfigurationSource conf =
                (ConfigurationSource) servletContext.getAttribute(PortofinoSpringConfiguration.CONFIGURATION_SOURCE);
        return isAdministrator(conf.getProperties());
    }

    public abstract boolean isAdministrator(Configuration conf);

    public abstract Object getSecurityUtilsBean();

    public abstract Object getUserId();

    /**
     * Returns the list of groups known to the system. This is used by the framework when presenting a list of
     * possible groups, e.g. when configuring permissions for a page.
     * @return the set of known groups.
     */
    public abstract Set<String> getGroups();

    public abstract void setup(FileObject appDirectory, String adminGroupName, String encryptionAlgorithm) throws IOException;

    public abstract boolean isUserAuthenticated();

    public abstract Map getUsers();

    public abstract void checkWebResourceIsAccessible(ContainerRequestContext requestContext, Object resource, Method handler);
}
