package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.portofino.demott.TtUtils

import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import net.sourceforge.stripes.action.RedirectResolution
import net.sourceforge.stripes.action.Resolution
import org.apache.shiro.SecurityUtils

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class ProjectsCrudAction extends CrudAction {

    //Automatically generated on Mon Oct 28 12:15:50 CET 2013 by ManyDesigns Portofino
    //Write your code here
    @Override
    Resolution read() {
        def path = getContext().actionPath
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() -1 );
        }
        return new RedirectResolution(path + "/summary");
    }

    //**************************************************************************
    // Extension hooks
    //**************************************************************************

    protected void createSetup(Object object) {
        object.last_ticket = 0L;
    }

    protected boolean createValidate(Object object) {
        return true;
    }

    protected void createPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Map member = new HashMap();
        member.project = object.id;
        member.user = principal.id;
        member.role = TtUtils.ROLE_MANAGER;
        session.save("members", (Object)member);
    }

    protected Resolution getSuccessfulSaveView() {
        return new RedirectResolution(context.getActionPath() + "/" + object.id);
    }


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