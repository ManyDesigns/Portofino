/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.security;

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.resourceactions.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            "Copyright (C) 2005-2020 ManyDesigns srl";

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

    public static Permissions calculateActualPermissions(ActionInstance instance) {
        List<ResourceActionConfiguration> actionDescriptors = new ArrayList<>();
        while (instance != null) {
            actionDescriptors.add(0, instance.getConfiguration());
            instance = instance.getParent();
        }

        return calculateActualPermissions(new Permissions(), actionDescriptors);
    }

    public static Permissions calculateActualPermissions(
            Permissions basePermissions, List<ResourceActionConfiguration> actionDescriptors) {
        Permissions result = new Permissions();
        Map<String, AccessLevel> resultLevels = result.getActualLevels();
        resultLevels.putAll(basePermissions.getActualLevels());
        for (ResourceActionConfiguration current : actionDescriptors) {
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

        if (actionDescriptors.size() > 0) {
            ResourceActionConfiguration lastAction = actionDescriptors.get(actionDescriptors.size() - 1);
            Map<String, Set<String>> lastPermissions =
                    lastAction.getPermissions().getActualPermissions();
            result.getActualPermissions().putAll(lastPermissions);
        } else {
            result.getActualPermissions().putAll(basePermissions.getActualPermissions());
        }

        return result;
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

    public static boolean satisfiesRequiresAdministrator(
            Object actionBean, Method handler, boolean isAdmin) {
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

        boolean doesNotSatisfy = requiresAdministrator && !isAdmin;
        if (doesNotSatisfy) {
            logger.debug("User is not an administrator");
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

    public static void installLogin(
            ResourceAction root, Configuration configuration, Class<? extends ResourceAction> fallbackLoginClass
    ) throws Exception {
        String relLoginPath = configuration.getString(PortofinoProperties.LOGIN_PATH);
        String loginPath;
        if(relLoginPath != null) {
            if (relLoginPath.startsWith("/")) {
                loginPath = "file:" + relLoginPath.substring(1);
            } else {
                loginPath = "file:" + relLoginPath;
            }
        } else {
            loginPath = "res:" + fallbackLoginClass.getPackage().getName().replace('.', '/');
        }
        root.unmount(":auth");
        root.mount(":auth", loginPath);
    }
}
