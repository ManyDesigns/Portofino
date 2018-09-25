/*
 * Copyright (C) 2005-2017 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.upstairs.actions.settings;

import com.manydesigns.elements.configuration.CommonsConfigurationUtils;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.util.FormUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.upstairs.Settings;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class SettingsAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (C) 2005-2017 ManyDesigns srl";

    public static final String URL_BINDING = "/actions/admin/settings";

    @Autowired
    public Configuration configuration;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    private final static Logger logger =
            LoggerFactory.getLogger(SettingsAction.class);

    //--------------------------------------------------------------------------
    // Action events
    //--------------------------------------------------------------------------

    protected Form setupFormAndBean() {
        Settings settings = new Settings();
        settings.appName = configuration.getString(PortofinoProperties.APP_NAME);
        settings.appVersion = configuration.getString(PortofinoProperties.APP_VERSION);
        settings.preloadGroovyPages = configuration.getBoolean(PortofinoProperties.GROOVY_PRELOAD_PAGES, false);
        settings.preloadGroovyClasses = configuration.getBoolean(PortofinoProperties.GROOVY_PRELOAD_CLASSES, false);
        Form form = new FormBuilder(Settings.class).build();
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
        ReflectionUtil.classAccessorToJson(JavaClassAccessor.getClassAccessor(Settings.class), jsonStringer);
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
                Settings settings = new Settings();
                form.writeToObject(settings);
                configuration.setProperty(PortofinoProperties.APP_NAME, settings.appName);
                configuration.setProperty(PortofinoProperties.APP_VERSION, settings.appVersion);
                if(!settings.preloadGroovyPages ||
                   configuration.getProperty(PortofinoProperties.GROOVY_PRELOAD_PAGES) != null) {
                    configuration.setProperty(PortofinoProperties.GROOVY_PRELOAD_PAGES, settings.preloadGroovyPages);
                }
                if(!settings.preloadGroovyClasses ||
                   configuration.getProperty(PortofinoProperties.GROOVY_PRELOAD_CLASSES) != null) {
                    configuration.setProperty(PortofinoProperties.GROOVY_PRELOAD_CLASSES, settings.preloadGroovyClasses);
                }
                CommonsConfigurationUtils.save(configuration);
                logger.info("Configuration saved");
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
