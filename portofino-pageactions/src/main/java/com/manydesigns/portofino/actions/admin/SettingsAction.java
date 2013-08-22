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
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.TextField;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.CommonsConfigurationAccessor;
import com.manydesigns.elements.util.BootstrapSizes;
import com.manydesigns.portofino.AppProperties;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.PageActionsModule;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.servlets.ServerInfo;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.*;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;

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

    @Inject(BaseModule.SERVER_INFO)
    public ServerInfo serverInfo;

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Inject(PageActionsModule.PAGES_DIRECTORY)
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
        return new ForwardResolution("/layouts/admin/settings.jsp");
    }

    private void setupFormAndBean() {
        SelectionProvider skinSelectionProvider =
                createSkinSelectionProvider();
        SelectionProvider pagesSelectionProvider =
                DispatcherLogic.createPagesSelectionProvider(pagesDir);

        CommonsConfigurationAccessor accessor = new CommonsConfigurationAccessor(configuration);
        form = new FormBuilder(accessor)
                .configFields(PortofinoProperties.APP_NAME, AppProperties.SKIN, AppProperties.LANDING_PAGE)
                .configSelectionProvider(skinSelectionProvider, AppProperties.SKIN)
                .configSelectionProvider(pagesSelectionProvider, AppProperties.LANDING_PAGE)
                .build();
        //TODO I18n
        TextField appNameField = (TextField) form.findFieldByPropertyName(PortofinoProperties.APP_NAME);
        appNameField.setLabel("Application name");
        appNameField.setRequired(true);
        appNameField.setFieldCssClass(BootstrapSizes.BLOCK_LEVEL);

        Field landingPageField = form.findFieldByPropertyName(AppProperties.LANDING_PAGE);
        landingPageField.setLabel("Landing page");
        landingPageField.setRequired(true);
        
        form.findFieldByPropertyName(AppProperties.SKIN).setRequired(true);
        form.readFromObject(configuration);
    }

    @Button(list = "settings", key = "commons.update", order = 1, type = Button.TYPE_PRIMARY)
    public Resolution update() {
        setupFormAndBean();
        form.readFromRequest(context.getRequest());
        if (form.validate()) {
            logger.debug("Applying settings to model");
            try {
                CompositeConfiguration appConfiguration = (CompositeConfiguration) configuration;
                FileConfiguration fileConfiguration = (FileConfiguration) appConfiguration.getConfiguration(0);
                form.writeToObject(fileConfiguration);
                fileConfiguration.save();
                logger.info("Configuration saved to " + fileConfiguration.getFile().getAbsolutePath());
            } catch (Exception e) {
                logger.error("Configuration not saved", e);
                SessionMessages.addErrorMessage(ElementsThreadLocals.getText("commons.configuration.notUpdated"));
                return new ForwardResolution("/layouts/admin/settings.jsp");
            }
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("commons.configuration.updated"));
            return new RedirectResolution(this.getClass());
        } else {
            return new ForwardResolution("/layouts/admin/settings.jsp");
        }
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
