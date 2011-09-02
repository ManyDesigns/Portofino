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

import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.system.model.users.Group;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SecurityLogic {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static final String ANONYMOUS_GROUP_ID = "anonymous";
    public static final String REGISTERED_GROUP_ID = "registered";
    public static final String ADMINISTRATORS_GROUP_ID = "administrators";

    public static Group findAnonymousGroup(Application application) {
        return findGroupById(application, ANONYMOUS_GROUP_ID);
    }

    public static Group findRegisteredGroup(Application application) {
        return findGroupById(application, REGISTERED_GROUP_ID);
    }

    public static Group findAdministratorsGroup(Application application) {
        return findGroupById(application, ADMINISTRATORS_GROUP_ID);
    }

    private static Group findGroupById(Application application, String groupId) {
        return (Group) application.getObjectByPk(
                "portofino.public.groups", groupId);
    }
}
