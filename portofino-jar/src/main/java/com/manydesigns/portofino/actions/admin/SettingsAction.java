/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.portofino.actions.admin;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.actions.AbstractActionBean;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.ServerInfo;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.logic.PageLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.pages.RootPage;
import com.manydesigns.portofino.system.model.users.annotations.RequiresAdministrator;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.ActionResolver;
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
@UrlBinding("/actions/admin/settings")
public class SettingsAction extends AbstractActionBean implements AdminAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    @Inject(RequestAttributes.APPLICATION)
    Application application;

    @Inject(ApplicationAttributes.SERVER_INFO)
    ServerInfo serverInfo;

    @Inject(RequestAttributes.MODEL)
    Model model;

    Form form;
    RootPage rootPage;

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
        rootPage = model.getRootPage();

        SelectionProvider skinSelectionProvider =
                createSkinSelectionProvider();
        SelectionProvider pagesSelectionProvider =
                PageLogic.createPagesSelectionProvider(model.getRootPage());

        form = new FormBuilder(RootPage.class)
                .configFields("title", "skin", "landingPage")
                .configSelectionProvider(skinSelectionProvider, "skin")
                .configSelectionProvider(pagesSelectionProvider, "landingPage")
                .build();
        form.findFieldByPropertyName("title").setLabel("Application name");
        form.readFromObject(rootPage);
    }

    @Button(list = "settings", key = "commons.update", order = 1)
    public Resolution update() {
        synchronized (application) {
            setupFormAndBean();
            form.readFromRequest(context.getRequest());
            if (form.validate()) {
                logger.debug("Applying settings to model");
                form.writeToObject(rootPage);

                model.init();
                application.saveXmlModel();
                SessionMessages.addInfoMessage("Settings updated successfully");
                return new RedirectResolution(this.getClass());
            } else {
                return new ForwardResolution("/layouts/admin/settings.jsp");
            }
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

        SelectionProvider skinSelectionProvider =
                DefaultSelectionProvider.create("skins", skins, skins);

        return skinSelectionProvider;
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

    public Form getForm() {
        return form;
    }

    public String getActionPath() {
        return (String) getContext().getRequest().getAttribute(ActionResolver.RESOLVED_ACTION);
    }
}
