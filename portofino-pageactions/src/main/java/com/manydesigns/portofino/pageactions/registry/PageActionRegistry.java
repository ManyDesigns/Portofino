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

package com.manydesigns.portofino.pageactions.registry;

import com.manydesigns.portofino.dispatcher.PageAction;
import com.manydesigns.portofino.pageactions.PageActionLogic;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PageActionRegistry implements Iterable<PageActionInfo> {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final List<PageActionInfo> registry = new CopyOnWriteArrayList<PageActionInfo>();

    public PageActionInfo register(Class<?> actionClass) {
        String descriptionKey = PageActionLogic.getDescriptionKey(actionClass);
        Class<?> configurationClass = PageActionLogic.getConfigurationClass(actionClass);
        String scriptTemplate = PageActionLogic.getScriptTemplate(actionClass);
        boolean supportsDetail = PageActionLogic.supportsDetail(actionClass);
        PageActionInfo info = new PageActionInfo(actionClass, configurationClass, scriptTemplate,
                                                 supportsDetail, descriptionKey);
        registry.add(info);
        return info;
    }

    public Iterator<PageActionInfo> iterator() {
        return registry.iterator();
    }

    public PageActionInfo getInfo(Class<? extends PageAction> actionClass) {
        for(PageActionInfo info : registry) {
            if(info.actionClass == actionClass) {
                return info;
            }
        }
        return null;
    }

}
