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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.Mode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class RegExpTextField extends TextField {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final Pattern pattern;
    protected String errorString;

    public RegExpTextField(PropertyAccessor accessor, Mode mode, String regExp) {
        this(accessor, mode, null, regExp);
    }

    public RegExpTextField(PropertyAccessor accessor, Mode mode,
                           String prefix, String regExp) {
        super(accessor, mode, prefix);
        pattern = Pattern.compile(regExp);
        setErrorString(getText("", regExp));
    }

    //**************************************************************************
    // Element implementation
    //**************************************************************************
    @Override
    public boolean validate() {
        if (mode.isView(insertable, updatable) || (mode.isBulk() && !bulkChecked)) {
            return true;
        }

        if (!super.validate()) {
            return false;
        }
        if (stringValue == null || stringValue.length() == 0) {
            return true;
        }
        Matcher matcher = pattern.matcher(stringValue);
        if (!matcher.matches()) {
            errors.add(errorString);
            return false;
        }
        return true;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
    }
}
