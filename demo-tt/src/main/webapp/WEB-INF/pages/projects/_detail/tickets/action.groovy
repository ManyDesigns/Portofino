package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.portofino.demott.TtUtils

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.Mode
import com.manydesigns.elements.forms.FormBuilder
import com.manydesigns.elements.messages.SessionMessages
import com.manydesigns.portofino.buttons.GuardType
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.buttons.annotations.Guard
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import com.manydesigns.portofino.shiro.ShiroUtils
import net.sourceforge.stripes.action.Before
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.RedirectResolution
import net.sourceforge.stripes.action.Resolution
import org.apache.shiro.SecurityUtils
import org.hibernate.LockOptions
import org.hibernate.Session

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class ProjectsTicketsAction extends CrudAction {

    Serializable project;

    @Before
    public void prepareProject() {
        project = ElementsThreadLocals.getOgnlContext().get("project");
    }

    @Override
    protected FormBuilder configureFormBuilder(FormBuilder formBuilder, Mode mode) {
        formBuilder.configPrefix(prefix).configMode(mode);
        configureFormSelectionProviders(formBuilder);

        if (mode == Mode.VIEW) {
            formBuilder.configFields(
                    "created_by",
                    "assignee",
                    "resolution",
                    "affected_version",
                    "fix_version",
                    "date_created",
                    "date_updated",
            );
        }
        return formBuilder;
    }

    //Automatically generated on Mon Oct 28 12:30:32 CET 2013 by ManyDesigns Portofino
    //Write your code here

    //**************************************************************************
    // Extension hooks
    //**************************************************************************

    protected void createSetup(Object object) {
        Object principal = ShiroUtils.getPrimaryPrincipal(SecurityUtils.getSubject());
        object.project = project.id;
        object.state = 1L;
        object.priority = 1L;
        object.created_by = principal.id;
    }

    protected boolean createValidate(Object object) {
        Date now = new Date();
        object.date_created = now;
        object.date_updated = now;

        session.buildLockRequest(LockOptions.UPGRADE).lock("project", project);
        long number = project.last_ticket + 1L;
        project.last_ticket = number;

        object.n = number;

        return true;
    }

    protected void createPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        Session session = persistence.getSession("tt");
        TtUtils.addActivity(session, object, principal.id, now, TtUtils.ACTIVITY_TYPE_TICKET_CREATED, null);
    }

    Object old;


    protected void editSetup(Object object) {
        old = object.clone();
    }

    protected boolean editValidate(Object object) {
        return true;
    }

    protected void editPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        String message = TtUtils.createDiffMessage(classAccessor, old, object);
        if (message != null) {
            Date now = new Date();
            Session session = persistence.getSession("tt");
            TtUtils.addActivity(session, object, principal.id, now, TtUtils.ACTIVITY_TYPE_TICKET_UPDATED, message);
        }
    }

    @Override
    protected Resolution getSuccessfulSaveView() {
        return new RedirectResolution(context.actualServletPath + "/" + object.project + "/" + object.n);
    }


    protected boolean deleteValidate(Object object) {
        return true;
    }

    protected void deletePostProcess(Object object) {}


    protected Resolution getBulkEditView() {
        return super.getBulkEditView();
    }

    protected Resolution getCreateView() {
        return super.getCreateView();
    }

    protected Resolution getEditView() {
        return super.getEditView();
    }

    protected Resolution getReadView() {
        return new ForwardResolution("/jsp/projects/tickets/ticket-read.jsp")
    }

    protected Resolution getSearchView() {
        return super.getSearchView();
    }

    protected Resolution getEmbeddedSearchView() {
        return super.getEmbeddedSearchView();
    }

    protected Resolution getSearchResultsPageView() {
        return super.getSearchResultsPageView()
    }

    @Override
    public Resolution delete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resolution exportReadPdf() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resolution exportReadExcel() {
        throw new UnsupportedOperationException();
    }

    // Workflow buttons

    @Button(list = "crud-read", key = "tt.assign.to.me", order = 2d, icon = "icon-hand-right",
            group = "crud", type = Button.TYPE_DEFAULT)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canAssignToMe()", type=GuardType.VISIBLE)
    public Resolution assignToMe() {
        old = object.clone();
        Object principal = SecurityUtils.subject.principal;
        object.assignee = principal.id;
        String message = TtUtils.createDiffMessage(classAccessor, old, object);
        if (message == null) {
            return new RedirectResolution(context.actualServletPath);
        }
        Date now = new Date();
        Session session = persistence.getSession("tt");
        TtUtils.addActivity(session, object, principal.id, now, TtUtils.ACTIVITY_TYPE_TICKET_UPDATED, message);
        session.getTransaction().commit();
        SessionMessages.addInfoMessage("Ticket assigned to you");
        return new RedirectResolution(context.actualServletPath);
    }

    public boolean canAssignToMe() {
        Object principal = SecurityUtils.subject.principal;
        return object.assignee != principal.id;
    }


    @Button(list = "ticket-workflow1", key = "tt.start.work", order = 2d, icon = "icon-play icon-white",
            group = "wf", type = Button.TYPE_INFO)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canStartWork()", type=GuardType.VISIBLE)
    public Resolution startWork() {
        changeState(TtUtils.TICKET_STATE_WORK_IN_PROGRESS);
        SessionMessages.addInfoMessage("Started work");
        return new RedirectResolution(context.actualServletPath)
    }

    public boolean canStartWork() {
        return object.state != TtUtils.TICKET_STATE_WORK_IN_PROGRESS;
    }

    private void changeState(long newState) {
        old = object.clone();
        Session session = persistence.getSession("tt");
        Object principal = SecurityUtils.subject.principal;
        object.state = newState;
        Date now = new Date();
        String message = TtUtils.createDiffMessage(classAccessor, old, object);
        if (message == null) {
            return;
        }
        TtUtils.addActivity(session, object, principal.id, now, TtUtils.ACTIVITY_TYPE_TICKET_UPDATED, message);
        session.getTransaction().commit();
    }

    @Button(list = "ticket-workflow2", key = "tt.resolve", order = 2d, icon = "icon-thumbs-up icon-white",
            group = "wf", type = Button.TYPE_PRIMARY)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canResolve()", type=GuardType.VISIBLE)
    public Resolution resolve() {
        changeState(TtUtils.TICKET_STATE_RESOLVED);
        SessionMessages.addInfoMessage("Ticket resolved");
        return new RedirectResolution(context.actualServletPath);
    }

    public boolean canResolve() {
        return object.state != TtUtils.TICKET_STATE_RESOLVED;
    }


    @Button(list = "ticket-workflow2", key = "tt.close", order = 3d, icon = "icon-thumbs-up icon-white",
            group = "wf", type = Button.TYPE_PRIMARY)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canClose()", type=GuardType.VISIBLE)
    public Resolution close() {
        changeState(TtUtils.TICKET_STATE_CLOSED);
        SessionMessages.addInfoMessage("Ticket closed");
        return new RedirectResolution(context.actualServletPath);
    }

    public boolean canClose() {
        return object.state != TtUtils.TICKET_STATE_CLOSED;
    }


    @Button(list = "ticket-workflow1", key = "tt.reopen", order = 1d, icon = "icon-repeat icon-white",
            group = "wf", type = Button.TYPE_INFO)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canReopen()", type=GuardType.VISIBLE)
    public Resolution reopen() {
        changeState(TtUtils.TICKET_STATE_OPEN);
        SessionMessages.addInfoMessage("Ticket reopened");
        return new RedirectResolution(context.actualServletPath);
    }

    public boolean canReopen() {
        return object.state != TtUtils.TICKET_STATE_OPEN;
    }


}