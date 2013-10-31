package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.Mode
import com.manydesigns.elements.forms.FormBuilder
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import com.manydesigns.portofino.shiro.ShiroUtils
import net.sourceforge.stripes.action.Before
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import org.apache.shiro.SecurityUtils
import org.hibernate.LockOptions

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

    protected void createPostProcess(Object object) {}


    protected void editSetup(Object object) {}

    protected boolean editValidate(Object object) {
        return true;
    }

    protected void editPostProcess(Object object) {}


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

}