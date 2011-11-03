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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.Password;
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
public class PasswordField extends TextField {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static String PASSWORD_PLACEHOLDER = "********";

    private String confirmationValue;
    private boolean confirmationRequired = false;

    //**************************************************************************
    // Constructors
    //**************************************************************************
    public PasswordField(PropertyAccessor accessor, Mode mode) {
        this(accessor, mode, null);
        confirmationRequired = accessor.getAnnotation(Password.class)
                .confirmationRequired();
    }

    public PasswordField(PropertyAccessor accessor, Mode mode, String prefix) {
        super(accessor, mode, prefix);
        confirmationRequired = accessor.getAnnotation(Password.class)
                .confirmationRequired();
    }

    //**************************************************************************
    // Field override
    //**************************************************************************
    @Override
    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);

        if (mode.isView(insertable, updatable)) {
            return;
        }

        String confirmationInputName = inputName + "_confirm";

        String reqValue = req.getParameter(confirmationInputName);
        if (reqValue == null) {
            return;
        }

        confirmationValue = reqValue.trim();
        if (autoCapitalize) {
            confirmationValue = confirmationValue.toUpperCase();
        }
    }

    @Override
    public boolean validate() {
        if (mode.isView(insertable, updatable) || (mode.isBulk() && !bulkChecked)) {
            return true;
        }

        if (!super.validate()) {
            return false;
        }

        if (!confirmationRequired) {
            return true;
        }

        if (StringUtils.equals(stringValue, confirmationValue)) {
            errors.add(getText("elements.error.field.passwords.dont.match"));
            return false;
        }

        return true;
    }

    @Override
    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        confirmationValue = stringValue;
    }

    @Override
    public void toXhtml(@NotNull XhtmlBuffer xb) {
        if (mode.isEdit() && (mode.isCreate() || updatable)) { //was !immutable
            // print out regular input field
            xb.openElement("th");
            labelToXhtml(xb);
            xb.closeElement("th");
            xb.openElement("td");
            valueToXhtml(xb, id, inputName, stringValue);
            if (confirmationRequired) {
                // print out confirmation input field
                String confirmationHtmlId = id + "_confirm";
                String confirmationInputName = inputName + "_confirm";
                xb.writeNbsp();
                xb.write(ElementsThreadLocals
                        .getText("elements.field.password.confirm"));
                xb.write(":");
                valueToXhtml(xb, confirmationHtmlId,
                        confirmationInputName, confirmationValue);
            }
            helpToXhtml(xb);
            errorsToXhtml(xb);
            xb.closeElement("td");
        } else {
            super.toXhtml(xb);
        }
    }

    @Override
    public void labelToXhtml(XhtmlBuffer xb) {
        labelToXhtml(xb, id, label);
    }

    public void labelToXhtml(XhtmlBuffer xb, String actualHtmlId,
                             String actualLabel) {
        xb.openElement("label");
        xb.addAttribute("for", actualHtmlId);
        xb.addAttribute("class", "field");
        if (required && (mode.isEdit())) {
            xb.openElement("span");
            xb.addAttribute("class", "required");
            xb.write("*");
            xb.closeElement("span");
            xb.writeNbsp();
        }
        xb.write(StringUtils.capitalize(actualLabel + ":"));
        xb.closeElement("label");
    }

    @Override
    public void valueToXhtml(XhtmlBuffer xb) {
        valueToXhtml(xb, id, inputName, stringValue);
    }

    public void valueToXhtml(XhtmlBuffer xb, String actualHtmlId,
                             String actualInputName, String actualStringValue) {
        if (mode.isView(insertable, updatable)) {
            valueToXhtmlView(xb);
        } else if (mode.isEdit()) {
            xb.openElement("input");
            xb.addAttribute("type", "password");
            xb.addAttribute("class", "text");
            xb.addAttribute("id", actualHtmlId);
            xb.addAttribute("name", actualInputName);
            xb.addAttribute("value", actualStringValue);
            if (maxLength != null) {
                int textInputSize = (size != null) && (maxLength > size)
                        ? size
                        : maxLength;
                xb.addAttribute("maxlength",
                        Integer.toString(maxLength));
                xb.addAttribute("size",
                        Integer.toString(textInputSize));
            }
            xb.closeElement("input");
        } else if (mode.isPreview()) {
            valueToXhtmlPreview(xb, actualInputName, actualStringValue);
        } else if (mode.isHidden()) {
            xb.writeInputHidden(actualInputName, actualStringValue);
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    protected void valueToXhtmlPreview(XhtmlBuffer xb,
                                     String actualInputName,
                                     String actualStringValue) {
        valueToXhtmlView(xb);
        xb.writeInputHidden(actualInputName, actualStringValue);
    }

    protected void valueToXhtmlView(XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", "value");
        xb.addAttribute("id", id);
        xb.write(PASSWORD_PLACEHOLDER);
        xb.closeElement("div");
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public String getConfirmationValue() {
        return confirmationValue;
    }

    public void setConfirmationValue(String confirmationValue) {
        this.confirmationValue = confirmationValue;
    }

    public boolean isConfirmationRequired() {
        return confirmationRequired;
    }

    public void setConfirmationRequired(boolean confirmationRequired) {
        this.confirmationRequired = confirmationRequired;
    }
}
