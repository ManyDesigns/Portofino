package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.portofino.tt.TtUtils

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.Mode
import com.manydesigns.elements.forms.FormBuilder
import com.manydesigns.elements.forms.TableForm
import com.manydesigns.elements.forms.TableFormBuilder
import com.manydesigns.elements.messages.SessionMessages
import com.manydesigns.elements.text.OgnlTextFormat
import com.manydesigns.elements.util.Util
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
    Object old;

    @Before
    public void prepareProject() {
        project = ElementsThreadLocals.getOgnlContext().get("project");
    }

    //**************************************************************************
    // Read customizations
    //**************************************************************************

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
            )
        }
        return formBuilder;
    }

    @Override
    protected Resolution getReadView() {
        String createdByUrl = "/users/$object.created_by";
        form.findFieldByPropertyName("created_by").href = Util.getAbsoluteUrl(createdByUrl);

        if (object.assignee != null) {
            String assigneeUrl = "/users/$object.assignee";
            form.findFieldByPropertyName("assignee").href = Util.getAbsoluteUrl(assigneeUrl);
        }

        if (object.fix_version != null) {
            String fixVersionUrl = "/projects/$object.project/versions/$object.fix_version";
            form.findFieldByPropertyName("fix_version").href = Util.getAbsoluteUrl(fixVersionUrl);
        }

        if (object.affected_version != null) {
            String affectedVersionUrl = "/projects/$object.project/versions/$object.affected_version";
            form.findFieldByPropertyName("affected_version").href = Util.getAbsoluteUrl(affectedVersionUrl);

        }

        return new ForwardResolution("/jsp/projects/tickets/ticket-read.jsp")
    }

    //**************************************************************************
    // Search customizations
    //**************************************************************************

    @Override
    protected TableForm buildTableForm(TableFormBuilder tableFormBuilder) {
        OgnlTextFormat titleHrefFormat = new OgnlTextFormat("/projects/%{project}/tickets/%{project}/%{n}");
        titleHrefFormat.url = true;
        tableFormBuilder.configHrefTextFormat("title", titleHrefFormat)

        OgnlTextFormat versionHrefFormat = new OgnlTextFormat("/projects/%{project}/versions/%{fix_version}");
        versionHrefFormat.url = true;
        tableFormBuilder.configHrefTextFormat("fix_version", versionHrefFormat)

        return super.buildTableForm(tableFormBuilder);
    }


    //**************************************************************************
    // Create customizations
    //**************************************************************************
    @Override
    @Button(list = "crud-search", key = "create.new", order = 1d, type = Button.TYPE_SUCCESS,
            icon = "icon-plus icon-white", group = "crud")
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_CREATE)
    @Guard(test="canCreate()", type=GuardType.VISIBLE)
    Resolution create() {
        return super.create()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    @Button(list = "crud-create", key = "save", order = 1d, type = Button.TYPE_PRIMARY)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_CREATE)
    @Guard(test="canCreate()", type=GuardType.VISIBLE)
    Resolution save() {
        return super.save()    //To change body of overridden methods use File | Settings | File Templates.
    }

    public boolean canCreate() {
        return TtUtils.principalHasProjectRole(project, TtUtils.ROLE_CONTRIBUTOR);
    }


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
        TtUtils.addActivity(session, object, principal.id, now, TtUtils.ACTIVITY_TYPE_TICKET_CREATED, null);
    }

    @Override
    protected Resolution getSuccessfulSaveView() {
        return new RedirectResolution(context.actionPath + "/" + object.project + "/" + object.n);
    }


    //**************************************************************************
    // Edit customizations
    //**************************************************************************

    protected void editSetup(Object object) {
        old = object.clone();
    }

    protected boolean editValidate(Object object) {
        Date now = new Date();
        object.date_updated = now;
        return true;
    }

    protected void editPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        String message = TtUtils.createDiffMessage(classAccessor, old, object);
        if (message != null) {
            Date now = new Date();
            TtUtils.addActivity(session, object, principal.id, now, TtUtils.ACTIVITY_TYPE_TICKET_UPDATED, message);
        }
    }

    //**************************************************************************
    // Delete customizations
    //**************************************************************************


    @Override
    public Resolution delete() {
        throw new UnsupportedOperationException();
    }

    //**************************************************************************
    // Assign to me
    //**************************************************************************


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
            return new RedirectResolution(context.actionPath);
        }
        Date now = new Date();
        Session session = persistence.getSession("tt");
        TtUtils.addActivity(session, object, principal.id, now, TtUtils.ACTIVITY_TYPE_TICKET_UPDATED, message);
        session.getTransaction().commit();
        SessionMessages.addInfoMessage("Ticket assigned to you");
        return new RedirectResolution(context.actionPath);
    }

    public boolean canAssignToMe() {
        Object principal = SecurityUtils.subject.principal;
        return object.assignee != principal.id;
    }

    //**************************************************************************
    // Start work
    //**************************************************************************


    @Button(list = "ticket-workflow1", key = "tt.start.work", order = 2d, icon = "icon-play icon-white",
            group = "wf", type = Button.TYPE_INFO)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canStartWork()", type=GuardType.VISIBLE)
    public Resolution startWork() {
        changeState(TtUtils.TICKET_STATE_WORK_IN_PROGRESS);
        SessionMessages.addInfoMessage("Started work");
        return new RedirectResolution(context.actionPath)
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

    //**************************************************************************
    // Resolve
    //**************************************************************************

    @Button(list = "ticket-workflow2", key = "tt.resolve", order = 2d, icon = "icon-thumbs-up icon-white",
            group = "wf", type = Button.TYPE_PRIMARY)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canResolve()", type=GuardType.VISIBLE)
    public Resolution resolve() {
        changeState(TtUtils.TICKET_STATE_RESOLVED);
        SessionMessages.addInfoMessage("Ticket resolved");
        return new RedirectResolution(context.actionPath);
    }

    public boolean canResolve() {
        return object.state != TtUtils.TICKET_STATE_RESOLVED;
    }

    //**************************************************************************
    // Close
    //**************************************************************************


    @Button(list = "ticket-workflow2", key = "tt.close", order = 3d, icon = "icon-thumbs-up icon-white",
            group = "wf", type = Button.TYPE_PRIMARY)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canClose()", type=GuardType.VISIBLE)
    public Resolution close() {
        changeState(TtUtils.TICKET_STATE_CLOSED);
        SessionMessages.addInfoMessage("Ticket closed");
        return new RedirectResolution(context.actionPath);
    }

    public boolean canClose() {
        return object.state != TtUtils.TICKET_STATE_CLOSED;
    }

    //**************************************************************************
    // Reopen
    //**************************************************************************


    @Button(list = "ticket-workflow1", key = "tt.reopen", order = 1d, icon = "icon-repeat icon-white",
            group = "wf", type = Button.TYPE_INFO)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canReopen()", type=GuardType.VISIBLE)
    public Resolution reopen() {
        changeState(TtUtils.TICKET_STATE_OPEN);
        SessionMessages.addInfoMessage("Ticket reopened");
        return new RedirectResolution(context.actionPath);
    }

    public boolean canReopen() {
        return object.state != TtUtils.TICKET_STATE_OPEN;
    }


}