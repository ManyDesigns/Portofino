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
package com.manydesigns.portofino.actions.admin.users;


import com.manydesigns.elements.servlet.ServletUtils;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.admin.AdminAction;
import com.manydesigns.portofino.application.QueryUtils;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.dispatcher.RequestAttributes;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.pageactions.crud.CrudAction;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudConfiguration;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UsersGroups;
import com.manydesigns.portofino.system.model.users.annotations.RequiresAdministrator;
import net.sourceforge.stripes.action.*;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStreamReader;
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
@UrlBinding(UserAdminAction.BASE_PATH + "/{userId}")
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
    protected String groupNames;
    protected String userId;

    //**************************************************************************
    // Logging
    //**************************************************************************

    private static final Logger logger =
            LoggerFactory.getLogger(UserAdminAction.class);

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
        try {
            Page page = DispatcherLogic.loadPage(new InputStreamReader(getClass().getResourceAsStream("page.xml")));
            page.init();
            crudConfiguration = DispatcherLogic.loadConfiguration
                    (new InputStreamReader(getClass().getResourceAsStream("configuration.xml")), CrudConfiguration.class);
            crudConfiguration.init(application);
            pageInstance = new PageInstance(null, null, application, page);
            pageInstance.setActionBean(this);
            pageInstance.setActionClass(getClass());
            pageInstance.setConfiguration(crudConfiguration);
            if(!StringUtils.isBlank(userId)) {
                pageInstance.getParameters().add(userId);
            }
            HttpServletRequest request = context.getRequest();
            String originalPath = ServletUtils.getOriginalPath(request);
            dispatch = new Dispatch(request.getContextPath(), originalPath, pageInstance);
            request.setAttribute(RequestAttributes.DISPATCH, dispatch);
            prepare(pageInstance, context);
            super.prepare();
        } catch (Exception e) {
            logger.error("Internal error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resolution read() {
        setupUserGroups();
        return super.read();
    }

    @Override
    @Button(list = "crud-read", key = "commons.edit", order = 1)
    public Resolution edit() {
        setupUserGroups();
        return super.edit();
    }

    protected void setupUserGroups() {
        Session session = application.getSystemSession();
        Criteria criteria = session.createCriteria(DatabaseLogic.GROUP_ENTITY_NAME);
        List<Group> groups = new ArrayList(criteria.list());
        availableUserGroups = new ArrayList<Group>();

        Group anonymous = getAnonymousGroup();
        Group registered = getRegisteredGroup();

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

    public Group getAllGroup() {
        String name = portofinoConfiguration.getString(PortofinoProperties.GROUP_ALL);
        return getGroup(name);
    }

    public Group getAnonymousGroup() {
        String name = portofinoConfiguration.getString(PortofinoProperties.GROUP_ANONYMOUS);
        return getGroup(name);
    }

    public Group getRegisteredGroup() {
        String name = portofinoConfiguration.getString(PortofinoProperties.GROUP_REGISTERED);
        return getGroup(name);
    }

    public Group getAdministratorsGroup() {
        String name = portofinoConfiguration.getString(PortofinoProperties.GROUP_ADMINISTRATORS);
        return getGroup(name);
    }

    protected Group getGroup(String name) {
        TableAccessor table =
                application.getTableAccessor(application.getSystemDatabaseName(), DatabaseLogic.GROUP_ENTITY_NAME);
        assert table != null;

        String actualEntityName = table.getTable().getActualEntityName();
        Session session = application.getSystemSession();
        List result = QueryUtils.runHqlQuery
                (session,
                        "FROM " + actualEntityName + " WHERE name = ?",
                        new Object[]{name});
        if(result.isEmpty()) {
            throw new IllegalStateException("Group " + name + " not found");
        } else if(result.size() == 1) {
            return (Group) result.get(0);
        } else {
            throw new IllegalStateException("Multiple groups named " + name + " found");
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
            for(String groupName : groupNames.split(",")) {
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
        Session session = application.getSystemSession();
        for(String groupName : names) {
            Group group = (Group) session
                    .createCriteria(DatabaseLogic.GROUP_ENTITY_NAME)
                    .add(Restrictions.eq("name", groupName))
                    .uniqueResult();
            UsersGroups ug = new UsersGroups();
            ug.setCreationDate(new Timestamp(System.currentTimeMillis()));
            ug.setUser(user);
            ug.setUserid(user.getUserId());
            ug.setGroup(group);
            ug.setGroupid(group.getGroupId());
            user.getGroups().add(ug);
            session.save("users_groups", ug);
            session.update(DatabaseLogic.USER_ENTITY_NAME, user);
        }

        return true;
    }

    @Override
    protected Resolution getEditView() {
        return new ForwardResolution("/layouts/admin/users/userEdit.jsp");
    }

    @Override
    protected Resolution getBulkEditView() {
        return new ForwardResolution("/layouts/admin/users/userBulkEdit.jsp");
    }

    @Override
    protected Resolution getCreateView() {
        return new ForwardResolution("/layouts/admin/users/userCreate.jsp");
    }

    @Override
    protected Resolution getReadView() {
        return forwardToPortletPage("/layouts/admin/users/userRead.jsp");
    }

    @Override
    protected Resolution getSearchView() {
        return forwardToPortletPage("/layouts/admin/users/userSearch.jsp");
    }

    @Button(list = "contentButtons", key = "commons.returnToPages", order = 1)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    //Do not show the configure button
    @Override
    public Resolution configure() {
        return super.configure();
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

    public String getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(String groupNames) {
        this.groupNames = groupNames;
    }

    public String getActionPath() {
        return dispatch.getOriginalPath();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
