/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.logic;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.pages.Permissions;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.shiro.GroupPermission;
import com.manydesigns.portofino.shiro.PagePermission;
import net.sourceforge.stripes.action.ActionBean;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SecurityLogic {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Default groups
    //**************************************************************************

    public static final String GROUP_ALL = "group.all";
    public static final String GROUP_ANONYMOUS = "group.anonymous";
    public static final String GROUP_REGISTERED = "group.registered";
    public static final String GROUP_ADMINISTRATORS = "group.administrators";
    public static final String GROUP_ALL_DEFAULT = "all";
    public static final String GROUP_ANONYMOUS_DEFAULT = "anonymous";
    public static final String GROUP_REGISTERED_DEFAULT = "registered";
    public static final String GROUP_ADMINISTRATORS_DEFAULT = "administrators";

    public static final Logger logger = LoggerFactory.getLogger(SecurityLogic.class);

    public static boolean hasPermissions(Configuration conf, Dispatch dispatch, Subject subject, Method handler) {
        logger.debug("Checking action permissions");
        return hasPermissions(conf, dispatch.getLastPageInstance(), subject, handler);
    }

    public static boolean hasPermissions(Configuration conf, PageInstance instance, Subject subject, Method handler) {
        logger.debug("Checking action permissions");
        Class<?> theClass = instance.getActionClass();
        RequiresPermissions requiresPermissions = getRequiresPermissionsAnnotation(handler, theClass);
        if(requiresPermissions != null) {
            AccessLevel accessLevel = requiresPermissions.level();
            String[] permissions = requiresPermissions.permissions();
            return hasPermissions(conf, instance, subject, accessLevel, permissions);
        }
        return true;
    }

    public static boolean hasPermissions
            (Configuration conf, PageInstance instance, Subject subject, AccessLevel accessLevel, String... permissions) {
        Permissions configuration = calculateActualPermissions(instance);
        return hasPermissions(conf, configuration, subject, accessLevel, permissions);
    }

    public static Permissions calculateActualPermissions(PageInstance instance) {
        List<Page> pages = new ArrayList<Page>();
        while (instance != null) {
            pages.add(0, instance.getPage());
            instance = instance.getParent();
        }

        return calculateActualPermissions(new Permissions(), pages);
    }

    public static Permissions calculateActualPermissions(Permissions basePermissions, List<Page> pages) {
        Permissions result = new Permissions();
        Map<String, AccessLevel> resultLevels = result.getActualLevels();
        resultLevels.putAll(basePermissions.getActualLevels());
        for (Page current : pages) {
            Permissions currentPerms = current.getPermissions();

            Map<String, AccessLevel> currentLevels = currentPerms.getActualLevels();
            for(Map.Entry<String, AccessLevel> entry : currentLevels.entrySet()) {
                String currentGroup = entry.getKey();
                AccessLevel currentLevel = entry.getValue();
                AccessLevel resultLevel = resultLevels.get(currentGroup);

                if(resultLevel != AccessLevel.DENY && currentLevel != null) {
                    resultLevels.put(currentGroup, currentLevel);
                }
            }
        }

        if (pages.size() > 0) {
            Page lastPage = pages.get(pages.size() - 1);
            Map<String, Set<String>> lastPermissions =
                    lastPage.getPermissions().getActualPermissions();
            result.getActualPermissions().putAll(lastPermissions);
        } else {
            result.getActualPermissions().putAll(basePermissions.getActualPermissions());
        }

        return result;
    }

    public static boolean hasPermissions
            (Configuration conf, Permissions configuration, Subject subject, Method handler, Class<?> theClass) {
        logger.debug("Checking action permissions");
        RequiresPermissions requiresPermissions = getRequiresPermissionsAnnotation(handler, theClass);
        if(requiresPermissions != null) {
            return hasPermissions(conf, configuration, subject, requiresPermissions);
        } else {
            return true;
        }
    }

    public static boolean hasPermissions
            (Configuration conf, Permissions configuration, Subject subject, RequiresPermissions thing) {
        return hasPermissions(conf, configuration, subject, thing.level(), thing.permissions());
    }

    public static RequiresPermissions getRequiresPermissionsAnnotation(Method handler, Class<?> theClass) {
        RequiresPermissions requiresPermissions = handler.getAnnotation(RequiresPermissions.class);
        if (requiresPermissions != null) {
            logger.debug("Action method requires specific permissions: {}", handler);
        } else {
            requiresPermissions = theClass.getAnnotation(RequiresPermissions.class);
            if (requiresPermissions != null) {
                logger.debug("Action class requires specific permissions: {}",
                             theClass);
            }
        }
        return requiresPermissions;
    }

    public static boolean hasPermissions
            (Configuration conf, Permissions configuration, Subject subject, AccessLevel level, String... permissions) {
        if(subject.isAuthenticated()) {
            String administratorsGroup = getAdministratorsGroup(conf);
            if(isUserInGroup(administratorsGroup)) {
                return true;
            }
            PagePermission pagePermission = new PagePermission(configuration, level, permissions);
            return subject.isPermitted(pagePermission);
        } else {
            //Shiro does not check permissions for non authenticated users
            return hasAnonymousPermissions(conf, configuration, level, permissions);
        }
    }

    public static boolean hasPermissions
            (Configuration conf, Permissions configuration, org.apache.shiro.mgt.SecurityManager securityManager,
             PrincipalCollection principals, AccessLevel level, String... permissions) {
        if(principals != null) {
            PagePermission pagePermission = new PagePermission(configuration, level, permissions);
            return securityManager.isPermitted(principals, pagePermission);
        } else {
            //Shiro does not check permissions for non authenticated users
            return hasAnonymousPermissions(conf, configuration, level, permissions);
        }
    }

    public static boolean hasAnonymousPermissions
            (Configuration conf, Permissions configuration, AccessLevel level, String... permissions) {
        PagePermission pagePermission = new PagePermission(configuration, level, permissions);
        List<String> groups = new ArrayList<String>();
        groups.add(getAllGroup(conf));
        groups.add(getAnonymousGroup(conf));
        return new GroupPermission(groups).implies(pagePermission);
    }

    public static boolean isUserInGroup(String groupId) {
        Subject subject = SecurityUtils.getSubject();
        return subject.hasRole(groupId);
    }

    public static boolean isAdministrator(ServletRequest request) {
        ServletContext servletContext = ElementsThreadLocals.getServletContext();
        Configuration conf =
                (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
        return isAdministrator(conf);
    }

    public static boolean isAdministrator(Configuration conf) {
        String administratorsGroup = getAdministratorsGroup(conf);
        return isUserInGroup(administratorsGroup);
    }

    public static boolean satisfiesRequiresAdministrator(HttpServletRequest request, ActionBean actionBean, Method handler) {
        logger.debug("Checking if action or method required administrator");
        boolean requiresAdministrator = false;
        if (handler.isAnnotationPresent(RequiresAdministrator.class)) {
            logger.debug("Action method requires administrator: {}", handler);
            requiresAdministrator = true;
        } else {
            Class actionClass = actionBean.getClass();
            while (actionClass != null) {
                if (actionClass.isAnnotationPresent(RequiresAdministrator.class)) {
                    logger.debug("Action class requires administrator: {}", actionClass);
                    requiresAdministrator = true;
                    break;
                }
                actionClass = actionClass.getSuperclass();
            }
        }

        boolean isNotAdmin = !isAdministrator(request);
        boolean doesNotSatisfy = requiresAdministrator && isNotAdmin;
        if (doesNotSatisfy) {
            logger.info("User is not an administrator");
            return false;
        }
        return true;
    }

    public static String getAdministratorsGroup(Configuration conf) {
        return conf.getString(GROUP_ADMINISTRATORS, GROUP_ADMINISTRATORS_DEFAULT);
    }

    public static String getAllGroup(Configuration conf) {
        return conf.getString(GROUP_ALL, GROUP_ALL_DEFAULT);
    }

    public static String getAnonymousGroup(Configuration conf) {
        return conf.getString(GROUP_ANONYMOUS, GROUP_ANONYMOUS_DEFAULT);
    }

    public static String getRegisteredGroup(Configuration conf) {
        return conf.getString(GROUP_REGISTERED, GROUP_REGISTERED_DEFAULT);
    }

}
