/*
 * Copyright (C) 2005-2019 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.resourceactions.form;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.util.FormUtil;
import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.resourceactions.ResourceActionName;
import com.manydesigns.portofino.resourceactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.security.SupportsPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@ResourceActionName("Form")
@ScriptTemplate("form_template.groovy")
@SupportsPermissions(FormAction.POST_FORM_PERMISSION)
public abstract class FormAction extends AbstractResourceAction {
    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(FormAction.class);
    public static final String POST_FORM_PERMISSION = "post-form";

    protected Form form;

    /*@Button(list = "pageHeaderButtons", titleKey = "configure", order = 1, icon = Button.ICON_WRENCH)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/m/pageactions/pageactions/form/configure.jsp");
    }

    @Button(list = "configuration", key = "update.configuration", order = 1, type = Button.TYPE_PRIMARY)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution updateConfiguration() {
        prepareConfigurationForms();
        readPageConfigurationFromRequest();
        boolean valid = validatePageConfiguration();
        if(valid) {
            updatePageConfiguration();
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("configuration.updated.successfully"));
        }
        return cancel();
    }*/

    public Response preparePage() {
        if(!actionInstance.getParameters().isEmpty()) {
            return Response.status(404).build();
        }
        return null;
    }

    protected void setupForm(Mode mode) {
        FormBuilder formBuilder = createFormBuilder();
        configureFormBuilder(formBuilder, mode);
        form = buildForm(formBuilder);
    }

    protected FormBuilder createFormBuilder() {
        return new FormBuilder(getClassAccessor());
    }

    protected FormBuilder configureFormBuilder(FormBuilder formBuilder, Mode mode) {
        return formBuilder.configMode(mode);
    }

    protected Form buildForm(FormBuilder formBuilder) {
        return formBuilder.build();
    }

    /**
     * Returns the ClassAccessor to access the data underlying the form
     */
    protected abstract ClassAccessor getClassAccessor();

    /**
     * Returns the object underlying the form, e.g. from a database query.
     */
    protected abstract Object getObject();

    /**
     * Returns what mode should the form be in.
     */
    protected Mode getMode() {
        return Mode.EDIT;
    }

    /*@DefaultHandler
    public Resolution execute() {
        setupForm(getMode());
        Object object = getObject();
        form.readFromObject(object);
        return getShowFormResolution();
    }

    protected Resolution doWithForm(ActionOnForm closure) {
        return doWithForm(getMode(), closure);
    }

    protected Resolution doWithForm(Mode mode, ActionOnForm closure) {
        setupForm(mode);
        Object object = getObject();
        form.readFromObject(object);
        form.readFromRequest(context.getRequest());
        if(form.validate()) {
            form.writeToObject(object);
            Object result = closure.invoke(form, object);
            if(result instanceof Resolution) {
                return (Resolution) result;
            }
        } else {
            validationFailed(form, object);
            return getShowFormResolution();
        }
        return new RedirectResolution(context.getActionPath());
    }

    protected Resolution getShowFormResolution() {
        return new ForwardResolution("/m/pageactions/pageactions/form/form.jsp");
    }*/

    /**
     * Invoked when form validation fails.
     */
    protected void validationFailed(Form form, Object object) {
        logger.debug("Form validation failed");
    }

    public Form getForm() {
        return form;
    }

    public static interface ActionOnForm {
        Object invoke(Form form, Object object);
    }

    public boolean isFormWithRichTextFields() {
        return !FormUtil.collectEditableRichTextFields(form).isEmpty();
    }

}
