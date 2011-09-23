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

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.FieldSize;
import com.manydesigns.elements.annotations.MaxLength;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringUtils;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class AbstractTextField extends AbstractField {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected String stringValue;
    protected boolean autoCapitalize = false;
    protected Integer maxLength = null;

    protected Integer size;

    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public AbstractTextField(PropertyAccessor accessor, Mode mode) {
        this(accessor, mode, null);
    }

    public AbstractTextField(PropertyAccessor accessor, Mode mode, String prefix) {
        super(accessor, mode, prefix);
        if (accessor.isAnnotationPresent(MaxLength.class)) {
            maxLength = accessor.getAnnotation(MaxLength.class).value();
        }
        if (accessor.isAnnotationPresent(FieldSize.class)) {
            size = accessor.getAnnotation(FieldSize.class).value();
        }
    }

    //**************************************************************************
    // Implementazione di Element
    //**************************************************************************
    public boolean validate() {
        if (mode.isView(insertable, updatable) || (mode.isBulk() && !bulkChecked)) {
            return true;
        }

        boolean result = true;
        if (required && StringUtils.isBlank(stringValue)) {
            errors.add(getText("elements.error.field.required"));
            result = false;
        }
        if (maxLength != null && StringUtils.length(stringValue) > maxLength) {
            errors.add(getText("elements.error.field.length.exceeded", maxLength));
            result = false;
        }
        return result;
    }

    //**************************************************************************
    // Field implementation
    //**************************************************************************
    public void valueToXhtml(XhtmlBuffer xb) {
        if (mode.isView(insertable, updatable)) {
            valueToXhtmlView(xb);
        } else if (mode.isEdit()) {
            valueToXhtmlEdit(xb);
        } else if (mode.isPreview()) {
            valueToXhtmlPreview(xb);
        } else if (mode.isHidden()) {
            xb.writeInputHidden(id, inputName, stringValue);
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    protected void valueToXhtmlEdit(XhtmlBuffer xb) {
        xb.writeInputText(id, inputName, stringValue,
                fieldCssClass, size, maxLength);
    }

    protected void valueToXhtmlPreview(XhtmlBuffer xb) {
        valueToXhtmlView(xb);
        xb.writeInputHidden(inputName, stringValue);
    }

    protected void valueToXhtmlView(XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", fieldCssClass);
        xb.addAttribute("id", id);
        if (href == null) {
            xb.write(stringValue);
        } else {
            xb.writeAnchor(href, stringValue, null, title);
        }
        xb.closeElement("div");
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************
    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public boolean isAutoCapitalize() {
        return autoCapitalize;
    }

    public void setAutoCapitalize(boolean autoCapitalize) {
        this.autoCapitalize = autoCapitalize;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
