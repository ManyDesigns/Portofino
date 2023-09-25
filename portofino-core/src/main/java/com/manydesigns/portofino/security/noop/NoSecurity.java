/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.security.noop;

import com.manydesigns.portofino.resourceactions.Permissions;
import com.manydesigns.portofino.resourceactions.ActionInstance;
import com.manydesigns.portofino.resourceactions.ResourceAction;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.SecurityFacade;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.FileObject;

import javax.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class NoSecurity extends SecurityFacade {
    public static final NoSecurity AT_ALL = new NoSecurity();
    public static final Object BEAN = new Object();

    private NoSecurity() {}

    @Override
    public boolean hasPermissions(Configuration conf, Permissions configuration, AccessLevel accessLevel, String... permissions) {
        return true;
    }

    @Override
    public boolean isOperationAllowed(HttpServletRequest request, ActionInstance actionInstance, ResourceAction resourceAction, Method handler) {
        return true;
    }

    @Override
    public boolean isAdministrator(Configuration conf) {
        return true;
    }

    @Override
    public Object getUserId() {
        return null;
    }

    @Override
    public Set<String> getGroups() {
        return Collections.emptySet();
    }

    @Override
    public Map getUsers() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public void setup(FileObject appDir, String adminGroupName, String encryptionAlgorithm) {}

    @Override
    public boolean isUserAuthenticated() {
        return true;
    }

    @Override
    public Object getSecurityUtilsBean() {
        return BEAN;
    }

    @Override
    public void checkWebResourceIsAccessible(ContainerRequestContext requestContext, Object resource, Method handler) {}
}
