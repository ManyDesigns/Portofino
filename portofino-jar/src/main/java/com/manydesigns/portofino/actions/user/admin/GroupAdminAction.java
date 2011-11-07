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
import com.manydesigns.portofino.actions.CrudAction;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.breadcrumbs.Breadcrumbs;
import com.manydesigns.portofino.dispatcher.CrudPageInstance;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.pages.CrudPage;
import com.manydesigns.portofino.model.pages.crud.Crud;
import com.manydesigns.portofino.model.pages.crud.CrudProperty;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.annotations.RequiresAdministrator;
import net.sourceforge.stripes.action.*;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@RequiresAdministrator
@UrlBinding(GroupAdminAction.BASE_PATH + "/{pk}")
public class GroupAdminAction extends CrudAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static final String BASE_PATH = "/actions/admin/groups";

    @Override
    @Before
    public void prepare() {
        crudPage = new CrudPage();
        Crud crud = new Crud();
        configureCrud(crud);
        crudPage.setCrud(crud);
        crudPage.setSearchUrl("/layouts/admin/groups/groupSearch.jsp");
        crudPage.setReadUrl("/layouts/admin/groups/groupRead.jsp");
        crudPage.setEditUrl("/layouts/admin/groups/groupEdit.jsp");
        crudPage.setBulkEditUrl("/layouts/admin/groups/groupBulkEdit.jsp");
        crudPage.setCreateUrl("/layouts/admin/groups/groupCreate.jsp");
        crudPage.setFragment(BASE_PATH);
        crudPage.setTitle("Groups");
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
    protected boolean createValidate(Object object) {
        Group group = (Group) object;
        group.setCreationDate(new Timestamp(System.currentTimeMillis()));
        return true;
    }

    private void configureCrud(Crud crud) {
        crud.setTable(SecurityLogic.GROUPTABLE);
        crud.setQuery("FROM portofino_public_groups");

        crud.setSearchTitle("Groups");
        crud.setCreateTitle("Create group");
        crud.setEditTitle("Edit group");
        crud.setReadTitle("Group");

        CrudProperty property;

        property = new CrudProperty();
        property.setName("groupId");
        property.setEnabled(true);
        property.setInSummary(true);
        property.setLabel("Id");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setName("creatorId");
        property.setEnabled(true);
        property.setLabel("Creator");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setName("name");
        property.setEnabled(true);
        property.setInSummary(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        property.setSearchable(true);
        property.setLabel("Name");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setName("description");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        property.setSearchable(true);
        property.setLabel("Description");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setName("creationDate");
        property.setEnabled(true);
        property.setInSummary(true);
        property.setLabel("Creation date");
        crud.getProperties().add(property);

        property = new CrudProperty();
        property.setName("deletionDate");
        property.setEnabled(true);
        property.setInSummary(true);
        property.setLabel("Deletion date");
        crud.getProperties().add(property);
    }

    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    //**************************************************************************
    // Overrides
    //**************************************************************************

    @Override
    protected Resolution forwardToPortletPage(String pageJsp) {
        return new ForwardResolution(pageJsp);
    }

}
