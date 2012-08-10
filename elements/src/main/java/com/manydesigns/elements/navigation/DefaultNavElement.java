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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
