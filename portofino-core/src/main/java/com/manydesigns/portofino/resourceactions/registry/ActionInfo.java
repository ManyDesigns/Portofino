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

package com.manydesigns.portofino.resourceactions.registry;

import com.manydesigns.elements.i18n.TextProvider;
import com.manydesigns.portofino.resourceactions.ResourceActionConfiguration;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ActionInfo {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public final Class<?> actionClass;
    public final Class<? extends ResourceActionConfiguration> configurationClass;
    public final String scriptTemplate;
    public final boolean supportsDetail;
    public final String description;

    public ActionInfo
            (Class<?> actionClass, Class<? extends ResourceActionConfiguration> configurationClass, String scriptTemplate,
             boolean supportsDetail, String description) {
        this.actionClass = actionClass;
        this.configurationClass = configurationClass;
        this.scriptTemplate = scriptTemplate;
        this.supportsDetail = supportsDetail;
        this.description = description;
    }

    public String getActionName(TextProvider textProvider) {
        return textProvider.getText(actionClass.getName(), actionClass.getSimpleName());
    }

    public String getActionDescription(TextProvider textProvider) {
        return textProvider.getTextOrNull(actionClass.getName() + ".description");
    }
}
