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

package com.manydesigns.portofino.actions.admin;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.annotations.CssClass;
import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.util.BootstrapSizes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.PageactionsModule;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAdministrator
@UrlBinding(SettingsAction.URL_BINDING)
public class SettingsAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final String URL_BINDING = "/actions/admin/settings";

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Inject(PageactionsModule.PAGES_DIRECTORY)
    public File pagesDir;

    Form form;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger =
            LoggerFactory.getLogger(SettingsAction.class);

    //--------------------------------------------------------------------------
    // Action events
    //--------------------------------------------------------------------------

    @DefaultHandler
    public Resolution execute() {
        setupFormAndBean();
        return new ForwardResolution("/m/pageactions/actions/admin/settings.jsp");
    }

    private void setupFormAndBean() {
        SelectionProvider pagesSelectionProvider =
                DispatcherLogic.createPagesSelectionProvider(pagesDir);

        Settings settings = new Settings();
        settings.appName = configuration.getString(PortofinoProperties.APP_NAME);
        settings.landingPage = configuration.getString(PortofinoProperties.LANDING_PAGE);
        settings.loginPage = configuration.getString(PortofinoProperties.LOGIN_PAGE);
        settings.preloadGroovyPages = configuration.getBoolean(PortofinoProperties.GROOVY_PRELOAD_PAGES, true);
        settings.preloadGroovyClasses = configuration.getBoolean(PortofinoProperties.GROOVY_PRELOAD_CLASSES, true);

        form = new FormBuilder(Settings.class)
                .configSelectionProvider(pagesSelectionProvider, "landingPage")
                .configSelectionProvider(pagesSelectionProvider, "loginPage")
                .build();

        form.readFromObject(settings);
    }

    @Button(list = "settings", key = "update", order = 1, type = Button.TYPE_PRIMARY)
    public Resolution update() {
        setupFormAndBean();
        form.readFromRequest(context.getRequest());
        if (form.validate()) {
            logger.debug("Applying settings to model");
            try {
                FileConfiguration fileConfiguration = (FileConfiguration) configuration;
                Settings settings = new Settings();
                form.writeToObject(settings);
                fileConfiguration.setProperty(PortofinoProperties.APP_NAME, settings.appName);
                fileConfiguration.setProperty(PortofinoProperties.LANDING_PAGE, settings.landingPage);
                fileConfiguration.setProperty(PortofinoProperties.LOGIN_PAGE, settings.loginPage);
                if(!settings.preloadGroovyPages ||
                   fileConfiguration.getProperty(PortofinoProperties.GROOVY_PRELOAD_PAGES) != null) {
                    fileConfiguration.setProperty(PortofinoProperties.GROOVY_PRELOAD_PAGES, settings.preloadGroovyPages);
                }
                if(!settings.preloadGroovyClasses ||
                   fileConfiguration.getProperty(PortofinoProperties.GROOVY_PRELOAD_CLASSES) != null) {
                    fileConfiguration.setProperty(PortofinoProperties.GROOVY_PRELOAD_CLASSES, settings.preloadGroovyClasses);
                }
                fileConfiguration.save();
                logger.info("Configuration saved to " + fileConfiguration.getFile().getAbsolutePath());
            } catch (Exception e) {
                logger.error("Configuration not saved", e);
                SessionMessages.addErrorMessage(ElementsThreadLocals.getText("the.configuration.could.not.be.saved"));
                return new ForwardResolution("/m/pageactions/actions/admin/settings.jsp");
            }
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("configuration.updated.successfully"));
            return new RedirectResolution(this.getClass());
        } else {
            return new ForwardResolution("/m/pageactions/actions/admin/settings.jsp");
        }
    }

    @Button(list = "settings", key = "return.to.pages", order = 2)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

    public Form getForm() {
        return form;
    }

    //--------------------------------------------------------------------------
    // Form
    //--------------------------------------------------------------------------

    public static class Settings {

        @Required
        @Label("Application name")
        @CssClass(BootstrapSizes.BLOCK_LEVEL)
        public String appName;
        @Required
        public String landingPage;
        @Required
        public String loginPage;
        @Required
        @Label("Preload Groovy pages at startup")
        public boolean preloadGroovyPages;
        @Required
        @Label("Preload Groovy shared classes at startup")
        public boolean preloadGroovyClasses;

    }

}
