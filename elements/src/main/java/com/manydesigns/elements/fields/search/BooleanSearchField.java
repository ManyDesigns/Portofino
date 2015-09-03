/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.fields.search;

import com.manydesigns.elements.fields.BooleanSearchValue;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class BooleanSearchField extends AbstractSearchField {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected BooleanSearchValue value = BooleanSearchValue.ANY;

    //**************************************************************************
    // Costruttori
    //**************************************************************************

    public BooleanSearchField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public BooleanSearchField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);
    }

    //**************************************************************************
    // SearchField implementation
    //**************************************************************************

    public void toSearchString(StringBuilder sb, String encoding) {
        if (value != null && value != BooleanSearchValue.ANY) {
            appendToSearchString(sb, inputName, value.getStringValue(), encoding);
        }
    }

    public void configureCriteria(Criteria criteria) {
        if (value == null) {
            return;
        }

        switch(value) {
            case ANY:
                break;
            case NULL:
                criteria.isNull(accessor);
                break;
            case FALSE:
                criteria.eq(accessor, Boolean.FALSE);
                break;
            case TRUE:
                criteria.eq(accessor, Boolean.TRUE);
                break;
            default:
                logger.error("Unknown BooleanSearchValue: {}", value.name());
        }
    }

    //**************************************************************************
    // Element implementation
    //**************************************************************************

    public void readFromRequest(HttpServletRequest req) {
        String stringValue = req.getParameter(inputName);
        value = BooleanSearchValue.ANY; // default
        for (BooleanSearchValue current : BooleanSearchValue.values()) {
            if (current.getStringValue().equals(stringValue)) {
                value = current;
            }
        }
    }

    public boolean validate() {
        return true;
    }

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", "form-group boolean-search-field");
        xb.openElement("label");
        xb.addAttribute("class", ATTR_NAME_HTML_CLASS);
        xb.write(StringUtils.capitalize(label));
        xb.closeElement("label");
        xb.openElement("div");
        xb.addAttribute("class", FORM_CONTROL_CSS_CLASS+" radio ");

        for (BooleanSearchValue current : BooleanSearchValue.values()) {
            // don't print null if the attribute is required
            if (required && current == BooleanSearchValue.NULL) {
                continue;
            }
            String idStr = id + "_" + current.name();
            String stringValue = current.getStringValue();
            boolean checked = (value == current);
            xb.writeInputRadio(idStr, inputName, stringValue, checked);
            xb.openElement("label");
            //xb.addAttribute("class", "radio");
            xb.addAttribute("for", idStr);
            String label = getText(current.getLabelI18N());
            xb.write(label);
           // xb.writeNbsp();

            xb.closeElement("label");
            xb.write(" ");
        }
        xb.closeElement("div");
        xb.closeElement("div");
    }

    //**************************************************************************
    // Getter/setter
    //**************************************************************************

    public BooleanSearchValue getValue() {
        return value;
    }

    public void setValue(BooleanSearchValue value) {
        this.value = value;
    }
}
