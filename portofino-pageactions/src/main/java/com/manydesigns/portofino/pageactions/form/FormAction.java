/*
 * Copyright (C) 2005-2014 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.form;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.security.SupportsPermissions;
import groovy.lang.Closure;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@PageActionName("Form")
@ScriptTemplate("form_template.groovy")
@SupportsPermissions(FormAction.POST_FORM_PERMISSION)
public abstract class FormAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2014, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(FormAction.class);
    public static final String POST_FORM_PERMISSION = "post-form";

    protected Form form;

    @Button(list = "pageHeaderButtons", titleKey = "configure", order = 1, icon = Button.ICON_WRENCH)
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
    }

    public Resolution preparePage() {
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
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

    protected abstract ClassAccessor getClassAccessor();

    protected abstract Object getObject();

    @DefaultHandler
    public Resolution execute() {
        setupForm(Mode.EDIT);
        Object object = getObject();
        form.readFromObject(object);
        return forwardTo("/m/pageactions/pageactions/form/form.jsp");
    }

    protected Resolution doWithForm(Closure<?> closure) {
        return doWithForm(Mode.EDIT, closure);
    }

    protected Resolution doWithForm(Mode mode, Closure<?> closure) {
        setupForm(mode);
        Object object = getObject();
        form.readFromObject(object);
        form.readFromRequest(context.getRequest());
        if(form.validate()) {
            form.writeToObject(object);
            closure.call(form, object);
        } else {
            validationFailed(form, object);
        }
        return forwardTo("/m/pageactions/pageactions/form/form.jsp");
    }

    protected void validationFailed(Form form, Object object) {
        logger.debug("Form validation failed");
    }

    public Form getForm() {
        return form;
    }

}
