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

import com.manydesigns.elements.ElementsThreadLocals;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class OgnlUtils {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

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
            logger.warn("Error during evaluation of ognl expression: " +
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
}
