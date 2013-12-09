package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.buttons.GuardType
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.buttons.annotations.Buttons
import com.manydesigns.portofino.buttons.annotations.Guard
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import com.manydesigns.portofino.tt.TtUtils
import net.sourceforge.stripes.action.Before
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class MyCrudAction extends CrudAction {

    Serializable project;

    @Before
    public void prepareProject() {
        project = ElementsThreadLocals.getOgnlContext().get("project");
    }


    //**************************************************************************
    // Role checking
    //**************************************************************************

    public boolean isManager() {
        return TtUtils.principalHasProjectRole(project, TtUtils.ROLE_MANAGER);
    }

    //**************************************************************************
    // Create customizations
    //**************************************************************************

    @Override
    @Button(list = "crud-search", key = "create.new", order = 1d, type = Button.TYPE_SUCCESS,
            icon = "icon-plus icon-white", group = "crud")
    @Guard(test="isManager()", type=GuardType.VISIBLE)
    Resolution create() {
        return super.create()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    @Button(list = "crud-create", key = "save", order = 1d, type = Button.TYPE_PRIMARY)
    @Guard(test="isManager()", type=GuardType.VISIBLE)
    Resolution save() {
        return super.save()    //To change body of overridden methods use File | Settings | File Templates.
    }


    protected void createSetup(Object object) {
        object.project = project.id;
    }

    //**************************************************************************
    // Read customizations
    //**************************************************************************

    protected Resolution getReadView() {
        return new ForwardResolution("/jsp/projects/components/component-read.jsp");
    }

    //**************************************************************************
    // Search customizations
    //**************************************************************************

    protected Resolution getSearchView() {
        return new ForwardResolution("/jsp/projects/components/components-search.jsp");
    }

    //**************************************************************************
    // Edit customizations
    //**************************************************************************

    @Override
    @Buttons([
        @Button(list = "pageHeaderButtons", key = "edit.version.details", order = 1d, icon = "icon-edit", group = "crud"),
        @Button(list = "crud-read-default-button", key = "search")
    ])
    @Guard(test="object != null && isManager()", type=GuardType.VISIBLE)
    Resolution edit() {
        return super.edit()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    @Button(list = "crud-edit", key = "update", order = 1d, type = Button.TYPE_PRIMARY)
    @Guard(test="isManager()", type=GuardType.VISIBLE)
    Resolution update() {
        return super.update()    //To change body of overridden methods use File | Settings | File Templates.
    }

    //**************************************************************************
    // Delete customizations
    //**************************************************************************

    @Button(list = "pageHeaderButtons", key = "delete", order = 2d, icon = Button.ICON_TRASH, group = "crud")
    @Guard(test = "object != null && isManager()", type = GuardType.VISIBLE)
    public Resolution delete() {
        return super.delete();
    }

    //**************************************************************************
    // Bulk edit customizations
    //**************************************************************************

    Resolution bulkEdit() {
        throw new UnsupportedOperationException("Bulk operations not supported on components");
    }

    Resolution bulkUpdate() {
        throw new UnsupportedOperationException("Bulk operations not supported on components");
    }

    //**************************************************************************
    // Bulk delete customizations
    //**************************************************************************

    public Resolution bulkDelete() {
        throw new UnsupportedOperationException("Bulk operations not supported on components");
    }


}