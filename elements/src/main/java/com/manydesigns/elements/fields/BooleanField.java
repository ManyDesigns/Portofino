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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.BooleanUtils;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class BooleanField extends AbstractField<Boolean> {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Costanti
    //**************************************************************************

    public static final String CHECK_PREFIX = "__checkbox_";
    public static final String CHECK_VALUE = "true";

    public static final String NULL_VALUE = null;
    public static final String NULL_LABEL_I18N = "elements.null";

    public static final String TRUE_VALUE = "true";
    public static final String TRUE_LABEL_I18N = "elements.Yes";

    public static final String FALSE_VALUE = "false";
    public static final String FALSE_LABEL_I18N = "elements.No";

    public static final String FORM_CONTROL_CSS_CLASS = "form-control";

    //**************************************************************************
    // Campi
    //**************************************************************************

    protected Boolean booleanValue;
    protected String checkInputName;


    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public BooleanField(PropertyAccessor accessor, Mode mode) {
        this(accessor, mode, null);
    }

    public BooleanField(PropertyAccessor accessor, Mode mode, String prefix) {
        super(accessor, mode, prefix);
        Class type = accessor.getType();
        if (type.isPrimitive()) {
            setRequired(true);
        }
        checkInputName = CHECK_PREFIX + inputName;
    }

    //**************************************************************************
    // Implementazione di Component
    //**************************************************************************
    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);

        if (mode.isView(insertable, updatable)) {
            return;
        }

        String stringValue = req.getParameter(inputName);
        String checkValue = req.getParameter(checkInputName);
        if (stringValue == null && checkValue == null) {
            return;
        }

        setStringValue(stringValue);
    }

    public boolean validate() {
        return true;
    }


    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        if (obj == null) {
            booleanValue = null;
        } else {
            booleanValue = (Boolean)accessor.get(obj);
        }
    }

    public void writeToObject(Object obj) {
        writeToObject(obj, booleanValue);
    }

    public void valueToXhtml(XhtmlBuffer xb) {
        if (mode.isView(insertable, updatable)) {
            valueToXhtmlView(xb);
        } else if (mode.isEdit()) {
            valueToXhtmlEdit(xb);
        } else if (mode.isPreview()) {
            valueToXhtmlPreview(xb);
        } else if (mode.isHidden()) {
            valueToXhtmlHidden(xb);
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    protected void valueToXhtmlEdit(XhtmlBuffer xb) {
        if (required) {
            xb.openElement("div");
            xb.addAttribute("class", "checkbox");

            xb.writeInputCheckbox(id, inputName, TRUE_VALUE, BooleanUtils.isTrue(booleanValue), false, null);
            xb.openElement("label");
            xb.addAttribute("for", id);
            xb.closeElement("label");
            xb.writeInputHidden(checkInputName, CHECK_VALUE);
            xb.closeElement("div");
        } else {
            xb.openElement("select");
            xb.addAttribute("id", id);
            xb.addAttribute("class", FORM_CONTROL_CSS_CLASS);
            xb.addAttribute("name", inputName);
            xb.writeOption(NULL_VALUE, (booleanValue == null),
                    getText(NULL_LABEL_I18N));
            xb.writeOption(TRUE_VALUE, BooleanUtils.isTrue(booleanValue),
                    getText(TRUE_LABEL_I18N));
            xb.writeOption(FALSE_VALUE, BooleanUtils.isFalse(booleanValue),
                    getText(FALSE_LABEL_I18N));
            xb.closeElement("select");
        }
        if(mode.isBulk()) {
            xb.writeJavaScript(
                    "$(function() { " +
                        "configureBulkEditField('" + id + "', '" + bulkCheckboxName + "'); " +
                    "});");
        }
    }

    protected void valueToXhtmlHidden(XhtmlBuffer xb) {
        xb.writeInputHidden(inputName, getStringValue());
        xb.writeInputHidden(checkInputName, CHECK_VALUE);
    }

    protected void valueToXhtmlPreview(XhtmlBuffer xb) {
        valueToXhtmlView(xb);
        valueToXhtmlHidden(xb);
    }

    protected void valueToXhtmlView(XhtmlBuffer xb) {
        xb.openElement("p");
        xb.addAttribute("class", STATIC_VALUE_CSS_CLASS);
        xb.addAttribute("id", id);
        if (href != null) {
            xb.openElement("a");
            xb.addAttribute("href", href);
        }
        xb.write(getDisplayValue());
        if (href != null) {
            xb.closeElement("a");
        }
        xb.closeElement("p");
    }

    public String getStringValue() {
        if (booleanValue == null) {
            return NULL_VALUE;
        } else if (booleanValue) {
            return TRUE_VALUE;
        } else {
            return FALSE_VALUE;
        }
    }

    public void setStringValue(String stringValue) {
        if (TRUE_VALUE.equals(stringValue)) {
            booleanValue = true;
        } else if (FALSE_VALUE.equals(stringValue)) {
            booleanValue = false;
        } else if (required) {
            booleanValue = false;
        } else { // not required
            booleanValue = null;
        }
    }

    @Override
    public String getDisplayValue() {
        String labelI18N;
        if (booleanValue == null) {
            labelI18N = NULL_LABEL_I18N;
        } else if (booleanValue) {
            labelI18N = TRUE_LABEL_I18N;
        } else {
            labelI18N = FALSE_LABEL_I18N;
        }
        return getText(labelI18N);
    }

    //**************************************************************************
    // Getter/setter
    //**************************************************************************
    public Boolean getValue() {
        return booleanValue;
    }

    public void setValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
}
