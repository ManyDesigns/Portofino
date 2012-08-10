/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.warningtable;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.warningtable.configuration.WarningTableConfiguration;
import com.manydesigns.portofino.pageactions.warningtable.model.Color;
import com.manydesigns.portofino.pageactions.warningtable.model.Message;
import com.manydesigns.portofino.pageactions.warningtable.model.MessagesModel;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@ConfigurationClass(WarningTableConfiguration.class)
@PageActionName("Warning Table")
public class WarningTableAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************


    //**************************************************************************
    // Variables
    //**************************************************************************
    protected MessagesModel messagesModel = new MessagesModel();

    //**************************************************************************
    // Support objects
    //**************************************************************************

    protected Form configurationForm;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(WarningTableAction.class);

    //**************************************************************************
    // Setup & configuration
    //**************************************************************************

    public Resolution preparePage() {
        Resolution resolution = super.preparePage();
        if(resolution != null) {
            return resolution;
        }
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        if(pageInstance.getConfiguration() == null) {
            pageInstance.setConfiguration(new WarningTableConfiguration());
        }
        return null;
    }




    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1, icon = "ui-icon-wrench")
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/warning-table/configure.jsp");
    }

    @Button(list = "configuration", key = "commons.updateConfiguration")
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
            SessionMessages.addInfoMessage(getMessage("commons.configuration.updated"));
            return cancel();
        } else {
            SessionMessages.addErrorMessage(getMessage("commons.configuration.notUpdated"));
            return new ForwardResolution("/layouts/warning-table/configure.jsp");
        }
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        configurationForm = new FormBuilder(WarningTableConfiguration.class)
                .build();
        configurationForm.readFromObject(pageInstance.getConfiguration());
    }



    //**************************************************************************
    // Default view
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        return forwardTo("/layouts/warning-table/view.jsp");
    }

    //--------------------------------------------------------------------------
    // Data provider
    //--------------------------------------------------------------------------
    public void loadMessagesModel() {
        int i=1;

        Message m1 = new Message(Color.GREEN,  "Project on time", "No problem found");
        Message m2 = new Message(Color.YELLOW, "Check your inbox", "It is a month that you don't check your inbox");
        Message m3 = new Message(Color.RED, "Failed login attempts", "Failed login attempts");

        messagesModel.getMessages().add(m1);
        messagesModel.getMessages().add(m2);
        messagesModel.getMessages().add(m3);

    }
    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------
    public Form getConfigurationForm() {
        return configurationForm;
    }

    public MessagesModel getMessagesModel() {
        return messagesModel;
    }

    public WarningTableConfiguration getConfiguration() {
        return (WarningTableConfiguration) pageInstance.getConfiguration();
    }

}
