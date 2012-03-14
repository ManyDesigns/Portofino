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
package com.manydesigns.elements.ognl;

import ognl.TypeConverter;

import java.lang.reflect.Member;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class CustomTypeConverter implements TypeConverter {

    private TypeConverter conv;


    public CustomTypeConverter(TypeConverter conv) {
        this.conv = conv;
    }

    public Object convertValue(Map context, Object target, Member member,
            String propertyName, Object value, Class toType) {
        if ((toType == Boolean.class || toType == Boolean.TYPE) && (value instanceof String)) {
            String thisValue = (String) value;
            return Boolean.valueOf(thisValue);
        } else if ((toType == Timestamp.class) && (value instanceof Date)) {
            Date thisValue = (Date) value;
            return new Timestamp(thisValue.getTime());
        } else if ((toType == Timestamp.class) && (value instanceof String)) {
            try {
                return Timestamp.valueOf((String) value);
            } catch (Exception e) {
                return throwFailedConversion(value, toType, e);
            }
        } else if ((toType == Time.class) && (value instanceof Date)) {
            Date thisValue = (Date) value;
            return new Time(thisValue.getTime());
        } else if (toType.isEnum() && value instanceof String){
            return Enum.valueOf(toType, (String) value);
        } else if (toType == Class.class && value instanceof String) {
            try {
                return Class.forName((String) value);
            } catch (Exception e) {
                return throwFailedConversion(value, toType, e);
            }
        } else {
            return conv.convertValue(context, target, member, propertyName, value, toType);
        }
    }

    private Object throwFailedConversion(Object value, Class toType, Exception e) {
        throw new IllegalArgumentException("Unable to convert type " + value.getClass().getName() + " of " + value + " to type of " + toType.getName(), e);
    }
}
