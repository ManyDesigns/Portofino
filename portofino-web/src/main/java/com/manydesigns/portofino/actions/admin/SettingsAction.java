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

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.CommonsConfigurationAccessor;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.application.AppProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.servlets.ServerInfo;
import net.sourceforge.stripes.action.*;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAdministrator
@UrlBinding("/actions/admin/settings")
public class SettingsAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    @Inject(RequestAttributes.APPLICATION)
    Application application;

    @Inject(ApplicationAttributes.SERVER_INFO)
    ServerInfo serverInfo;

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
        return new ForwardResolution("/layouts/admin/settings.jsp");
    }

    private void setupFormAndBean() {
        SelectionProvider skinSelectionProvider =
                createSkinSelectionProvider();
        SelectionProvider pagesSelectionProvider =
                DispatcherLogic.createPagesSelectionProvider(application, application.getPagesDir());

        Configuration appConfiguration = application.getAppConfiguration();
        CommonsConfigurationAccessor accessor = new CommonsConfigurationAccessor(appConfiguration);
        form = new FormBuilder(accessor)
                .configFields(AppProperties.APPLICATION_NAME, AppProperties.SKIN, AppProperties.LANDING_PAGE)
                .configSelectionProvider(skinSelectionProvider, AppProperties.SKIN)
                .configSelectionProvider(pagesSelectionProvider, AppProperties.LANDING_PAGE)
                .build();
        //TODO I18n
        form.findFieldByPropertyName(AppProperties.APPLICATION_NAME).setLabel("Application name");
        form.findFieldByPropertyName(AppProperties.LANDING_PAGE).setLabel("Landing page");

        form.findFieldByPropertyName(AppProperties.APPLICATION_NAME).setRequired(true);
        form.findFieldByPropertyName(AppProperties.LANDING_PAGE).setRequired(true);
        form.findFieldByPropertyName(AppProperties.SKIN).setRequired(true);
        form.readFromObject(appConfiguration);
    }

    @Button(list = "settings", key = "commons.update", order = 1, primary = true)
    public Resolution update() {
        setupFormAndBean();
        form.readFromRequest(context.getRequest());
        if (form.validate()) {
            logger.debug("Applying settings to model");
            try {
                CompositeConfiguration appConfiguration = (CompositeConfiguration) application.getAppConfiguration();
                form.writeToObject(appConfiguration);
                ((FileConfiguration) appConfiguration.getConfiguration(0)).save();
            } catch (Exception e) {
                logger.error("Configuration not saved", e);
                SessionMessages.addInfoMessage(getMessage("commons.configuration.notUpdated"));
                return new ForwardResolution("/layouts/admin/settings.jsp");
            }
            SessionMessages.addInfoMessage(getMessage("commons.configuration.updated"));
            return new RedirectResolution(this.getClass());
        } else {
            return new ForwardResolution("/layouts/admin/settings.jsp");
        }
    }

    private String getMessage(String key) {
        Locale locale = context.getLocale();
        ResourceBundle bundle = application.getBundle(locale);
        return bundle.getString(key);
    }

    @Button(list = "settings", key = "commons.returnToPages", order = 2)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    //--------------------------------------------------------------------------
    // Utility methods
    //--------------------------------------------------------------------------

    public SelectionProvider createSkinSelectionProvider() {
                logger.debug("Looking for available skins");
        File webAppDirFile = new File(serverInfo.getRealPath());
        File skinDirFile = new File(webAppDirFile, "skins");
        File[] skinFiles = skinDirFile.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        String[] skins = new String[skinFiles.length];
        for (int i = 0; i < skinFiles.length; i++) {
            File current = skinFiles[i];
            String skinName = current.getName();
            skins[i] = skinName;
            logger.debug("Found skin: {}", skinName);
        }

        DefaultSelectionProvider skinSelectionProvider = new DefaultSelectionProvider("skins");
        for(String s : skins) {
            skinSelectionProvider.appendRow(s, s, true);
        }

        return skinSelectionProvider;
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

    public Form getForm() {
        return form;
    }

}
