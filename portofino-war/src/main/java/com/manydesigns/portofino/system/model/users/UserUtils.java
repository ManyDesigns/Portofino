/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.portofino.system.model.users;

import com.manydesigns.portofino.context.Context;

import java.util.List;
import java.util.ArrayList;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UserUtils {
    public static final String copyright
            = "Copyright (c) 2005-2010, ManyDesigns srl";

    public static final String USERTABLE = "portofino.public.users";
    public static final String GROUPTABLE = "portofino.public.groups";

    public static final Long ACTIVE = 1L;
    public static final Long SUSPENDED = 2L;
    public static final Long BANNED = 3L;
    public static final Long SELFREGITRED = 4L;

    public static  List<String> manageGroups(Context context) {
        User currentUser = context.getCurrentUser();
        List<String> groups = new ArrayList<String>();
        if(currentUser==null){
                groups.add(Group.ANONYMOUS);
            } else {
            User u = (User) context.getObjectByPk(UserUtils.USERTABLE, currentUser);
            groups.add(Group.ANONYMOUS);
            groups.add(Group.REGISTERED);

            for (UsersGroups ug : u.getGroups()){
                if (ug.getDeletionDate()==null){
                    groups.add(ug.getGroup().getName());
                }
            }
        }
        return groups;
    }
}
