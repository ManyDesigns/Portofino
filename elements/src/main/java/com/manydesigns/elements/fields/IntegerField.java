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

import com.manydesigns.elements.annotations.MaxIntValue;
import com.manydesigns.elements.annotations.MinIntValue;
import com.manydesigns.elements.reflection.PropertyAccessor;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class IntegerField extends AbstractTextField {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected Integer integerValue;
    protected boolean integerFormatError;

    protected Integer minValue;
    protected Integer maxValue;

    //--------------------------------------------------------------------------
    // Costruttori
    //--------------------------------------------------------------------------
    public IntegerField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public IntegerField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);
        if (accessor.isAnnotationPresent(MinIntValue.class)) {
            minValue = accessor.getAnnotation(MinIntValue.class).value();
        }
        if (accessor.isAnnotationPresent(MaxIntValue.class)) {
            setMaxValue(accessor.getAnnotation(MaxIntValue.class).value());
        }
    }

    //--------------------------------------------------------------------------
    // Implementazione di Component
    //--------------------------------------------------------------------------
    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);

        if (mode.isView(immutable)) {
            return;
        }

        String reqValue = req.getParameter(inputName);
        if (reqValue == null) {
            return;
        }

        stringValue = reqValue.trim();
        integerFormatError = false;
        integerValue = null;

        if (stringValue.length() == 0) {
            return;
        }
        try {
            integerValue = Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            integerFormatError = true;
        }
    }

    @Override
    public boolean validate() {
        if (mode.isView(immutable) || (mode.isBulk() && !bulkChecked)) {
            return true;
        }

        if (!super.validate()) {
            return false;
        }
        if (stringValue == null || stringValue.length() == 0) {
            return true;
        }
        if (integerFormatError) {
            errors.add(getText("elements.error.field.integer.format"));
            return false;
        }
        if (minValue != null && integerValue < minValue) {
            errors.add(getText("elements.error.field.greater.or.equal", minValue));
            return false;
        }
        if (maxValue != null && integerValue > maxValue) {
            errors.add(getText("elements.error.field.less.or.equal", maxValue));
            return false;
        }
        return true;
    }

    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        try {
            if (obj == null) {
                integerValue = null;
            } else {
                integerValue = (Integer)accessor.get(obj);
            }
            if (integerValue == null) {
                stringValue = null;
            } else {
                stringValue = integerValue.toString();
            }
        } catch (IllegalAccessException e) {
            throw new Error(e);
        } catch (InvocationTargetException e) {
            throw new Error(e);
        }
    }

    public void writeToObject(Object obj) {
        writeToObject(obj, integerValue);
    }



    //--------------------------------------------------------------------------
    // Getter/setter
    //--------------------------------------------------------------------------
    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
        if (maxValue != null) {
            // set the text input max size
            if (maxValue < 0) {
                maxValue = -maxValue;
            }
            if (maxValue == 0) {
                maxValue = 1;
            }
            int digits = (int) (Math.log10(maxValue) + 1);
            setMaxLength(digits);
        }
    }
}
