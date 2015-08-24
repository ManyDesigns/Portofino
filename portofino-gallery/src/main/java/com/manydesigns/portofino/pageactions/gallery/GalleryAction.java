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

package com.manydesigns.portofino.pageactions.gallery;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.gallery.configuration.GalleryConfiguration;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Emanuele Poggi    - emanuele.poggi@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@ConfigurationClass(GalleryConfiguration.class)
@PageActionName("Gallery")
@ScriptTemplate("script_template.groovy")
public class GalleryAction extends AbstractPageAction {
    public static final String copyright = "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    //**************************************************************************
    // Variables
    //**************************************************************************

    protected GalleryView galleryView;
    protected final Gallery gallery = new Gallery();

    //**************************************************************************
    // Support objects
    //**************************************************************************

    protected Form configurationForm;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(GalleryAction.class);

    //**************************************************************************
    // Setup and configuration
    //**************************************************************************

    public Resolution preparePage() {
        if(pageInstance.getConfiguration() == null) {
            pageInstance.setConfiguration(new GalleryConfiguration());
        }
        return null;
    }

    @Button(list = "pageHeaderButtons", titleKey = "configure", order = 1, icon = Button.ICON_WRENCH)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/m/gallery/configure.jsp");
    }

    @Button(list = "configuration", key = "update.configuration", type = Button.TYPE_PRIMARY)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution updateConfiguration() {
        prepareConfigurationForms();
        readPageConfigurationFromRequest();
        configurationForm.readFromRequest(context.getRequest());
        boolean valid = validatePageConfiguration();
        valid = valid && configurationForm.validate();
        if(valid) {
            updatePageConfiguration();
            configurationForm.writeToObject(pageInstance.getConfiguration());
            saveConfiguration(pageInstance.getConfiguration());
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("configuration.updated.successfully"));
            return cancel();
        } else {
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("the.configuration.could.not.be.saved"));
            return new ForwardResolution("/m/gallery/configure.jsp");
        }
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        configurationForm = new FormBuilder(GalleryConfiguration.class).build();
        configurationForm.readFromObject(pageInstance.getConfiguration());
    }

    //**************************************************************************
    // Handlers
    //**************************************************************************

    @DefaultHandler
    @RequiresPermissions(level = AccessLevel.VIEW)
    public Resolution execute() {
            return galleryView();
    }

    public Resolution galleryView() {
        return new ForwardResolution("/m/gallery/gallery.jsp");
    }

    //--------------------------------------------------------------------------
    // Data provider
    //--------------------------------------------------------------------------

    public void loadImages() {}

    public GalleryView getGalleryView() {
        return galleryView;
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public Gallery getGallery() {
        return gallery;
    }

    public List<Image> getImages() {
        loadImages();
        return gallery.getImages();
    }

    public Form getConfigurationForm() {
        return configurationForm;
    }

    public GalleryConfiguration getConfiguration() {
        return (GalleryConfiguration) pageInstance.getConfiguration();
    }

}
