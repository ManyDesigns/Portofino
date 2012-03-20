/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.util;

import com.manydesigns.elements.annotations.ShortName;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ShortNameUtils {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static final String PK_ELEMENT_SEPARATOR = " ";

    public static String getName(ClassAccessor classAccessor, Object object) {
        ShortName annotation = classAccessor.getAnnotation(ShortName.class);
        String formatString;
        if (annotation == null) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            // sintetizziamo una stringa a partire dalla chiave primaria
            for (PropertyAccessor propertyAccessor : classAccessor.getKeyProperties()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(PK_ELEMENT_SEPARATOR);
                }
                sb.append(String.format("%%{%s}", propertyAccessor.getName()));
            }
            formatString = sb.toString();
        } else {
            formatString = annotation.value();
        }
        OgnlTextFormat ognlTextFormat = OgnlTextFormat.create(formatString);
        return ognlTextFormat.format(object);
    }
}
