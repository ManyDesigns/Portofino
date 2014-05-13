package com.manydesigns.portofino.pageactions.form

import com.manydesigns.elements.annotations.MaxIntValue
import com.manydesigns.elements.annotations.MinIntValue
import com.manydesigns.elements.annotations.RegExp
import com.manydesigns.elements.forms.TableForm
import com.manydesigns.elements.reflection.ClassAccessor
import com.manydesigns.elements.reflection.GroovyClassAccessor
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import net.sourceforge.stripes.action.Resolution

@RequiresPermissions(level = AccessLevel.VIEW)
class MyTableFormAction extends TableFormAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Write your code here

    public static class MyFormBean {
        protected String aField;
        protected int anotherField;

        @RegExp("a+.*")
        String getaField() {
            return aField
        }

        void setaField(String aField) {
            this.aField = aField
        }

        @MinIntValue(-10)
        @MaxIntValue(100)
        int getAnotherField() {
            return anotherField
        }

        void setAnotherField(int anotherField) {
            this.anotherField = anotherField
        }

    }

    protected List<MyFormBean> objects = [new MyFormBean(), new MyFormBean()];

    @Button(list = "form", key = "submit")
    @RequiresPermissions(permissions = FormAction.POST_FORM_PERMISSION)
    public Resolution process() {
        return doWithForm({ form, objects ->
            logger.info("Processing table form {} with objects {}", form, objects);
        });
    }

    protected ClassAccessor getClassAccessor() {
        return new GroovyClassAccessor(MyFormBean.class);
    }

    protected List<MyFormBean> getObjects() {
        return objects;
    }

    @Override
    protected void validationFailed(TableForm form, Object object) {
        super.validationFailed(form, object);
    }

}