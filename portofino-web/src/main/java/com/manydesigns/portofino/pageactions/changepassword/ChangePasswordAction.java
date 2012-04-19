/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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
import com.manydesigns.elements.text.OgnlSqlFormat;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SelectionProviderLogic;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.changepassword.configuration.ChangePasswordConfiguration;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.*;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(ChangePasswordAction.class);

    protected Form form;

    protected String oldPassword;
    protected String newPassword;

    //Conf
    protected ChangePasswordConfiguration configuration;
    protected Form configurationForm;

    @DefaultHandler
    public Resolution execute() {
        prepareForm();
        if(!isConfigurationValid()) {
            return forwardToPortletNotConfigured();
        }
        if(isEmbedded()) {
            return new ForwardResolution("/layouts/changepassword/embedded.jsp");
        } else {
            return forwardToPortletPage("/layouts/changepassword/change.jsp");
        }
    }

    protected boolean isConfigurationValid() {
        return configuration != null &&
               configuration.getActualDatabase() != null &&
               configuration.getActualTable() != null &&
               configuration.getProperty() != null;
    }

    protected void prepareForm() {
        form = new FormBuilder(getClass())
                    .configFields("oldPassword", "newPassword")
                    .build();
        form.readFromObject(this);
    }

    @Button(list = "changepassword", key = "commons.ok")
    public Resolution change() {
        prepareForm();
        if(!isConfigurationValid()) {
            return forwardToPortletNotConfigured();
        }
        form.readFromRequest(context.getRequest());
        if(form.validate()) {
            Object user = loadUser();
            if(user != null) {
                try {
                    PropertyAccessor pwdAccessor = getPasswordPropertyAccessor();
                    String oldPwd = getOldPasswordFromUser(user, pwdAccessor);
                    if(encrypt(oldPassword).equals(oldPwd)) {
                        savePassword(user, pwdAccessor);
                    } else {
                        SessionMessages.addErrorMessage(getMessage("changepasswordaction.wrong.password"));
                    }
                } catch (NoSuchFieldException e) {
                    logger.error("Password property accessor: no such field", e);
                    return forwardToPortletNotConfigured();
                }
            } else {
                return forwardToPortletNotConfigured();
            }
        }
        String fwd = "/layouts/changepassword/change.jsp";
        return forwardToPortletPage(fwd);
    }

    @Override
    @Buttons({
        @Button(list = "configuration", key = "commons.cancel", order = 99),
        @Button(list = "changepassword",  key = "commons.cancel", order = 99)})
    public Resolution cancel() {
        return super.cancel();
    }

    @Override
    protected String getDefaultCancelReturnUrl() {
        PageInstance parent = dispatch.getLastPageInstance().getParent();
        if(parent != null) {
            return context.getRequest().getContextPath() + "/" +
                   parent.getPath();
        } else {
            return super.getDefaultCancelReturnUrl();
        }
    }

    //Implementation/hooks

    protected String getOldPasswordFromUser(Object user, PropertyAccessor pwdAccessor) {
        //TODO check type
        return (String) pwdAccessor.get(user);
    }

    protected Object loadUser() {
        Session session = application.getSession(configuration.getActualDatabase().getDatabaseName());
        OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(configuration.getQuery());
        final String queryString = sqlFormat.getFormatString();
        final Object[] parameters = sqlFormat.evaluateOgnlExpressions(this);
        Query query = session.createQuery(queryString);
        for (int i = 0; i < parameters.length; i++) {
            query.setParameter(i, parameters[i]);
        }
        try {
            return query.uniqueResult();
        } catch (NonUniqueResultException e) {
            logger.error("The query did not return a unique result", e);
            return null;
        }
    }

    protected PropertyAccessor getPasswordPropertyAccessor() throws NoSuchFieldException {
        Table table = configuration.getActualTable();
        TableAccessor accessor = new TableAccessor(table);
        return accessor.getProperty(configuration.getProperty());
    }

    protected void savePassword(Object user, PropertyAccessor pwdAccessor) {
        Session session = application.getSession(configuration.getActualDatabase().getDatabaseName());
        Table table = configuration.getActualTable();
        pwdAccessor.set(user, encrypt(newPassword));
        session.save(table.getActualEntityName(), user);
        session.getTransaction().commit();
    }

    protected String encrypt(String oldPassword) {
        return oldPassword;
    }

    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1, icon = "ui-icon-wrench")
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/changepassword/configure.jsp");
    }

    @Button(list = "configuration", key = "commons.updateConfiguration")
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

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        this.pageInstance = pageInstance;
        this.configuration = (ChangePasswordConfiguration) pageInstance.getConfiguration();
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
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
