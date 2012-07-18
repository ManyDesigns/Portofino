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

package com.manydesigns.portofino.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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

    /**
     * Clears the cache for a list of principals identifying a user. The cache typically contains authentication and
     * authorization information.
     * @param principals the principals associated with a user.
     */
    public static void clearCache(PrincipalCollection principals) {
        SecurityManager securityManager = SecurityUtils.getSecurityManager();
        if(securityManager instanceof RealmSecurityManager) {
            RealmSecurityManager rsm = (RealmSecurityManager) securityManager;
            for(Realm realm : rsm.getRealms()) {
                if(realm instanceof ApplicationRealm) {
                    ((ApplicationRealm) realm).clearCache(principals);
                }
            }
        }
    }

    /**
     * Clears the Shiro cache for the current Subject.
     */
    public static void clearCacheForCurrentSubject() {
        clearCache(SecurityUtils.getSubject().getPrincipals());
    }

}
