/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.portofino.logic;

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.SessionAttributes;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.QueryUtils;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.pages.Permissions;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UsersGroups;
import com.manydesigns.portofino.system.model.users.annotations.RequiresAdministrator;
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions;
import net.sourceforge.stripes.action.ActionBean;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SecurityLogic {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(SecurityLogic.class);

    public static final String USER_ENTITY_NAME = "users";
    public static final String GROUP_ENTITY_NAME = "groups";

    public static final String PASSWORD = "pwd";
    public static final String GROUPS = "groups";

    public static final int ACTIVE = 1;
    public static final int SUSPENDED = 2;
    public static final int BANNED = 3;
    public static final int SELFREGISTERED = 4;

    public static List<String> manageGroups(Application application, String userId) {
        List<String> groups = new ArrayList<String>();
        Configuration conf = application.getPortofinoProperties();
        groups.add(conf.getString(PortofinoProperties.GROUP_ALL));
        if (userId == null) {
            groups.add(conf.getString(PortofinoProperties.GROUP_ANONYMOUS));
        } else {
            User u = (User) QueryUtils.getObjectByPk
                    (application, application.getSystemDatabaseName(), USER_ENTITY_NAME, new User(userId));
            groups.add(conf.getString(PortofinoProperties.GROUP_REGISTERED));

            for (UsersGroups ug : u.getGroups()) {
                if (ug.getDeletionDate() == null) {
                    groups.add(ug.getGroup().getGroupId());
                }
            }
        }
        return groups;
    }

    public static boolean hasPermissions
            (ServletRequest request, AccessLevel level, String... permissions) {
        boolean isNotAdmin = !isAdministrator(request);
        Dispatch dispatch =
                (Dispatch) request.getAttribute(RequestAttributes.DISPATCH);
        if (isNotAdmin && dispatch != null) {
            List<String> groups = (List<String>) request.getAttribute(RequestAttributes.GROUPS);
            PageInstance pageInstance = dispatch.getLastPageInstance();
            Page page = pageInstance.getPage();
            return hasPermissions(page.getPermissions(), groups, level, permissions);
        } else {
            return true;
        }
    }

    public static boolean hasPermissions(Dispatch dispatch, Collection<String> groups, Method handler) {
        logger.debug("Checking action permissions");
        return hasPermissions(dispatch.getLastPageInstance(), groups, handler);
    }

    public static boolean hasPermissions(PageInstance instance, Collection<String> groups, Method handler) {
        logger.debug("Checking action permissions");
        Class<?> theClass = instance.getActionClass();
        RequiresPermissions requiresPermissions = getRequiresPermissionsAnnotation(handler, theClass);
        if(requiresPermissions != null) {
            AccessLevel accessLevel = requiresPermissions.level();
            String[] permissions = requiresPermissions.permissions();
            return hasPermissions(instance, groups, accessLevel, permissions);
        }
        return true;
    }

    public static boolean hasPermissions
            (PageInstance instance, Collection<String> groups, AccessLevel accessLevel, String... permissions) {
        Permissions configuration = calculateActualPermissions(instance);
        return hasPermissions(configuration, groups, accessLevel, permissions);
    }

    public static Permissions calculateActualPermissions(PageInstance instance) {
        Permissions localPerms = instance.getPage().getPermissions();
        if(instance.getParent() == null) {
            return localPerms;
        }

        Permissions configuration = new Permissions();
        configuration.getActualLevels().putAll(localPerms.getActualLevels());
        configuration.getActualPermissions().putAll(localPerms.getActualPermissions());
        Permissions parentPermissions = calculateActualPermissions(instance.getParent());
        Map<String, AccessLevel> parentLevels = parentPermissions.getActualLevels();
        for(Map.Entry<String, AccessLevel> entry : parentLevels.entrySet()) {
            String key = entry.getKey();
            AccessLevel value = entry.getValue();
            if(value == AccessLevel.DENY || configuration.getActualLevels().get(key) == null) {
                configuration.getActualLevels().put(key, value);
            }
        }
        return configuration;
    }

    public static boolean hasPermissions
            (Permissions configuration, Collection<String> groups, Method handler, Class<?> theClass) {
        logger.debug("Checking action permissions");
        RequiresPermissions requiresPermissions = getRequiresPermissionsAnnotation(handler, theClass);
        if(requiresPermissions != null) {
            return hasPermissions(configuration, groups, requiresPermissions);
        } else {
            return true;
        }
    }

    public static boolean hasPermissions
            (Permissions configuration, Collection<String> groups, RequiresPermissions thing) {
        return hasPermissions(configuration, groups, thing.level(), thing.permissions());
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
            (Permissions configuration, Collection<String> groups, AccessLevel level, String... permissions) {
        boolean hasLevel = level == null;
        boolean hasPermissions = true;
        Map<String, Boolean> permMap = new HashMap<String, Boolean>(permissions.length);
        for(String group : groups) {
            AccessLevel actualLevel = configuration.getActualLevels().get(group);
            if(actualLevel == AccessLevel.DENY) {
                return false;
            } else if(!hasLevel &&
                      actualLevel != null &&
                      actualLevel.isGreaterThanOrEqual(level)) {
                hasLevel = true;
            }

            Set<String> perms = configuration.getActualPermissions().get(group);
            if(perms != null) {
                for(String permission : permissions) {
                    if(perms.contains(permission)) {
                        permMap.put(permission, true);
                    }
                }
            }
        }

        for(String permission : permissions) {
            hasPermissions &= permMap.containsKey(permission);
        }

        hasPermissions = hasLevel && hasPermissions;
        if(!hasPermissions) {
            logger.debug("User does not have permissions. User's groups: {}", ArrayUtils.toString(groups));
        }
        return hasPermissions;
    }

    public static String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(password.getBytes("UTF-8"));
            byte raw[] = md.digest();
            return (new BASE64Encoder()).encode(raw);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static boolean isUserInGroup(ServletRequest request, Group group) {
        return isUserInGroup(request, group.getGroupId());
    }

    public static boolean isUserInGroup(ServletRequest request, String groupId) {
        List<String> groups = (List<String>) request.getAttribute(RequestAttributes.GROUPS);
        return groups.contains(groupId);
    }

    public static boolean isRegisteredUser(ServletRequest request) {
        Application appl = (Application) request.getAttribute(RequestAttributes.APPLICATION);
        Group registeredGroup = appl.getRegisteredGroup();
        return isUserInGroup(request, registeredGroup);
    }

    public static boolean isAdministrator(ServletRequest request) {
        Application appl = (Application) request.getAttribute(RequestAttributes.APPLICATION);
        Group administratorsGroup = appl.getAdministratorsGroup();
        return isUserInGroup(request, administratorsGroup);
    }

    private static Group findGroupById(Application application, String groupId) {
        return (Group) QueryUtils.getObjectByPk(application, application.getSystemDatabaseName(),
                GROUP_ENTITY_NAME, groupId);
    }

    public static User defaultLogin(Application application, String username, String password) {
        Session session = application.getSystemSession();
        org.hibernate.Criteria criteria = session.createCriteria("users");
        criteria.add(Restrictions.eq(SessionAttributes.USER_NAME, username));
        criteria.add(Restrictions.eq(PASSWORD, password));

        @SuppressWarnings({"unchecked"})
        List<Object> result = (List<Object>) criteria.list();

        if (result.size() == 1) {
            return (User) result.get(0);
        } else {
            return null;
        }
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
