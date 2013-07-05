/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.changepassword;

import com.manydesigns.elements.annotations.LabelI18N;
import com.manydesigns.elements.annotations.Password;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.logic.SelectionProviderLogic;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.changepassword.configuration.ChangePasswordConfiguration;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@PageActionName("Change Password")
@ConfigurationClass(ChangePasswordConfiguration.class)
@ScriptTemplate("script_template.groovy")
public class ChangePasswordAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(ChangePasswordAction.class);

    protected Form form;

    protected String oldPassword;
    protected String newPassword;

    //Conf
    protected ChangePasswordConfiguration configuration;
    protected Form configurationForm;

    @Button(list = "portletHeaderButtons", titleKey = "commons.configure", order = 1, icon = Button.ICON_WRENCH)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/changepassword/configure.jsp");
    }

    @Button(list = "configuration", key = "commons.updateConfiguration", order = 1, type = Button.TYPE_PRIMARY)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution updateConfiguration() {
        prepareConfigurationForms();
        readPageConfigurationFromRequest();
        configurationForm.readFromRequest(context.getRequest());
        boolean valid = validatePageConfiguration();
        valid = configurationForm.validate() && valid;
        if(valid) {
            updatePageConfiguration();
            configurationForm.writeToObject(configuration);
            saveConfiguration(configuration);
            SessionMessages.addInfoMessage(getMessage("commons.configuration.updated"));
        }
        return cancel();
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        FormBuilder formBuilder = new FormBuilder(ChangePasswordConfiguration.class);
        formBuilder.configFieldSetNames("Password change configuration");
        formBuilder.configFields("database", "query", "property");

        SelectionProvider databaseSelectionProvider =
                SelectionProviderLogic.createSelectionProvider(
                        "database",
                        model.getDatabases(),
                        Database.class,
                        null,
                        new String[] { "databaseName" });
        formBuilder.configSelectionProvider(databaseSelectionProvider, "database");

        if(configuration.getActualTable() != null) {
            ClassAccessor accessor = new TableAccessor(configuration.getActualTable());
            SelectionProvider propertySelectionProvider =
                SelectionProviderLogic.createSelectionProvider(
                        "property",
                        Arrays.asList(accessor.getProperties()),
                        PropertyAccessor.class,
                        null,
                        new String[] { "name" });

            formBuilder.configSelectionProvider(propertySelectionProvider, "property");
        }

        configurationForm = formBuilder.build();
        configurationForm.readFromObject(configuration);
    }

    public Resolution preparePage() {
        Resolution resolution = super.preparePage();
        if(resolution != null) {
            return resolution;
        }
        this.configuration = (ChangePasswordConfiguration) pageInstance.getConfiguration();
        if(!pageInstance.getParameters().isEmpty()) {
            return portletPageNotFound();
        }
        return null;
    }

    public Form getForm() {
        return form;
    }

    @Password
    @Required
    @LabelI18N("changepasswordaction.old.password")
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    @Password(confirmationRequired = true)
    @Required
    @LabelI18N("changepasswordaction.new.password")
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public Form getConfigurationForm() {
        return configurationForm;
    }
}
