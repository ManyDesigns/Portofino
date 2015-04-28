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

package com.manydesigns.portofino.pageactions.activitystream;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@PageActionName("Activity stream")
@ScriptTemplate("script_template.groovy")
public class ActivityStreamAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LoggerFactory.getLogger(ActivityStreamAction.class);

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final List<ActivityItem> activityItems = new ArrayList<ActivityItem>();

    //--------------------------------------------------------------------------
    // Web methods
    //--------------------------------------------------------------------------

    @DefaultHandler
    public Resolution execute() {
        populateActivityItems();

        return getViewResolution();
    }

    //--------------------------------------------------------------------------
    // Hooks
    //--------------------------------------------------------------------------

    public void populateActivityItems() {
        logger.info("Default populateActivityItems()");
    }

    protected Resolution getViewResolution() {
        return new ForwardResolution("/m/pageactions/pageactions/activitystream/view.jsp");
    }


    //--------------------------------------------------------------------------
    // Configuration
    //--------------------------------------------------------------------------

    @Button(list = "pageHeaderButtons", titleKey = "configure", order = 1, icon = Button.ICON_WRENCH)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/m/pageactions/pageactions/activitystream/configure.jsp");
    }

    @Button(list = "configuration", key = "update.configuration", order = 1, type = Button.TYPE_PRIMARY)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution updateConfiguration() {
        prepareConfigurationForms();
        readPageConfigurationFromRequest();
        boolean valid = validatePageConfiguration();
        if(valid) {
            updatePageConfiguration();
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("configuration.updated.successfully"));
        }
        return cancel();
    }

    //--------------------------------------------------------------------------
    // PageAction implementation
    //--------------------------------------------------------------------------

    public Resolution preparePage() {
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        return null;
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

    public List<ActivityItem> getActivityItems() {
        return activityItems;
    }
}
