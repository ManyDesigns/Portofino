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

package com.manydesigns.elements.navigation;

import com.manydesigns.elements.xml.XhtmlBuffer;
import org.jetbrains.annotations.NotNull;

/*
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 */
public class DefaultNavElement implements NavElement {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    private final String text;
    private final String href;
    private final boolean selected;

    public DefaultNavElement(String href, String text, boolean selected) {
        this.text = text;
        this.href = href;
        this.selected = selected;
    }

    public String getText() {
        return text;
    }

    public String getHref() {
        return href;
    }

    public boolean isSelected() {
        return selected;
    }

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.openElement("a");
        xb.addAttribute("href", href);
        xb.openElement("span");
        xb.write(text);
        xb.closeElement("span");
        xb.closeElement("a");
    }
}
