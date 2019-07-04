package com.manydesigns.portofino.resourceactions.crud

import javax.servlet.*

import com.manydesigns.elements.forms.*
import com.manydesigns.elements.messages.*
import com.manydesigns.elements.reflection.*
import com.manydesigns.portofino.*
import com.manydesigns.portofino.operations.*
import com.manydesigns.portofino.operations.annotations.*
import com.manydesigns.portofino.dispatcher.*
import com.manydesigns.portofino.model.database.*
import com.manydesigns.portofino.resourceactions.*
import com.manydesigns.portofino.security.*
import com.manydesigns.portofino.shiro.*

import org.apache.shiro.*
import org.hibernate.*
import org.hibernate.criterion.*

import com.manydesigns.portofino.resourceactions.crud.*

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class MyCrudAction extends CrudAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Write your code here

    //**************************************************************************
    // Extension hooks
    //**************************************************************************

    public boolean isCreateEnabled() {
        true
    }
    
    protected void createSetup(Object object) {}

    protected boolean createValidate(Object object) {
        true
    }

    protected void createPostProcess(Object object) {}


    public boolean isEditEnabled() {
        true
    }

    protected void editSetup(Object object) {}

    protected boolean editValidate(Object object) {
        true
    }

    protected void editPostProcess(Object object) {}


    public boolean isDeleteEnabled() {
        true
    }

    protected boolean deleteValidate(Object object) {
        true
    }

    protected void deletePostProcess(Object object) {}


}
