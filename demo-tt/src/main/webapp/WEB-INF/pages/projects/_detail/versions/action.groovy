package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.portofino.tt.TtUtils

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.Mode
import com.manydesigns.elements.forms.Form
import com.manydesigns.portofino.buttons.GuardType
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.buttons.annotations.Buttons
import com.manydesigns.portofino.buttons.annotations.Guard
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import net.sourceforge.stripes.action.Before
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import org.apache.shiro.SecurityUtils

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class ProjectVersionsAction extends CrudAction {

    Serializable project;
    Object old;

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
            icon = "glyphicon-plus white")
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
        object.state = 1L;
    }

    @Override
    protected boolean createValidate(Object object) {
        Date now = new Date();
        object.created = now;
        object.last_updated = now;
        return true;
    }

    @Override
    protected void createPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_VERSION_CREATED,
                null,
                null,
                project,
                null,
                null,
                object,
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
    // Read customizations
    //**************************************************************************

    protected Resolution getReadView() {
        return new ForwardResolution("/jsp/projects/versions/version-read.jsp");
    }

    //**************************************************************************
    // Search customizations
    //**************************************************************************

    protected Resolution getSearchView() {
        return new ForwardResolution("/jsp/projects/versions/versions-search.jsp");
    }

    protected Resolution getSearchResultsPageView() {
        return new ForwardResolution("/jsp/projects/versions/versions-datatable.jsp");
    }


    //**************************************************************************
    // Edit customizations
    //**************************************************************************

    @Override
    @Buttons([
        @Button(list = "pageHeaderButtons", key = "edit.version.details", order = 1d, icon = "glyphicon-edit"),
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
        setupForm(Mode.EDIT);
        form.readFromObject(old);
        String message = TtUtils.createDiffMessage(form, newForm);
        if (message != null) {
            Date now = new Date();
            TtUtils.addActivity(session,
                    principal,
                    now,
                    TtUtils.ACTIVITY_TYPE_VERSION_UPDATED,
                    message,
                    null,
                    project,
                    null,
                    null,
                    object,
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

    @Button(list = "pageHeaderButtons", key = "delete", order = 2d, icon = Button.ICON_TRASH)
    @Guard(test = "object != null && isManager()", type = GuardType.VISIBLE)
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
                TtUtils.ACTIVITY_TYPE_VERSION_DELETED,
                null,
                null,
                project,
                null,
                null,
                object,
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

    Resolution bulkEdit() {
        throw new UnsupportedOperationException("Bulk operations not supported on versions");
    }

    Resolution bulkUpdate() {
        throw new UnsupportedOperationException("Bulk operations not supported on versions");
    }

    //**************************************************************************
    // Bulk delete customizations
    //**************************************************************************

    public Resolution bulkDelete() {
        throw new UnsupportedOperationException("Bulk operations not supported on versions");
    }



}