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
package com.manydesigns.portofino.actions.user.admin;


import com.manydesigns.elements.servlet.ServletUtils;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.actions.CrudAction;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.actions.admin.AdminAction;
import com.manydesigns.portofino.breadcrumbs.Breadcrumbs;
import com.manydesigns.portofino.dispatcher.CrudPageInstance;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.pages.CrudPage;
import com.manydesigns.portofino.model.pages.crud.Crud;
import com.manydesigns.portofino.model.pages.crud.CrudProperty;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UsersGroups;
import com.manydesigns.portofino.system.model.users.annotations.RequiresAdministrator;
import net.sourceforge.stripes.action.*;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@RequiresAdministrator
@UrlBinding(UserAdminAction.BASE_PATH + "/{pk}")
public class UserAdminAction extends CrudAction implements AdminAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String BASE_PATH = "/actions/admin/users";

    /*private final int pwdLength;
    private final Boolean enc;*/

    protected List<Group> availableUserGroups;
    protected List<Group> userGroups;
    protected List<String> groupNames;

    //**************************************************************************
    // Injections
    //**************************************************************************

    //**************************************************************************
    // Action methods
    //**************************************************************************

    public UserAdminAction() {
        /*this.pwdLength = Integer.parseInt(PortofinoProperties.getProperties()
                .getProperty("pwd.lenght.min","6"));
        enc = Boolean.parseBoolean(PortofinoProperties.getProperties()
                .getProperty(PortofinoProperties.PWD_ENCRYPTED, "false"));
        */
    }

    @Override
    @Before
    public void prepare() {
        crudPage = new CrudPage();
        Crud crud = new Crud();
        configureCrud(crud);
        crudPage.setCrud(crud);
        crudPage.setSearchUrl("/layouts/admin/users/userSearch.jsp");
        crudPage.setReadUrl("/layouts/admin/users/userRead.jsp");
        crudPage.setEditUrl("/layouts/admin/users/userEdit.jsp");
        crudPage.setBulkEditUrl("/layouts/admin/users/userBulkEdit.jsp");
        crudPage.setCreateUrl("/layouts/admin/users/userCreate.jsp");
        crudPage.setFragment(BASE_PATH.substring(1));
        crudPage.setTitle("Users");
        crudPage.setDescription("Users administration");
        model.init(crudPage);
        String mode;
        if (StringUtils.isEmpty(pk)) {
            mode = CrudPage.MODE_SEARCH;
        } else {
            mode = CrudPage.MODE_DETAIL;
        }
        pageInstance = new CrudPageInstance(application, crudPage, mode, pk);
        pageInstance.realize();
        PageInstance rootPageInstance = new PageInstance(application, model.getRootPage(), null);
        HttpServletRequest request = context.getRequest();
        String originalPath = ServletUtils.getOriginalPath(request);
        dispatch = new Dispatch(request, originalPath, originalPath, rootPageInstance, pageInstance);
        Breadcrumbs breadcrumbs = new Breadcrumbs(dispatch);
        request.setAttribute(RequestAttributes.DISPATCH, dispatch);
        request.setAttribute(RequestAttributes.BREADCRUMBS, breadcrumbs);
        super.prepare();
    }

    @Override
    public Resolution read() {
        setupUserGroups();
        return super.read();
    }

    @Override
    public Resolution edit() {
        setupUserGroups();
        return super.edit();
    }

    protected void setupUserGroups() {
        List<Group> groups = new ArrayList<Group>();
        groups.addAll(application.getAllObjects(SecurityLogic.GROUPTABLE));
        availableUserGroups = new ArrayList<Group>();

        Group anonymous = application.getAnonymousGroup();
        Group registered = application.getRegisteredGroup();

        userGroups = new ArrayList<Group>();
        User user = (User) object;
        for(UsersGroups ug : user.getGroups()) {
            if(ug.getDeletionDate() == null) {
                userGroups.add(ug.getGroup());
            }
        }

        for (Group group : groups) {
            if (!userGroups.contains(group) && !anonymous.equals(group) && !registered.equals(group)) {
                availableUserGroups.add(group);
            }
        }
    }

    @Override
    protected boolean createValidate(Object object) {
        User user = (User) object;
        user.setCreationDate(new Timestamp(System.currentTimeMillis()));
        user.setUserId(RandomUtil.createRandomId(20));
        return true;
    }

    @Override
    protected boolean editValidate(Object object) {
        User user = (User) object;

        user.setModifiedDate(new Timestamp(System.currentTimeMillis()));

        ArrayList<String> names = new ArrayList<String>();
        if(groupNames != null) {
            for(String groupName : groupNames) {
                names.add(groupName.substring("group_".length()));
            }
        }
        for(UsersGroups ug : user.getGroups()) {
            if(ug.getDeletionDate() == null) {
                Group group = ug.getGroup();
                String groupName = group.getName();
                if(names.contains(groupName)) {
                    names.remove(groupName);
                } else {
                    ug.setDeletionDate(new Timestamp(System.currentTimeMillis()));
                }
            }
        }
        for(String groupName : names) {
            Session session = application.getSessionByDatabaseName("portofino");
            Group group = (Group) session
                    .createCriteria(SecurityLogic.GROUP_ENTITY_NAME)
                    .add(Restrictions.eq("name", groupName))
                    .uniqueResult();
            UsersGroups ug = new UsersGroups();
            ug.setCreationDate(new Timestamp(System.currentTimeMillis()));
            ug.setUser(user);
            ug.setUserid(user.getUserId());
            ug.setGroup(group);
            ug.setGroupid(group.getGroupId());
            user.getGroups().add(ug);
            session.save("portofino_public_users_groups", ug);
            session.update(SecurityLogic.USER_ENTITY_NAME, user);
        }

        return true;
    }

    protected void configureCrud(Crud crud) {
        crud.setTable(SecurityLogic.USERTABLE);
        crud.setQuery("FROM portofino_public_users");
        crud.setSearchTitle("Users");
        crud.setCreateTitle("Create user");
        crud.setEditTitle("Edit user");
        crud.setReadTitle("User");

        CrudProperty property;

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("userId");
        property.setEnabled(true);
        property.setInSummary(true);
        property.setLabel("Id");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("creationDate");
        property.setEnabled(true);
        property.setInSummary(true);
        property.setLabel("Creation date");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("middleName");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        property.setLabel("Creation date");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("modifiedDate");
        property.setEnabled(true);
        property.setLabel("Modified date");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("deletionDate");
        property.setEnabled(true);
        property.setLabel("Deletion date");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("pwd");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        property.setLabel("Password");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("extAuth");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        property.setLabel("Ext auth");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("pwdModDate");
        property.setEnabled(true);
        property.setLabel("Pwd mod date");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("defaultUser");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("bounced");
        property.setEnabled(true);
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("jobTitle");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("lastLoginDate");
        property.setEnabled(true);
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("lastFailedLoginDate");
        property.setEnabled(true);
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("failedLoginAttempts");
        property.setEnabled(true);
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("remQuestion");
        property.setEnabled(false);
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("remans");
        property.setEnabled(false);
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("graceLoginCount");
        property.setEnabled(true);
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("agreedToTerms");
        property.setEnabled(true);
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setCrud(crud);
        property.setName("token");
        property.setEnabled(true);
        crud.getProperties().add(property);
    }

    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    @Override
    protected Resolution forwardToPortletPage(String pageJsp) {
        return new ForwardResolution(pageJsp);
    }

    public List<Group> getAvailableUserGroups() {
        return availableUserGroups;
    }

    public List<Group> getUserGroups() {
        return userGroups;
    }

    public List<String> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
    }

    public String getActionPath() {
        return dispatch.getAbsoluteOriginalPath();
    }
}
