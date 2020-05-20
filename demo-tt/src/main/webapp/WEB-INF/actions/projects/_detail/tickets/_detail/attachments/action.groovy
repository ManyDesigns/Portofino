package com.manydesigns.portofino.resourceactions.crud

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.resourceactions.crud.CrudAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import com.manydesigns.portofino.tt.TtUtils
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class TicketAttachementsCrudAction extends CrudAction {

    Object project;
    Object ticket;

    @Override
    void prepareForExecution() {
        super.prepareForExecution()
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
    boolean isCreateEnabled() {
        return super.isCreateEnabled() && canEditTicket()
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
    boolean isEditEnabled() {
        super.editEnabled && canEditTicket()
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

    @Override
    boolean isDeleteEnabled() {
        canEditTicket()
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

}
