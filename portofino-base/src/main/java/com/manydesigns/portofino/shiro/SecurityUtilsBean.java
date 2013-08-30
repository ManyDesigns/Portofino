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
import org.apache.shiro.subject.Subject;

import java.io.Serializable;

/**
 * A bean meant to facilitate access to certain static methods in OGNL. Wraps static methods in the SecurityUtils
 * and ShiroUtils classes and exposed them as JavaBean properties or ordinary instance methods.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SecurityUtilsBean {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    /**
     * Wrapper for SecurityUtils.getSubject()
     */
    public Subject getSubject() {
        return SecurityUtils.getSubject();
    }

    public Serializable getUserId() {
        return ShiroUtils.getUserId(getSubject());
    }

    /**
     * Wrapper for SecurityUtils.getSecurityManager()
     */
    public org.apache.shiro.mgt.SecurityManager getSecurityManager() {
        return SecurityUtils.getSecurityManager();
    }

    /**
     * Wrapper for ShiroUtils.getPrimaryPrincipal(Subject)
     */
    public Object getPrimaryPrincipal() {
        return ShiroUtils.getPrimaryPrincipal(getSubject());
    }

    /**
     * Wrapper for ShiroUtils.getPrincipal(Subject, int)
     */
    public Object getPrincipal(int index) {
        return ShiroUtils.getPrincipal(getSubject(), index);
    }

}
