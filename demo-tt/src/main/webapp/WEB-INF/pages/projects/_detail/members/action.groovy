package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.forms.Form
import com.manydesigns.portofino.buttons.GuardType
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.buttons.annotations.Buttons
import com.manydesigns.portofino.buttons.annotations.Guard
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import com.manydesigns.portofino.tt.TtUtils
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import org.apache.shiro.SecurityUtils

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class ProjectMembersAction extends CrudAction {

    Serializable project;
    Object old;

    @Override
    Resolution preparePage() {
        project = ElementsThreadLocals.getOgnlContext().get("project");
        if (!isViewer()) {
            return new ForwardResolution("/jsp/projects/members-not-available.jsp")
        }
        return super.preparePage();
    }

    //**************************************************************************
    // Role checking
    //**************************************************************************

    public boolean isViewer() {
        return TtUtils.principalHasProjectRole(project, TtUtils.ROLE_VIEWER);
    }

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
        return super.create()
    }

    @Override
    @Button(list = "crud-create", key = "save", order = 1d, type = Button.TYPE_PRIMARY)
    @Guard(test="isManager()", type=GuardType.VISIBLE)
    Resolution save() {
        return super.save() 
    }

    protected void createSetup(Object object) {
        object.project = project.id;
        object.notifications = false;
    }

    protected void createPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Object user2 = session.load("users", object.user_);
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_MEMBER_CREATED,
                null,
                user2,
                project,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    //**************************************************************************
    // Edit customizations
    //**************************************************************************

    @Override
    @Buttons([
        @Button(list = "crud-read", key = "edit", order = 1d, icon = "icon-edit icon-white",
                group = "crud", type = Button.TYPE_SUCCESS),
        @Button(list = "crud-read-default-button", key = "search")
    ])
    @Guard(test="isManager()", type=GuardType.VISIBLE)
    Resolution edit() {
        return super.edit()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    @Button(list = "crud-edit", key = "update", order = 1d, type = Button.TYPE_PRIMARY)
    @Guard(test="isManager()", type=GuardType.VISIBLE)
    Resolution update() {
        return super.update()    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected void editSetup(Object object) {
        old = object.clone();
    }

    protected void editPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Form newForm = form;
        form = buildForm(formBuilder);
        form.readFromObject(old);
        String message = TtUtils.createDiffMessage(form, newForm);
        if (message != null) {
            Date now = new Date();
            TtUtils.addActivity(session,
                    principal,
                    now,
                    TtUtils.ACTIVITY_TYPE_MEMBER_UPDATED,
                    message,
                    object.fk_member_user,
                    project,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }


    //**************************************************************************
    // Delete customizations
    //**************************************************************************

    @Override
    @Button(list = "crud-read", key = "delete", order = 2d, icon = Button.ICON_TRASH, group = "crud")
    @Guard(test = "isManager()", type = GuardType.VISIBLE)
    public Resolution delete() {
        return super.delete();
    }

    protected void deletePostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_MEMBER_DELETED,
                null,
                object.fk_member_user,
                project,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    //**************************************************************************
    // Bulk edit customizations
    //**************************************************************************

    @Override
    @Button(list = "crud-search", key = "edit", order = 2d, icon = Button.ICON_EDIT, group = "crud")
    @Guard(test = "isBulkOperationsEnabled() && isManager()", type = GuardType.VISIBLE)
    Resolution bulkEdit() {
        return super.bulkEdit();
    }

    @Button(list = "crud-bulk-edit", key = "update", order = 1d, type = Button.TYPE_PRIMARY)
    @Guard(test = "isManager()", type = GuardType.VISIBLE)
    Resolution bulkUpdate() {
        return super.bulkUpdate();
    }

    //**************************************************************************
    // Bulk delete customizations
    //**************************************************************************

    @Button(list = "crud-search", key = "delete", order = 3d, icon = Button.ICON_TRASH, group = "crud")
    @Guard(test = "isBulkOperationsEnabled() && isManager()", type = GuardType.VISIBLE)
    public Resolution bulkDelete() {
        return super.bulkDelete();
    }


}