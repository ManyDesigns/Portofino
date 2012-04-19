/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
