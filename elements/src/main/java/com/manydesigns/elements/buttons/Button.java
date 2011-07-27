/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.buttons;

import com.manydesigns.elements.Element;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Button implements Element {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected String id;
    protected String name;
    protected String value;
    protected String onclick;
    protected Mode mode = Mode.EDIT;

    public Button(String id, String name, String value, String onclick) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.onclick = onclick;
    }

    public Button() {}

    public void readFromRequest(HttpServletRequest req) {}

    public boolean validate() {
        return true;
    }

    public void readFromObject(Object obj) {}

    public void writeToObject(Object obj) {}

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        if (mode.isView()) {
            xb.openElement("input");
            xb.addAttribute("id", id);
            xb.addAttribute("type", "submit");
            xb.addAttribute("name", name);
            xb.addAttribute("value", value);
            xb.addAttribute("class", "submit");
            xb.addAttribute("disabled", "disabled");
            xb.closeElement("input");
        } else if (mode.isEdit() || mode.isPreview()) {
            xb.openElement("input");
            xb.addAttribute("id", id);
            xb.addAttribute("type", "submit");
            xb.addAttribute("name", name);
            xb.addAttribute("value", value);
            xb.addAttribute("class", "submit");
            xb.addAttribute("onclick", onclick);
            xb.closeElement("input");
        } else if (mode.isHidden()) {
            // do nothing
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOnclick() {
        return onclick;
    }

    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }
}
