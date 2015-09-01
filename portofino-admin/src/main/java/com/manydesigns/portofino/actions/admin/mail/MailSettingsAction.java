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

package com.manydesigns.portofino.actions.admin.mail;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.configuration.CommonsConfigurationUtils;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.util.BootstrapSizes;
import com.manydesigns.mail.setup.MailProperties;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.*;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAuthentication
@RequiresAdministrator
@UrlBinding(MailSettingsAction.URL_BINDING)
public class MailSettingsAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String URL_BINDING = "/actions/admin/mail/settings";

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    Form form;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger =
            LoggerFactory.getLogger(MailSettingsAction.class);

    //--------------------------------------------------------------------------
    // Action events
    //--------------------------------------------------------------------------

    @DefaultHandler
    public Resolution execute() {
        setupFormAndBean();
        return new ForwardResolution("/m/admin/mail/settings.jsp");
    }

    @Button(list = "settings", key = "update", order = 1, type = Button.TYPE_PRIMARY)
    public Resolution update() {
        setupFormAndBean();
        form.readFromRequest(context.getRequest());
        if (form.validate()) {
            logger.debug("Applying settings to app configuration");
            try {
                MailSettingsForm bean = new MailSettingsForm();
                form.writeToObject(bean);
                configuration.setProperty(MailProperties.MAIL_ENABLED, bean.mailEnabled);
                configuration.setProperty(MailProperties.MAIL_KEEP_SENT, bean.keepSent);
                configuration.setProperty(MailProperties.MAIL_QUEUE_LOCATION, bean.queueLocation);
                configuration.setProperty(MailProperties.MAIL_SMTP_HOST, bean.smtpHost);
                configuration.setProperty(MailProperties.MAIL_SMTP_PORT, bean.smtpPort);
                configuration.setProperty(MailProperties.MAIL_SMTP_SSL_ENABLED, bean.smtpSSL);
                configuration.setProperty(MailProperties.MAIL_SMTP_TLS_ENABLED, bean.smtpTLS);
                configuration.setProperty(MailProperties.MAIL_SMTP_LOGIN, bean.smtpLogin);
                configuration.setProperty(MailProperties.MAIL_SMTP_PASSWORD, bean.smtpPassword);
                CommonsConfigurationUtils.save(configuration);
                logger.info("Configuration saved");
            } catch (Exception e) {
                logger.error("Configuration not saved", e);
                SessionMessages.addErrorMessage(ElementsThreadLocals.getText("the.configuration.could.not.be.saved"));
                return new ForwardResolution("/m/admin/mail/settings.jsp");
            }
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("configuration.updated.successfully"));
            return new RedirectResolution(this.getClass());
        } else {
            return new ForwardResolution("/m/admin/mail/settings.jsp");
        }
    }

    @Button(list = "settings", key = "return.to.pages", order = 2  , icon = Button.ICON_HOME)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    //--------------------------------------------------------------------------
    // Form
    //--------------------------------------------------------------------------

    public static class MailSettingsForm {

        @Required
        @FieldSet("General")
        public boolean mailEnabled;

        @FieldSet("General")
        @Label("Keep sent messages")
        public boolean keepSent;

        @Required
        @FieldSet("General")
        @Label("Queue location")
        @FieldSize(100)
        @CssClass(BootstrapSizes.FILL_ROW)
        public String queueLocation;

        @FieldSet("SMTP")
        @Label("Host")
        @CssClass(BootstrapSizes.FILL_ROW)
        public String smtpHost;

        @FieldSet("SMTP")
        @Label("Port")
        public Integer smtpPort;

        @Required
        @FieldSet("SMTP")
        @Label("SSL enabled")
        public boolean smtpSSL;

        @FieldSet("SMTP")
        @Label("TLS enabled")
        public boolean smtpTLS;

        @FieldSet("SMTP")
        @Label("Login")
        public String smtpLogin;

        @FieldSet("SMTP")
        @Password
        @Label("Password")
        public String smtpPassword;

    }

    private void setupFormAndBean() {
        form = new FormBuilder(MailSettingsForm.class).build();
        MailSettingsForm bean = new MailSettingsForm();
        bean.mailEnabled = configuration.getBoolean(MailProperties.MAIL_ENABLED, false);
        bean.keepSent = configuration.getBoolean(MailProperties.MAIL_KEEP_SENT, false);
        bean.smtpHost = configuration.getString(MailProperties.MAIL_SMTP_HOST);
        bean.smtpPort = configuration.getInteger(MailProperties.MAIL_SMTP_PORT, null);
        bean.smtpSSL = configuration.getBoolean(MailProperties.MAIL_SMTP_SSL_ENABLED, false);
        bean.smtpTLS = configuration.getBoolean(MailProperties.MAIL_SMTP_TLS_ENABLED, false);
        bean.smtpLogin = configuration.getString(MailProperties.MAIL_SMTP_LOGIN);
        bean.smtpPassword = configuration.getString(MailProperties.MAIL_SMTP_PASSWORD);
        bean.queueLocation = configuration.getProperty(MailProperties.MAIL_QUEUE_LOCATION) + "";

        form.readFromObject(bean);
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

    public Form getForm() {
        return form;
    }

}
