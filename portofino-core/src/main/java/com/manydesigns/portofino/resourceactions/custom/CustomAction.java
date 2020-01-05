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

package com.manydesigns.portofino.resourceactions.custom;

import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.resourceactions.ResourceActionName;
import com.manydesigns.portofino.resourceactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@ResourceActionName("Custom")
@ScriptTemplate("script_template.groovy")
public class CustomAction extends AbstractResourceAction {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(CustomAction.class);

    /*@Button(list = "pageHeaderButtons", titleKey = "configure", order = 1, icon = Button.ICON_WRENCH)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/m/pageactions/pageactions/custom/configure.jsp");
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
    }*/

}
