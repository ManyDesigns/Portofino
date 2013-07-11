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

package com.manydesigns.portofino.shiro.openid;

import org.apache.shiro.authc.AuthenticationToken;
import org.openid4java.discovery.Identifier;

import java.io.Serializable;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class OpenIDToken implements AuthenticationToken, Serializable {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final Identifier identifier;
    protected final String firstLoginToken;

    public OpenIDToken(Identifier identifier, String firstLoginToken) {
        this.identifier = identifier;
        this.firstLoginToken = firstLoginToken;
    }

    public Identifier getPrincipal() {
        return identifier;
    }

    public Identifier getCredentials() {
        return identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public String getFirstLoginToken() {
        return firstLoginToken;
    }
}
