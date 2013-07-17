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

package com.manydesigns.portofino.shiro;

import com.manydesigns.portofino.pages.Permissions;
import com.manydesigns.portofino.security.AccessLevel;
import org.apache.shiro.authz.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 *
 * Permission associated to a Subject which holds the subject's groups.
 */
public class GroupPermission implements Permission {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final Collection<String> groups;
    public static final Logger logger = LoggerFactory.getLogger(GroupPermission.class);

    public GroupPermission(Collection<String> groups) {
        this.groups = groups;
    }

    public boolean implies(Permission p) {
        if(p instanceof GroupPermission) {
            GroupPermission gp = (GroupPermission) p;
            return gp.groups.containsAll(groups) && groups.containsAll(gp.groups);
        } else if(p instanceof PagePermission) {
            PagePermission pp = (PagePermission) p;
            return hasPermissions
                    (pp.getCalculatedPermissions(), groups, pp.getAccessLevel(), pp.getPermissions());
        }
        return false;
    }

    public static boolean hasPermissions
            (Permissions configuration, Collection<String> groups, AccessLevel level, String... permissions) {
        boolean hasLevel = level == null;
        boolean hasPermissions = true;
        Map<String, Boolean> permMap = new HashMap<String, Boolean>(permissions.length);
        for(String groupId : groups) {
            AccessLevel actualLevel = configuration.getActualLevels().get(groupId);
            if(actualLevel == AccessLevel.DENY) {
                return false;
            } else if(!hasLevel &&
                      actualLevel != null &&
                      actualLevel.isGreaterThanOrEqual(level)) {
                hasLevel = true;
            }

            Set<String> perms = configuration.getActualPermissions().get(groupId);
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
            logger.debug("User does not have permissions. User's groups: {}", groups);
        }
        return hasPermissions;
    }
}
