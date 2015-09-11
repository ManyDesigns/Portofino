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

package com.manydesigns.elements.ognl;

import com.manydesigns.elements.ElementsThreadLocals;
import ognl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class OgnlUtils {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(OgnlUtils.class);

    public static Object getValueQuietly(String expression,
                                         Map ognlContext,
                                         Object root) {
        Object parsedOgnlExpression = parseExpressionQuietly(expression);
        return getValueQuietly(
                parsedOgnlExpression, ognlContext, root);
    }

    public static Object parseExpressionQuietly(String expression) {
        if (expression == null) {
            logger.warn("Null expression");
            return null;
        }

        Object result;
        try {
            result = Ognl.parseExpression(expression);
        } catch (OgnlException e) {
            result = null;
            logger.warn("Error during parsing of ognl expression: " +
                    expression, e);
        }
        return result;
    }

    public static Object getValueQuietly(Object parsedExpression,
                                         Map ognlContext, Object root) {
        if (parsedExpression == null) {
            logger.warn("Null parsed expression");
            return null;
        }

        Object result;
        try {
            if (ognlContext == null) {
                result = Ognl.getValue(parsedExpression, root);
            } else {
                result = Ognl.getValue(parsedExpression, ognlContext, root);
            }
        } catch (OgnlException e) {
            result = null;
            logger.debug("Error during evaluation of ognl expression: " +
                    parsedExpression.toString(), e);
        }
        return result;
    }

    public static String convertValueToString(Object value) {
        return (String) convertValue(value, String.class);
    }

    public static Object convertValue(Object value, Class toType) {
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        TypeConverter typeConverter = ognlContext.getTypeConverter();

        return typeConverter.convertValue(
                ognlContext, null, null, null, value, toType);
    }

    public static Object convertValueQuietly(Object value, Class toType) {
        try {
            return convertValue(value, toType);
        } catch (Throwable e) {
            logger.debug("Error during conversion of value: " + value, e);
            return null;
        }
    }

    public static void clearCache() {
        OgnlRuntime.clearCache();
        clearOGNLCache("cacheGetMethod");
        clearOGNLCache("cacheSetMethod");
    }

    protected static void clearOGNLCache(String fieldName) {
        try {
            Field field = OgnlRuntime.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Map ognlCache = (Map) field.get(null);
            synchronized (ognlCache) {
                ognlCache.clear();
            }
        } catch (Exception e) {
            logger.warn("Could not clear OGNL cache " + fieldName, e);
        }
    }

}
