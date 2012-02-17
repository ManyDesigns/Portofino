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
package com.manydesigns.portofino.actions.admin.groups;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.servlet.ServletUtils;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.actions.admin.AdminAction;
import com.manydesigns.portofino.actions.admin.users.UserAdminAction;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.pageactions.crud.CrudAction;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudConfiguration;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.system.model.users.Group;
import net.sourceforge.stripes.action.*;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@RequiresAdministrator
@UrlBinding(GroupAdminAction.BASE_PATH + "/{groupId}")
public class GroupAdminAction extends CrudAction implements AdminAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static final String BASE_PATH = "/actions/admin/groups";

    protected String groupId;

    //**************************************************************************
    // Logging
    //**************************************************************************

    private static final Logger logger =
            LoggerFactory.getLogger(GroupAdminAction.class);

    @Button(list = "contentButtons", key = "commons.returnToPages", order = 1)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    //**************************************************************************
    // Overrides
    //**************************************************************************

    @Override
    @Before
    public void prepare() {
        try {
            Page page = DispatcherLogic.loadPage(getClass().getResourceAsStream("page.xml"));
            page.init();
            crudConfiguration = DispatcherLogic.loadConfiguration
                    (getClass().getResourceAsStream("configuration.xml"), CrudConfiguration.class);
            crudConfiguration.init(application);
            pageInstance = new PageInstance(null, null, application, page);
            pageInstance.setActionBean(this);
            pageInstance.setActionClass(getClass());
            pageInstance.setConfiguration(crudConfiguration);
            if(!StringUtils.isBlank(groupId)) {
                pageInstance.getParameters().add(groupId);
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
    protected boolean createValidate(Object object) {
        Group group = (Group) object;
        group.setCreationDate(new Timestamp(System.currentTimeMillis()));
        group.setGroupId(RandomUtil.createRandomId(20));
        group.setCreatorId((String) SecurityUtils.getSubject().getPrincipal());
        return true;
    }

    @Override
    protected void setupForm(Mode mode) {
        super.setupForm(mode);
        Group group = (Group) object;
        if(group != null) {
            String contextPath = getContext().getRequest().getContextPath();
            String usersPath = contextPath + UserAdminAction.BASE_PATH + "/";
            form.findFieldByPropertyName("creatorId").setHref(usersPath + group.getCreatorId());
        }
    }

    @Override
    protected Resolution getEditView() {
        return new ForwardResolution("/layouts/admin/groups/groupEdit.jsp");
    }

    @Override
    protected Resolution getBulkEditView() {
        return new ForwardResolution("/layouts/admin/groups/groupBulkEdit.jsp");
    }

    @Override
    protected Resolution getCreateView() {
        return new ForwardResolution("/layouts/admin/groups/groupCreate.jsp");
    }

    @Override
    protected Resolution getReadView() {
        return forwardToPortletPage("/layouts/admin/groups/groupRead.jsp");
    }

    @Override
    protected Resolution getSearchView() {
        return forwardToPortletPage("/layouts/admin/groups/groupSearch.jsp");
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

    public String getActionPath() {
        return dispatch.getOriginalPath();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
