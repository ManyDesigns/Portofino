/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class BooleanField extends AbstractField {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static final String CHECK_SUFFIX = "_chk";

    protected boolean booleanValue;

    //--------------------------------------------------------------------------
    // Costruttori
    //--------------------------------------------------------------------------
    public BooleanField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public BooleanField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);
    }

    //--------------------------------------------------------------------------
    // Implementazione di Component
    //--------------------------------------------------------------------------
    public void readFromRequest(HttpServletRequest req) {
        if (mode.isView(immutable)) {
            return;
        }

        String checkInputName = inputName + CHECK_SUFFIX;
        if (req.getParameter(checkInputName) == null) {
            return;
        }
        
        String stringValue = req.getParameter(inputName);
        booleanValue = (stringValue != null);
    }

    public boolean validate() {
        return true;
    }


    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        try {
            Boolean tmpValue;
            if (obj == null) {
                tmpValue = null;
            } else {
                tmpValue = (Boolean)accessor.get(obj);
            }
            booleanValue = tmpValue != null && tmpValue;
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }

    public void writeToObject(Object obj) {
        writeToObject(obj, booleanValue);
    }

    public void valueToXhtml(XhtmlBuffer xb) {
        String checkInputName = inputName + CHECK_SUFFIX;

        if (mode.isView(immutable)) {
            valueToXhtmlView(xb);
        } else if (mode.isEdit()) {
            xb.writeInputCheckbox(id, inputName,
                    "checked", booleanValue, false, "checkbox");
            xb.writeInputHidden(checkInputName, "");
        } else if (mode.isPreview()) {
            valueToXhtmlPreview(xb);
        } else if (mode.isHidden()) {
            if (booleanValue) {
                xb.writeInputHidden(inputName, "checked");
            }
            xb.writeInputHidden(checkInputName, "");
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    private void valueToXhtmlPreview(XhtmlBuffer xb) {
        String checkInputName = inputName + CHECK_SUFFIX;

        valueToXhtmlView(xb);
        if (booleanValue) {
            xb.writeInputHidden(inputName, "checked");
        }
        xb.writeInputHidden(checkInputName, "");
    }

    private void valueToXhtmlView(XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", "value");
        xb.addAttribute("id", id);
        if (href != null) {
            xb.openElement("a");
            xb.addAttribute("href", href);
        }
        xb.write(booleanValue
                ? getText("elements.Yes")
                : getText("elements.No"));
        if (href != null) {
            xb.closeElement("a");
        }
        xb.closeElement("div");
    }

    //--------------------------------------------------------------------------
    // Other methods
    //--------------------------------------------------------------------------
    public void labelToXhtml(XhtmlBuffer xb) {
        xb.openElement("label");
        xb.addAttribute("for", id);
        xb.addAttribute("class", "field");
        xb.write(StringUtils.capitalize(label + ":"));
        xb.closeElement("label");
    }

    //--------------------------------------------------------------------------
    // Getter/setter
    //--------------------------------------------------------------------------
    public boolean getBooleanValue() {
        return booleanValue;
    }
}
