package com.manydesigns.portofino.pageactions.form

import com.manydesigns.elements.Mode
import com.manydesigns.elements.forms.Form
import com.manydesigns.elements.forms.FormBuilder
import com.manydesigns.elements.messages.SessionMessages
import com.manydesigns.elements.options.DefaultSelectionProvider
import com.manydesigns.elements.reflection.ClassAccessor
import com.manydesigns.elements.reflection.GroovyClassAccessor
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import net.sourceforge.stripes.action.Resolution
import com.manydesigns.elements.annotations.*

@RequiresPermissions(level = AccessLevel.VIEW)
class MyFormAction extends FormAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Write your code here

    /**
     * An example bean that backs the form. On getters you can use Elements annotations for validation and presentation tweaks
     */
    public static class MyFormBean {
        protected String aField;
        protected int anotherField;
        protected Date aDateField;
        protected String aReadOnlyField = "I am read only";
        protected String aSelectField;
        protected String aDisabledField;

        @RegExp("a+.*")
        @Multiline
        @ColSpan(2)
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

        @Required
        Date getaDateField() {
            return aDateField
        }

        void setaDateField(Date aDateField) {
            this.aDateField = aDateField
        }

        @Insertable(false) //For mode.CREATE
        @Updatable(false) //For mode.EDIT
        String getaReadOnlyField() {
            return aReadOnlyField
        }

        void setaReadOnlyField(String aReadOnlyField) {
            this.aReadOnlyField = aReadOnlyField
        }

        @Enabled(false)
        String getaDisabledField() {
            return aDisabledField
        }

        void setaDisabledField(String aDisabledField) {
            this.aDisabledField = aDisabledField
        }


        String getaSelectField() {
            return aSelectField
        }

        void setaSelectField(String aSelectField) {
            this.aSelectField = aSelectField
        }
    }

    protected MyFormBean object = new MyFormBean();

    //Adds a button to the page to process the form
    @Button(list = "form", key = "submit", type = Button.TYPE_SUCCESS)
    @RequiresPermissions(permissions = FormAction.POST_FORM_PERMISSION)
    public Resolution process() {
        return doWithForm({ form, object ->
            SessionMessages.addInfoMessage("Processed form, written to object " + (object.properties));
        });
    }

    //Methods to implement

    @Override
    protected ClassAccessor getClassAccessor() {
        return new GroovyClassAccessor(MyFormBean.class);
    }

    @Override
    protected MyFormBean getObject() {
        return object;
    }

    //Hook methods that can optionally be overridden to tweak the default behaviour

    @Override
    protected void validationFailed(Form form, Object object) {
        super.validationFailed(form, object);
    }

    @Override
    protected FormBuilder configureFormBuilder(FormBuilder formBuilder, Mode mode) {
        //On the FormBuilder you can call various configXXX methods to:
        // - choose which fields to show and in what order
        // - add selection providers
        // - build a 2-columns or 3-columns form
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider("aSelectField");
        selectionProvider.appendRow(1, "One", true);
        selectionProvider.appendRow(2, "Two", true);
        selectionProvider.appendRow(3, "Three", true);
        return super.configureFormBuilder(formBuilder, mode).
                configNColumns(2).
                configFields("aField", "anotherField", "aDateField", "aReadOnlyField", "aSelectField").
                configSelectionProvider(selectionProvider, "aSelectField");
    }

    @Override
    protected void setupForm(Mode mode) {
        super.setupForm(mode);
        form.findFieldByPropertyName("aReadOnlyField").setHref("javascript:alert('And I\\'m also a link!');");
    }


}