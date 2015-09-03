package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.portofino.tt.TtUtils

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.Mode
import com.manydesigns.elements.fields.Field
import com.manydesigns.elements.forms.Form
import com.manydesigns.elements.forms.FormBuilder
import com.manydesigns.elements.forms.TableForm
import com.manydesigns.elements.forms.TableFormBuilder
import com.manydesigns.elements.messages.SessionMessages
import com.manydesigns.elements.text.OgnlTextFormat
import com.manydesigns.elements.util.Util
import com.manydesigns.portofino.buttons.GuardType
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.buttons.annotations.Buttons
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
import org.apache.shiro.subject.Subject
import org.hibernate.LockOptions

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
    // Role checking
    //**************************************************************************

    public boolean isContributor() {
        return TtUtils.principalHasProjectRole(project, TtUtils.ROLE_CONTRIBUTOR);
    }

    public boolean isEditor() {
        return TtUtils.principalHasProjectRole(project, TtUtils.ROLE_EDITOR);
    }

    public boolean isManager() {
        return TtUtils.principalHasProjectRole(project, TtUtils.ROLE_MANAGER);
    }

    //**************************************************************************
    // Read customizations
    //**************************************************************************

    public static final String[] VIEW_FIELDS = [
            "affected_version",
                    "created_by",
                    "assignee",
                    "fix_version",
                    "resolution",
                    "created",
                    "last_updated",
    ];

    @Override
    protected FormBuilder configureFormBuilder(FormBuilder formBuilder, Mode mode) {
        formBuilder.configPrefix(prefix).configMode(mode);
        configureFormSelectionProviders(formBuilder);

        if (mode == Mode.VIEW) {
            def viewFields = [];
            for (String fieldName : VIEW_FIELDS) {
                Object fieldValue = object.get(fieldName);
                if (fieldValue != null) {
                    viewFields.add(fieldName);
                }
            }
            formBuilder.configFields((String[])viewFields);
        }
        return formBuilder;
    }

    public static final String[] WORKFLOW_FIELDS = [
            "state", "assignee", "fix_version", "resolution"
    ];

    @Override
    protected Form buildForm(FormBuilder formBuilder) {
        Form result = super.buildForm(formBuilder);
        if (isEditor()) {
            for (String fieldName : WORKFLOW_FIELDS) {
                Field field = result.findFieldByPropertyName(fieldName);
                if (field != null) {
                    field.setUpdatable(true);
                    field.setInsertable(true);
                }
            }
        }
        return result;
    }



    @Override
    protected Resolution getReadView() {
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
        OgnlTextFormat titleHrefFormat = new OgnlTextFormat(
                appendSearchStringParamIfNecessary("/projects/%{project}/tickets/%{project}/%{n}"));
        titleHrefFormat.url = true;
        tableFormBuilder.configHrefTextFormat("title", titleHrefFormat);

        OgnlTextFormat fixVersionHrefFormat =
                new OgnlTextFormat("/projects/%{project}/versions/%{fix_version}");
        fixVersionHrefFormat.url = true;
        tableFormBuilder.configHrefTextFormat("fix_version", fixVersionHrefFormat)

        OgnlTextFormat affectedVersionHrefFormat =
                new OgnlTextFormat("/projects/%{project}/versions/%{affected_version}");
        affectedVersionHrefFormat.url = true;
        tableFormBuilder.configHrefTextFormat("affected_version", affectedVersionHrefFormat)

        return super.buildTableForm(tableFormBuilder);
    }


    //**************************************************************************
    // Create customizations
    //**************************************************************************
    @Override
    @Button(list = "crud-search", key = "create.new", order = 1d, type = Button.TYPE_SUCCESS,
            icon = "glyphicon-plus white")
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_CREATE)
    @Guard(test="isContributor()", type=GuardType.VISIBLE)
    Resolution create() {
        return super.create()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    @Button(list = "crud-create", key = "save", order = 1d, type = Button.TYPE_PRIMARY)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_CREATE)
    @Guard(test="isContributor()", type=GuardType.VISIBLE)
    Resolution save() {
        return super.save()    //To change body of overridden methods use File | Settings | File Templates.
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
        object.created = now;
        object.last_updated = now;

        session.buildLockRequest(LockOptions.UPGRADE).lock("project", project);
        long number = project.last_ticket + 1L;
        project.last_ticket = number;

        object.n = number;

        return true;
    }

    @Override
    protected void createPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_TICKET_CREATED,
                null,
                null,
                null,
                object,
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

    @Override
    protected Resolution getSuccessfulSaveView() {
        return new RedirectResolution(context.actionPath + "/" + object.project + "/" + object.n);
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
    @Guard(test="canEdit()", type=GuardType.VISIBLE)
    Resolution edit() {
        return super.edit()    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    @Button(list = "crud-edit", key = "update", order = 1d, type = Button.TYPE_PRIMARY)
    @Guard(test="canEdit()", type=GuardType.VISIBLE)
    Resolution update() {
        return super.update()    //To change body of overridden methods use File | Settings | File Templates.
    }

    public boolean canEdit() {
        return isEditor() || (myTicket() && isContributor());
    }

    public boolean myTicket() {
        Subject subject = SecurityUtils.subject;
        Object principal = subject.principal;
        if (principal == null) {
            return false;
        }
        return object.created_by == principal.id;
    }


    @Override
    protected boolean editValidate(Object object) {
        Date now = new Date();
        object.last_updated = now;
        return true;
    }

    @Override
    protected void editSetup(Object object) {
        old = object.clone();
    }

    @Override
    protected void editPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Form newForm = form;
        form = buildForm(createFormBuilder());
        form.readFromObject(old);
        String message = TtUtils.createDiffMessage(form, newForm);
        if (message != null) {
            Date now = new Date();
            TtUtils.addActivity(session,
                    principal,
                    now,
                    TtUtils.ACTIVITY_TYPE_TICKET_UPDATED,
                    message,
                    null,
                    null,
                    object,
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
    // Bulk edit customizations
    //**************************************************************************
    Resolution bulkEdit() {
        throw new UnsupportedOperationException("Bulk operations not supported on tickets");
    }

    Resolution bulkUpdate() {
        throw new UnsupportedOperationException("Bulk operations not supported on tickets");
    }

    //**************************************************************************
    // Delete customizations
    //**************************************************************************


    @Button(list = "crud-read", key = "delete", order = 2d, icon = Button.ICON_TRASH)
    @Guard(test = "isManager()", type = GuardType.VISIBLE)
    public Resolution delete() {
        return super.delete();
    }

    @Override
    protected void deletePostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_TICKET_DELETED,
                null,
                null,
                null,
                object,
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
    // Bulk delete customizations
    //**************************************************************************

    public Resolution bulkDelete() {
        throw new UnsupportedOperationException("Bulk operations not supported on tickets");
    }

    //**************************************************************************
    // Assign to me
    //**************************************************************************


    @Button(list = "assign", key = "tt.assign.to.me", order = 2d, icon = "glyphicon-hand-right",
            group = "crud", type = Button.TYPE_DEFAULT)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canAssignToMe()", type=GuardType.VISIBLE)
    public Resolution assignToMe() {
        Object principal = SecurityUtils.subject.principal;
        setupForm(Mode.EDIT);
        editSetup(object);
        object.assignee = principal.id;
        form.readFromObject(object);
        editValidate(object);
        doUpdate(object);
        editPostProcess(object);
        session.getTransaction().commit();
        SessionMessages.addInfoMessage("Ticket assigned to you");
        return new RedirectResolution(context.actionPath);
    }

    public boolean canAssignToMe() {
        Subject subject = SecurityUtils.subject
        Object principal = subject.principal;
        if (principal == null) {
            return false;
        }
        if (object.assignee == principal.id) {
            return false;
        }
        return isEditor();
    }

    //**************************************************************************
    // Start work
    //**************************************************************************


    @Button(list = "ticket-workflow1", key = "tt.start.work", order = 2d, icon = "glyphicon-play white",
            group = "wf", type = Button.TYPE_INFO)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canStartWork()", type=GuardType.VISIBLE)
    public Resolution startWork() {
        changeState(TtUtils.TICKET_STATE_WORK_IN_PROGRESS);
        SessionMessages.addInfoMessage("Started work");
        return new RedirectResolution(context.actionPath)
    }

    public boolean canStartWork() {
        if (object.state == TtUtils.TICKET_STATE_WORK_IN_PROGRESS) {
            return false;
        }
        return isEditor();
    }

    private void changeState(long newState) {
        setupForm(Mode.EDIT);
        editSetup(object);
        object.state = newState;
        form.readFromObject(object);
        editValidate(object);
        doUpdate(object);
        editPostProcess(object);
        session.getTransaction().commit();
    }

    //**************************************************************************
    // Resolve
    //**************************************************************************

    @Button(list = "ticket-workflow2", key = "tt.resolve", order = 2d, icon = "glyphicon-thumbs-up white",
            group = "wf", type = Button.TYPE_PRIMARY)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canResolve()", type=GuardType.VISIBLE)
    public Resolution resolve() {
        changeState(TtUtils.TICKET_STATE_RESOLVED);
        SessionMessages.addInfoMessage("Ticket resolved");
        return new RedirectResolution(context.actionPath);
    }

    public boolean canResolve() {
        if (object.state == TtUtils.TICKET_STATE_RESOLVED) {
            return false;
        }
        return isEditor();
    }

    //**************************************************************************
    // Close
    //**************************************************************************


    @Button(list = "ticket-workflow2", key = "tt.close", order = 3d, icon = "glyphicon-thumbs-up white",
            group = "wf", type = Button.TYPE_PRIMARY)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canClose()", type=GuardType.VISIBLE)
    public Resolution close() {
        changeState(TtUtils.TICKET_STATE_CLOSED);
        SessionMessages.addInfoMessage("Ticket closed");
        return new RedirectResolution(context.actionPath);
    }

    public boolean canClose() {
        if (object.state == TtUtils.TICKET_STATE_CLOSED) {
            return false;
        }
        return isEditor();
    }

    //**************************************************************************
    // Reopen
    //**************************************************************************


    @Button(list = "ticket-workflow1", key = "tt.reopen", order = 1d, icon = "glyphicon-repeat white",
            group = "wf", type = Button.TYPE_INFO)
    @RequiresPermissions(permissions = AbstractCrudAction.PERMISSION_EDIT)
    @Guard(test="canReopen()", type=GuardType.VISIBLE)
    public Resolution reopen() {
        changeState(TtUtils.TICKET_STATE_OPEN);
        SessionMessages.addInfoMessage("Ticket reopened");
        return new RedirectResolution(context.actionPath);
    }

    public boolean canReopen() {
        if (object.state == TtUtils.TICKET_STATE_OPEN) {
            return false;
        }
        return isEditor();
    }

}