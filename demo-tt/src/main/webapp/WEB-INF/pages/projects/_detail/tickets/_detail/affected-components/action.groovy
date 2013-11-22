package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.demott.TtUtils
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import net.sourceforge.stripes.action.Before
import net.sourceforge.stripes.action.Resolution
import org.apache.shiro.SecurityUtils

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class MyCrudAction extends CrudAction {

    Serializable ticket;

    @Before
    public void prepareProject() {
        ticket = ElementsThreadLocals.getOgnlContext().get("ticket");
    }

    //**************************************************************************
    // Extension hooks
    //**************************************************************************

    protected void createSetup(Object object) {
        object.project = ticket.project;
        object.n = ticket.n;
    }

    protected boolean createValidate(Object object) {
        return true;
    }

    protected void createPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Object component = session.load("components", object.component);
        String message = component.title;
        Date now = new Date();
        TtUtils.addActivity(session, object, principal.id, now, TtUtils.ACTIVITY_TYPE_AFFECTED_COMPONENT_CREATED, message);
    }


    protected void editSetup(Object object) {}

    protected boolean editValidate(Object object) {
        return true;
    }

    protected void editPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        String message = object.fk_affected_component_component.title;
        Date now = new Date();
        TtUtils.addActivity(session, object, principal.id, now, TtUtils.ACTIVITY_TYPE_AFFECTED_COMPONENT_UPDATED, message);
    }


    protected boolean deleteValidate(Object object) {
        return true;
    }

    protected void deletePostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        String message = object.fk_affected_component_component.title;
        Date now = new Date();
        TtUtils.addActivity(session, object, principal.id, now, TtUtils.ACTIVITY_TYPE_AFFECTED_COMPONENT_DELETED, message);
    }


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
        return super.getReadView();
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