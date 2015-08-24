/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.security.SupportsPermissions;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@PageActionName("TableForm")
@ScriptTemplate("table_form_template.groovy")
@SupportsPermissions(TableFormAction.POST_FORM_PERMISSION)
public abstract class TableFormAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(TableFormAction.class);
    public static final String POST_FORM_PERMISSION = "post-form";

    protected TableForm form;

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

    protected void setupTableForm(Mode mode) {
        List<?> objects = getObjects();
        int nRows;
        if (objects == null) {
            nRows = 0;
        } else {
            nRows = objects.size();
        }
        TableFormBuilder tableFormBuilder = createTableFormBuilder();
        configureTableFormBuilder(tableFormBuilder, mode, nRows);
        form = buildTableForm(tableFormBuilder);
    }

    protected TableFormBuilder createTableFormBuilder() {
        return new TableFormBuilder(getClassAccessor());
    }

    protected TableFormBuilder configureTableFormBuilder(TableFormBuilder formBuilder, Mode mode, int nRows) {
        return formBuilder.configMode(mode).configNRows(nRows);
    }

    protected TableForm buildTableForm(TableFormBuilder formBuilder) {
        return formBuilder.build();
    }

    /**
     * Returns the ClassAccessor to access the data underlying the form
     */
    protected abstract ClassAccessor getClassAccessor();

    /**
     * Returns the list of objects underlying the form, e.g. from a database query.
     */
    protected abstract List<?> getObjects();

    /**
     * Returns what mode should the form be in.
     */
    protected Mode getMode() {
        return Mode.EDIT;
    }

    @DefaultHandler
    public Resolution execute() {
        setupTableForm(getMode());
        List<?> objects = getObjects();
        form.readFromObject(objects);
        return getShowFormResolution();
    }

    protected Resolution doWithForm(ActionOnForm closure) {
        return doWithForm(getMode(), closure);
    }

    protected Resolution doWithForm(Mode mode, ActionOnForm closure) {
        setupTableForm(mode);
        List<?> objects = getObjects();
        form.readFromObject(objects);
        form.readFromRequest(context.getRequest());
        if(form.validate()) {
            form.writeToObject(objects);
            Object result = closure.invoke(form, objects);
            if(result instanceof Resolution) {
                return (Resolution) result;
            }
        } else {
            validationFailed(form, objects);
        }
        return getShowFormResolution();
    }

    protected Resolution getShowFormResolution() {
        return new ForwardResolution("/m/pageactions/pageactions/form/table-form.jsp");
    }

    /**
     * Invoked when form validation fails.
     */
    protected void validationFailed(TableForm form, Object object) {
        logger.debug("Form validation failed");
    }

    public TableForm getForm() {
        return form;
    }

    public static interface ActionOnForm {
        Object invoke(TableForm form, Object object);
    }

}
