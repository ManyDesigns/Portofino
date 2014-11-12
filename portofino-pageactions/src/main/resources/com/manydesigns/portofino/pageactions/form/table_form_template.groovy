package com.manydesigns.portofino.pageactions.form

import com.manydesigns.elements.Mode
import com.manydesigns.elements.annotations.MaxIntValue
import com.manydesigns.elements.annotations.MinIntValue
import com.manydesigns.elements.annotations.RegExp
import com.manydesigns.elements.forms.TableForm
import com.manydesigns.elements.forms.TableFormBuilder
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

    /**
     * An example bean that backs the form. See also FormAction.
     */
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

    //Adds a button to the page to process the form
    @Button(list = "form", key = "submit", type = Button.TYPE_SUCCESS)
    @RequiresPermissions(permissions = FormAction.POST_FORM_PERMISSION)
    public Resolution process() {
        return doWithForm({ form, objects ->
            logger.info("Processing table form {} with objects {}", form, objects);
        });
    }

    //Methods to implement

    @Override
    protected ClassAccessor getClassAccessor() {
        return new GroovyClassAccessor(MyFormBean.class);
    }

    @Override
    protected List<MyFormBean> getObjects() {
        return objects;
    }

    //Hook methods that can optionally be overridden to tweak the default behaviour

    @Override
    protected void validationFailed(TableForm form, Object object) {
        super.validationFailed(form, object);
    }

    @Override
    protected TableFormBuilder configureTableFormBuilder(TableFormBuilder formBuilder, Mode mode, int nRows) {
        //On the FormBuilder you can call various configXXX methods to, for example:
        // - choose which fields to show and in what order
        // - add selection providers
        return super.configureTableFormBuilder(formBuilder, mode, nRows);
    }

}