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

import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.servlet.ServletUtils;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.actions.CrudAction;
import com.manydesigns.portofino.actions.RequestAttributes;
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

import java.sql.Timestamp;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@RequiresAdministrator
@UrlBinding(GroupAdminAction.ACTION_PATH)
public class GroupAdminAction extends CrudAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static final String ACTION_PATH = "/admin/groups.action";

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
        String originalPath = ServletUtils.getOriginalPath(context.getRequest());
        dispatch = new Dispatch(context.getRequest(), originalPath, originalPath, rootPageInstance, pageInstance);
        context.getRequest().setAttribute(RequestAttributes.DISPATCH, dispatch);
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
    public Resolution delete() {
        super.delete();
        return new RedirectResolution(ACTION_PATH);
    }

    @Override
    public Resolution bulkDelete() {
        super.bulkDelete();
        return new RedirectResolution(ACTION_PATH);
    }

    public Resolution bulkEdit() {
        Resolution res = super.bulkEdit();
        if (selection.length == 1) {
            String url = dispatch.getOriginalPath();
            return new RedirectResolution(url)
                    .addParameter("pk", pk)
                    .addParameter("cancelReturnUrl", cancelReturnUrl)
                    .addParameter("edit");
        } else {
            return res;
        }
    }

    @Override
    protected String getReadLinkExpression() {
        StringBuilder sb = new StringBuilder();
        sb.append(dispatch.getOriginalPath());
        sb.append("?pk=");
        boolean first = true;

        for (PropertyAccessor property : classAccessor.getKeyProperties()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("%{");
            sb.append(property.getName());
            sb.append("}");
        }
        if (searchString != null) {
            sb.append("&searchString=");
            sb.append(Util.urlencode(searchString));
        }
        return sb.toString();
    }

    @Override
    protected Resolution forwardToPortletPage(String pageJsp) {
        return new ForwardResolution(pageJsp);
    }

    //**************************************************************************
    // Delete
    //**************************************************************************
/*
    public String delete() {
        Group pkGrp = new Group(Long.parseLong(pk));
        Group aGroup = (Group) application.getObjectByPk(UserUtils.GROUPTABLE, pkGrp);
        aGroup.setDeletionDate(new Timestamp(System.currentTimeMillis()));
        application.saveObject(UserUtils.GROUPTABLE, aGroup);
        String databaseName = model.findTableByQualifiedName(UserUtils.GROUPTABLE)
                .getDatabaseName();
        application.commit(databaseName);
        SessionMessages.addInfoMessage("DELETE avvenuto con successo");
        return PortofinoAction.RETURN_TO_READ;
    }

    public String bulkDelete() {        
        if (selection == null) {
            SessionMessages.addWarningMessage(
                    "DELETE non avvenuto: nessun oggetto selezionato");
            return PortofinoAction.CANCEL;
        }
        for (String current : selection) {
            Group pkGrp = new Group(new Long(current));
            Group aGroup = (Group) application
                    .getObjectByPk(UserUtils.GROUPTABLE, pkGrp);
            aGroup.setDeletionDate(new Timestamp(System.currentTimeMillis()));
            application.saveObject(UserUtils.GROUPTABLE, aGroup);
            String databaseName = model
                    .findTableByQualifiedName(UserUtils.GROUPTABLE)
                    .getDatabaseName();
            SessionMessages.addInfoMessage("DELETE avvenuto con successo");
        }
        String databaseName = model
                .findTableByQualifiedName(UserUtils.GROUPTABLE)
                .getDatabaseName();
        application.commit(databaseName);
        SessionMessages.addInfoMessage(MessageFormat.format(
                "DELETE di {0} oggetti avvenuto con successo",
                selection.length));
        return PortofinoAction.RETURN_TO_SEARCH;
    }
*/

}
