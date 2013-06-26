/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.menu;

import com.manydesigns.elements.ElementsThreadLocals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SimpleMenuAppender implements MenuAppender {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(SimpleMenuAppender.class);

    public final String targetMenuGroupId;
    public final String id;
    public final String icon;
    public final String labelKey;
    public final String link;

    protected SimpleMenuAppender(String targetMenuGroupId, String id, String icon, String labelKey, String link) {
        this.targetMenuGroupId = targetMenuGroupId;
        this.id = id;
        this.icon = icon;
        this.labelKey = labelKey;
        this.link = link;
    }

    public static SimpleMenuAppender group(String id, String icon, String labelKey) {
        return new SimpleMenuAppender(null, id, icon, labelKey, null);
    }

    public static SimpleMenuAppender link(String id, String icon, String labelKey, String link) {
        return new SimpleMenuAppender(null, id, icon, labelKey, link);
    }

    public static SimpleMenuAppender link
            (String targetMenuGroupId, String id, String icon, String labelKey, String link) {
        return new SimpleMenuAppender(targetMenuGroupId, id, icon, labelKey, link);
    }

    @Override
    public void append(Menu menu) {
        String label = ElementsThreadLocals.getText(labelKey);
        if(this.link == null) {
            appendMenuGroup(menu, label);
        } else {
            appendMenuLink(menu, label);
        }
    }

    protected void appendMenuGroup(Menu menu, String label) {
        MenuGroup group = new MenuGroup(id, icon, label);
        menu.items.add(group);
    }

    protected void appendMenuLink(Menu menu, String label) {
        MenuLink link = new MenuLink(id, icon, label, this.link);
        MenuGroup targetGroup = null;
        if(targetMenuGroupId != null) {
            for(MenuItem item : menu.items) {
                if(targetMenuGroupId.equals(item.id)) {
                    if(item instanceof MenuGroup) {
                        targetGroup = (MenuGroup) item;
                        break;
                    } else {
                        logger.warn("Could not add menu link " + id + " to " + targetMenuGroupId + " because it is a link, not a menu group");
                    }
                }
            }
            if(targetGroup == null) {
                logger.warn("Could not find menu group " + targetMenuGroupId);
            }
        }
        if(targetGroup == null) {
            menu.items.add(link);
        } else {
            targetGroup.menuLinks.add(link);
        }
    }
}
