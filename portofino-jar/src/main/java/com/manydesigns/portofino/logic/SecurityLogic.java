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

import com.manydesigns.portofino.SessionAttributes;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UsersGroups;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import sun.misc.BASE64Encoder;

import javax.servlet.ServletRequest;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

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
    public static final String USERTABLE = "portofino.public.users";
    public static final String GROUPTABLE = "portofino.public.groups";

    public static final String PASSWORD = "pwd";
    public static final String GROUPS = "groups";

    public static final int ACTIVE = 1;
    public static final int SUSPENDED = 2;
    public static final int BANNED = 3;
    public static final int SELFREGITRED = 4;

    public static List<String> manageGroups(Application application, String userId) {
        List<String> groups = new ArrayList<String>();
        if (userId == null) {
            groups.add(application.getAnonymousGroup().getName());
        } else {
            User u = (User) application.getObjectByPk(USERTABLE,
                    new User(userId));
            groups.add(application.getAnonymousGroup().getName());
            groups.add(application.getRegisteredGroup().getName());

            for (UsersGroups ug : u.getGroups()) {
                if (ug.getDeletionDate() == null) {
                    groups.add(ug.getGroup().getName());
                }
            }
        }
        return groups;
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
        return isUserInGroup(request, group.getName());
    }

    public static boolean isUserInGroup(ServletRequest request, String name) {
        List<String> groups = (List<String>) request.getAttribute(RequestAttributes.GROUPS);
        return groups.contains(name);
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

    public static User defaultLogin(Application application, String username, String password) {
        String qualifiedTableName = USERTABLE;
        Session session = application.getSession(qualifiedTableName);
        org.hibernate.Criteria criteria = session.createCriteria("portofino_public_users");
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

}
