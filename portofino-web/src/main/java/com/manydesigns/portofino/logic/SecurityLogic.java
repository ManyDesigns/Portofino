/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.logic;

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.RequestAttributes;
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(SecurityLogic.class);

    public static boolean hasPermissions
            (ServletRequest request, AccessLevel level, String... permissions) {
        boolean isNotAdmin = !isAdministrator(request);
        Dispatch dispatch =
                (Dispatch) request.getAttribute(RequestAttributes.DISPATCH);
        if (isNotAdmin && dispatch != null) {
            Subject subject = SecurityUtils.getSubject();
            PageInstance pageInstance = dispatch.getLastPageInstance();
            Page page = pageInstance.getPage();
            return hasPermissions
                    (dispatch.getLastPageInstance().getApplication(), page.getPermissions(),
                     subject, level, permissions);
        } else {
            return true;
        }
    }

    public static boolean hasPermissions(Dispatch dispatch, Subject subject, Method handler) {
        logger.debug("Checking action permissions");
        return hasPermissions(dispatch.getLastPageInstance(), subject, handler);
    }

    public static boolean hasPermissions(PageInstance instance, Subject subject, Method handler) {
        logger.debug("Checking action permissions");
        Class<?> theClass = instance.getActionClass();
        RequiresPermissions requiresPermissions = getRequiresPermissionsAnnotation(handler, theClass);
        if(requiresPermissions != null) {
            AccessLevel accessLevel = requiresPermissions.level();
            String[] permissions = requiresPermissions.permissions();
            return hasPermissions(instance, subject, accessLevel, permissions);
        }
        return true;
    }

    public static boolean hasPermissions
            (PageInstance instance, Subject subject, AccessLevel accessLevel, String... permissions) {
        Permissions configuration = calculateActualPermissions(instance);
        return hasPermissions(instance.getApplication(), configuration, subject, accessLevel, permissions);
    }

    public static Permissions calculateActualPermissions(PageInstance instance) {
        List<Page> pages = new ArrayList<Page>();
        while (instance != null) {
            pages.add(0, instance.getPage());
            instance = instance.getParent();
        }

        return calculateActualPermissions(pages);
    }

    public static Permissions calculateActualPermissions(List<Page> pages) {
        Permissions result = new Permissions();
        Map<String, AccessLevel> resultLevels = result.getActualLevels();
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
        }

        return result;
    }

    public static boolean hasPermissions
            (Application application, Permissions configuration, Subject subject, Method handler, Class<?> theClass) {
        logger.debug("Checking action permissions");
        RequiresPermissions requiresPermissions = getRequiresPermissionsAnnotation(handler, theClass);
        if(requiresPermissions != null) {
            return hasPermissions(application, configuration, subject, requiresPermissions);
        } else {
            return true;
        }
    }

    public static boolean hasPermissions
            (Application application, Permissions configuration, Subject subject, RequiresPermissions thing) {
        return hasPermissions(application, configuration, subject, thing.level(), thing.permissions());
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
            (Application application, Permissions configuration, Subject subject, AccessLevel level, String... permissions) {
        if(subject.isAuthenticated()) {
            PagePermission pagePermission = new PagePermission(configuration, level, permissions);
            return subject.isPermitted(pagePermission);
        } else {
            //Shiro does not check permissions for non authenticated users
            return hasAnonymousPermissions(application, configuration, level, permissions);
        }
    }

    public static boolean hasPermissions
            (Application application, Permissions configuration, org.apache.shiro.mgt.SecurityManager securityManager,
             PrincipalCollection principals, AccessLevel level, String... permissions) {
        if(principals != null) {
            PagePermission pagePermission = new PagePermission(configuration, level, permissions);
            return securityManager.isPermitted(principals, pagePermission);
        } else {
            //Shiro does not check permissions for non authenticated users
            return hasAnonymousPermissions(application, configuration, level, permissions);
        }
    }

    public static boolean hasAnonymousPermissions
            (Application application, Permissions configuration, AccessLevel level, String... permissions) {
        PagePermission pagePermission = new PagePermission(configuration, level, permissions);
        Configuration portofinoConfiguration = application.getPortofinoProperties();
        List<String> groups = new ArrayList<String>();
        groups.add(portofinoConfiguration.getString(PortofinoProperties.GROUP_ALL));
        groups.add(portofinoConfiguration.getString(PortofinoProperties.GROUP_ANONYMOUS));
        return new GroupPermission(groups).implies(pagePermission);
    }

    public static boolean isUserInGroup(ServletRequest request, String groupId) {
        Subject subject = SecurityUtils.getSubject();
        return subject.hasRole(groupId);
    }

    public static boolean isAdministrator(ServletRequest request) {
        Application appl = (Application) request.getAttribute(RequestAttributes.APPLICATION);
        Configuration portofinoConfiguration = appl.getPortofinoProperties();
        String administratorsGroup = portofinoConfiguration.getString(PortofinoProperties.GROUP_ADMINISTRATORS);
        return isUserInGroup(request, administratorsGroup);
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
                    logger.debug("Action class requires administrator: {}",
                    actionClass);
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
}
