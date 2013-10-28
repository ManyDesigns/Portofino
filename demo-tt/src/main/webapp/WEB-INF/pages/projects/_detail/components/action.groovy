package com.manydesigns.portofino.pageactions.crud

import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import net.sourceforge.stripes.action.Resolution

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class MyCrudAction extends CrudAction {

    //Automatically generated on Mon Oct 28 12:35:41 CET 2013 by ManyDesigns Portofino
    //Write your code here

    //**************************************************************************
    // Extension hooks
    //**************************************************************************

    protected void createSetup(Object object) {}

    protected boolean createValidate(Object object) {
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