package com.manydesigns.portofino.resourceactions.crud

import com.manydesigns.portofino.resourceactions.crud.CrudAction
import com.manydesigns.portofino.tt.TtUtils

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.operations.GuardType

import com.manydesigns.portofino.operations.annotations.Guard
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import net.sourceforge.stripes.action.Before
import net.sourceforge.stripes.action.Resolution
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class TicketAffectedComponentsCrudAction extends CrudAction {

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

    @Guard(test="canEditTicket()", type=GuardType.VISIBLE)
    Resolution create() {
        return super.create()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override

    @Guard(test="canEditTicket()", type=GuardType.VISIBLE)
    Resolution save() {
        return super.save()    //To change body of overridden methods use File | Settings | File Templates.
    }


    protected void createSetup(Object object) {
        object.project = ticket.project;
        object.n = ticket.n;
    }

    protected boolean createValidate(Object object) {
        return true;
    }

    protected void createPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_AFFECTED_COMPONENT_CREATED,
                null,
                null,
                null,
                ticket,
                null,
                null,
                object,
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

    @Guard(test="canEditTicket()", type=GuardType.VISIBLE)
    Resolution edit() {
        return super.edit()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override

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
                TtUtils.ACTIVITY_TYPE_AFFECTED_COMPONENT_UPDATED,
                null,
                null,
                null,
                ticket,
                null,
                null,
                object,
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
                TtUtils.ACTIVITY_TYPE_AFFECTED_COMPONENT_DELETED,
                null,
                null,
                null,
                ticket,
                null,
                null,
                object,
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

    Resolution bulkEdit() {
        throw new UnsupportedOperationException("Bulk operations not supported on attachments");
    }

    Resolution bulkUpdate() {
        throw new UnsupportedOperationException("Bulk operations not supported on attachments");
    }

    //**************************************************************************
    // Bulk delete customizations
    //**************************************************************************


    @Guard(test = "isBulkOperationsEnabled() && canEditTicket()", type = GuardType.VISIBLE)
    public Resolution bulkDelete() {
        return super.bulkDelete()
    }

}
