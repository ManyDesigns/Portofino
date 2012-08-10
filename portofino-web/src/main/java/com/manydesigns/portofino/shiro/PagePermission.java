/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.shiro;

import com.manydesigns.portofino.pages.Permissions;
import com.manydesigns.portofino.security.AccessLevel;
import org.apache.shiro.authz.Permission;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PagePermission implements Permission {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected final Permissions calculatedPermissions;
    protected final AccessLevel accessLevel;
    protected final String[] permissions;

    public PagePermission(Permissions calculatedPermissions, AccessLevel accessLevel, String... permissions) {
        this.calculatedPermissions = calculatedPermissions;
        this.accessLevel = accessLevel;
        this.permissions = permissions;
    }

    public boolean implies(Permission p) {
        return false;
    }

    public Permissions getCalculatedPermissions() {
        return calculatedPermissions;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public String[] getPermissions() {
        return permissions;
    }
}
