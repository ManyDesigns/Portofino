package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.portofino.tt.TtUtils

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.buttons.GuardType
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.buttons.annotations.Buttons
import com.manydesigns.portofino.buttons.annotations.Guard
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import net.sourceforge.stripes.action.Before
import net.sourceforge.stripes.action.Resolution
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class TicketAttachementsCrudAction extends CrudAction {

    Object project;
    Object ticket;

    @Before
    public void prepareProject() {
        project = ElementsThreadLocals.getOgnlContext().get("project");
        ticket = ElementsThreadLocals.getOgnlContext().get("ticket");
    }

    //**************************************************************************
    // Role checking
    //**************************************************************************

    public boolean isContributor() {
        return TtUtils.principalHasProjectRole(project, TtUtils.ROLE_CONTRIBUTOR);
    }

    public boolean isEditor() {
        return TtUtils.principalHasProjectRole(project, TtUtils.ROLE_EDITOR);
    }

    public boolean canEditTicket() {
        return isEditor() || (myTicket() && isContributor());
    }

    public boolean myTicket() {
        Subject subject = SecurityUtils.subject;
        Object principal = subject.principal;
        if (principal == null) {
            return false;
        }
        return ticket.created_by == principal.id;
    }

    //**************************************************************************
    // Create customizations
    //**************************************************************************

    @Override
    @Button(list = "crud-search", key = "create.new", order = 1d, type = Button.TYPE_SUCCESS,
            icon = "glyphicon-plus white")
    @Guard(test="canEditTicket()", type=GuardType.VISIBLE)
    Resolution create() {
        return super.create()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    @Button(list = "crud-create", key = "save", order = 1d, type = Button.TYPE_PRIMARY)
    @Guard(test="canEditTicket()", type=GuardType.VISIBLE)
    Resolution save() {
        return super.save()    //To change body of overridden methods use File | Settings | File Templates.
    }


    protected void createSetup(Object object) {
        object.project = ticket.project;
        object.n = ticket.n;
    }

    protected void createPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_ATTACHMENT_CREATED,
                null,
                null,
                null,
                ticket,
                object,
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
        @Button(list = "crud-read", key = "edit", order = 1d, icon = "glyphicon-edit white",
                group = "crud", type = Button.TYPE_SUCCESS),
        @Button(list = "crud-read-default-button", key = "search")
    ])
    @Guard(test="canEditTicket()", type=GuardType.VISIBLE)
    Resolution edit() {
        return super.edit()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    @Button(list = "crud-edit", key = "update", order = 1d, type = Button.TYPE_PRIMARY)
    @Guard(test="canEditTicket()", type=GuardType.VISIBLE)
    Resolution update() {
        return super.update()    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected void editPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_ATTACHMENT_UPDATED,
                null,
                null,
                null,
                ticket,
                object,
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
    // Delete customizations
    //**************************************************************************

    @Button(list = "crud-read", key = "delete", order = 2d, icon = Button.ICON_TRASH)
    @Guard(test = "canEditTicket()", type = GuardType.VISIBLE)
    public Resolution delete() {
        return super.delete();
    }

    protected void deletePostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_ATTACHMENT_DELETED,
                null,
                null,
                null,
                ticket,
                object,
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

    @Button(list = "crud-bulk", key = "edit", order = 2d, icon = Button.ICON_EDIT)
    @Guard(test = "isBulkOperationsEnabled() && canEditTicket()", type = GuardType.VISIBLE)
    Resolution bulkEdit() {
        return super.bulkEdit()
    }

    @Button(list = "crud-bulk", key = "update", order = 1d, type = Button.TYPE_PRIMARY)
    @Guard(test = "canEditTicket()", type = GuardType.VISIBLE)
    Resolution bulkUpdate() {
        return super.bulkUpdate()
    }

    //**************************************************************************
    // Bulk delete customizations
    //**************************************************************************

    @Button(list = "crud-search", key = "delete", order = 3d, icon = Button.ICON_TRASH)
    @Guard(test = "isBulkOperationsEnabled() && canEditTicket()", type = GuardType.VISIBLE)
    public Resolution bulkDelete() {
        return super.bulkDelete()
    }

}