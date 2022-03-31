package com.manydesigns.portofino.resourceactions.m2m

import javax.servlet.*

import com.manydesigns.elements.messages.*
import com.manydesigns.elements.reflection.*
import com.manydesigns.portofino.*
import com.manydesigns.portofino.operations.*
import com.manydesigns.portofino.operations.annotations.*
import com.manydesigns.portofino.dispatcher.*
import com.manydesigns.portofino.database.model.*
import com.manydesigns.portofino.resourceactions.*
import com.manydesigns.portofino.security.*
import com.manydesigns.portofino.shiro.*

import org.apache.shiro.*
import org.hibernate.*
import org.hibernate.criterion.*

import com.manydesigns.portofino.resourceactions.m2m.ManyToManyAction

@RequiresPermissions(level = AccessLevel.VIEW)
@SupportsPermissions(ManyToManyAction.PERMISSION_UPDATE)
class %{#generatedClassName} extends ManyToManyAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Write your code here

    //Hooks

    @Override
    protected void prepareSave(Object newRelation) {}

    //Overrides

    @Override
    protected Object saveNewRelation(Object pk, PropertyAccessor onePropertyAccessor, PropertyAccessor manyPropertyAccessor) {
        return super.saveNewRelation(pk, onePropertyAccessor, manyPropertyAccessor)
    }

    @Override
    protected void deleteRelation(Object rel) {
        super.deleteRelation(rel)
    }


}
