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

import com.manydesigns.portofino.resourceactions.ResourceAction;
import com.manydesigns.portofino.resourceactions.ResourceActionConfiguration;
import com.manydesigns.portofino.resourceactions.ResourceActionSupport;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ActionRegistry implements Iterable<ActionInfo> {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    protected final List<ActionInfo> registry = new CopyOnWriteArrayList<>();

    public ActionInfo register(Class<?> actionClass) {
        String descriptionKey = ResourceActionSupport.getDescriptionKey(actionClass);
        Class<? extends ResourceActionConfiguration> configurationClass =
                ResourceActionSupport.getConfigurationClass(actionClass);
        String scriptTemplate = ResourceActionSupport.getScriptTemplate(actionClass);
        boolean supportsDetail = ResourceActionSupport.supportsDetail(actionClass);
        ActionInfo info = new ActionInfo(
                actionClass, configurationClass, scriptTemplate, supportsDetail, descriptionKey);
        registry.add(info);
        return info;
    }

    public Iterator<ActionInfo> iterator() {
        return registry.iterator();
    }

    public ActionInfo getInfo(Class<? extends ResourceAction> actionClass) {
        for(ActionInfo info : registry) {
            if(info.actionClass == actionClass) {
                return info;
            }
        }
        return null;
    }

}
