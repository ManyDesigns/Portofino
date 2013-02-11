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
        } else if (toType == Timestamp.class) {
            if(value instanceof String) {
                try {
                    return Timestamp.valueOf((String) value);
                } catch (Exception e) {
                    return throwFailedConversion(value, toType, e);
                }
            } else if(value instanceof Date) {
                return new Timestamp(((Date) value).getTime());
            }
        } else if (toType == java.sql.Date.class) {
            if (value instanceof String) {
                try {
                    return java.sql.Date.valueOf((String) value);
                } catch (Exception e) {
                    return throwFailedConversion(value, toType, e);
                }
            } else if(value instanceof Date) {
                return new java.sql.Date(((Date) value).getTime());
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
        }
        return conv.convertValue(context, target, member, propertyName, value, toType);
    }

    private Object throwFailedConversion(Object value, Class toType, Exception e) {
        throw new IllegalArgumentException("Unable to convert type " + value.getClass().getName() + " of " + value + " to type of " + toType.getName(), e);
    }
}
