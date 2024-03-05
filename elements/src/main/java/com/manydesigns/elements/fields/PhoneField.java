/*
 * Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
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

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PhoneField extends RegExpTextField {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    public final static String phoneRegExp = "[0-9+-]+";

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public PhoneField(PropertyAccessor accessor, Mode mode) {
        this(accessor, mode, null);
    }

    public PhoneField(PropertyAccessor accessor, Mode mode, String prefix) {
        super(accessor, mode, prefix, phoneRegExp);
        setErrorString(getText("elements.error.field.phone.format"));
    }
}
