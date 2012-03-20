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
