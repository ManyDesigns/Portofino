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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import java.io.Serializable;
import java.util.List;

/**
 * Contains a few utility methods for Shiro.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ShiroUtils {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    /**
     * Returns the primary principal for a Subject - that is, in Portofino, the username.
     * @param s the subject
     * @return the username.
     */
    public static Object getPrimaryPrincipal(Subject s) {
        return getPrincipal(s, 0);
    }

    /**
     * Returns the nth principal of the given Subject. Custom security.groovy implementations might assign
     * more than one principal to a Subject.
     * @param s the subject
     * @param i the zero-based index of the principal
     * @return the principal
     * @throws IndexOutOfBoundsException if the index is greather than the number of principals associated with the
     * subject.
     */
    public static Object getPrincipal(Subject s, int i) {
        Object principal = s.getPrincipal();
        if(principal instanceof PrincipalCollection) {
            List principals = ((PrincipalCollection) principal).asList();
            return principals.get(i);
        } else {
            if(i == 0) {
                return principal;
            } else {
                throw new IndexOutOfBoundsException("The subject has only 1 principal, index " + i + " is not valid");
            }
        }
    }

    public static PortofinoRealm getPortofinoRealm() {
        RealmSecurityManager realmSecurityManager =
                (RealmSecurityManager)SecurityUtils.getSecurityManager();
        PortofinoRealm portofinoRealm =
                (PortofinoRealm) realmSecurityManager.getRealms().iterator().next();
        return portofinoRealm;
    }

    public static Serializable getUserId(Subject subject) {
        PortofinoRealm portofinoRealm = getPortofinoRealm();
        Serializable principal = (Serializable) getPrimaryPrincipal(subject);
        if(portofinoRealm != null) {
            return portofinoRealm.getUserId(principal);
        } else {
            return principal;
        }
    }

}
