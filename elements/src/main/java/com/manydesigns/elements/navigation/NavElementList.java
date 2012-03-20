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

package com.manydesigns.elements.navigation;

import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/*
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 */
public class NavElementList
        extends ArrayList<NavElement>
        implements XhtmlFragment {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    private final String id;

    public NavElementList(String id) {
        this.id = id;
    }

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("id", id);
        listToUl(xb);
        xb.closeElement("div");
    }

    private void listToUl(XhtmlBuffer xb) {
        xb.openElement("ul");
        boolean first = true;
        for (NavElement element : this) {
            String cssClass = null;
            if (first) {
                cssClass = "first-child";
                first = false;
            }
            if (element.isSelected()) {
                if (cssClass == null) {
                    cssClass = "selected";
                } else {
                    cssClass = cssClass + " selected";
                }
            }
            xb.openElement("li");
            if (cssClass != null) {
                xb.addAttribute("class", cssClass);
            }
            element.toXhtml(xb);
            xb.closeElement("li");
        }
        if (first) { // se la lista era vuota mettiamo un elemento fittizio
            xb.openElement("li");
            xb.addAttribute("class", "first-child");
            xb.writeNbsp();
            xb.closeElement("li");
        }
        xb.closeElement("ul");
    }
}
