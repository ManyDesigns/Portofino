/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.upstairs.actions.mail;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.util.FormUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.mail.setup.MailProperties;
import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import com.manydesigns.portofino.upstairs.actions.mail.support.MailSettings;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class MailSettingsAction extends AbstractResourceAction {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION_FILE)
    public FileBasedConfigurationBuilder configurationFile;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    private final static Logger logger =
            LoggerFactory.getLogger(MailSettingsAction.class);

    //--------------------------------------------------------------------------
    // Action events
    //--------------------------------------------------------------------------

    protected Form setupFormAndBean() {
        Form form = new FormBuilder(MailSettings.class).build();
        MailSettings settings = new MailSettings();
        settings.mailEnabled = configuration.getBoolean(MailProperties.MAIL_ENABLED, false);
        settings.keepSent = configuration.getBoolean(MailProperties.MAIL_KEEP_SENT, false);
        settings.smtpHost = configuration.getString(MailProperties.MAIL_SMTP_HOST);
        settings.smtpPort = configuration.getInteger(MailProperties.MAIL_SMTP_PORT, null);
        settings.smtpSSL = configuration.getBoolean(MailProperties.MAIL_SMTP_SSL_ENABLED, false);
        settings.smtpTLS = configuration.getBoolean(MailProperties.MAIL_SMTP_TLS_ENABLED, false);
        settings.smtpLogin = configuration.getString(MailProperties.MAIL_SMTP_LOGIN);
        settings.smtpPassword = configuration.getString(MailProperties.MAIL_SMTP_PASSWORD);
        settings.queueLocation = configuration.getProperty(MailProperties.MAIL_QUEUE_LOCATION) + "";

        form.readFromObject(settings);
        return form;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Form read() {
        return setupFormAndBean();
    }

    @Path(":classAccessor")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String describeClassAccessor() {
        JSONStringer jsonStringer = new JSONStringer();
        ReflectionUtil.classAccessorToJson(JavaClassAccessor.getClassAccessor(MailSettings.class), jsonStringer);
        return jsonStringer.toString();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Form update(String jsonObject) {
        Form form = setupFormAndBean();
        FormUtil.readFromJson(form, new JSONObject(jsonObject));
        if (form.validate()) {
            logger.debug("Applying settings to model");
            try {
                MailSettings settings = new MailSettings();
                form.writeToObject(settings);
                configuration.setProperty(MailProperties.MAIL_ENABLED, settings.mailEnabled);
                configuration.setProperty(MailProperties.MAIL_KEEP_SENT, settings.keepSent);
                configuration.setProperty(MailProperties.MAIL_QUEUE_LOCATION, settings.queueLocation);
                configuration.setProperty(MailProperties.MAIL_SMTP_HOST, settings.smtpHost);
                configuration.setProperty(MailProperties.MAIL_SMTP_PORT, settings.smtpPort);
                configuration.setProperty(MailProperties.MAIL_SMTP_SSL_ENABLED, settings.smtpSSL);
                configuration.setProperty(MailProperties.MAIL_SMTP_TLS_ENABLED, settings.smtpTLS);
                configuration.setProperty(MailProperties.MAIL_SMTP_LOGIN, settings.smtpLogin);
                configuration.setProperty(MailProperties.MAIL_SMTP_PASSWORD, settings.smtpPassword);
                configurationFile.save();
                logger.info("Saved configuration file {}", configurationFile.getFileHandler().getFile().getAbsolutePath());
                return form;
            } catch (Exception e) {
                logger.error("Configuration not saved", e);
                throw new WebApplicationException("Configuration not saved", e);
            }
        } else {
            throw new WebApplicationException(Response.serverError().entity(form).build());
        }
    }

}
