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

package com.manydesigns.elements.text;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.Util;
import ognl.Ognl;
import ognl.OgnlException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ExpressionGenerator implements Generator {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    public static final Pattern pattern = Pattern.compile("%\\{[^\\}]*\\}");

    protected final String parsedExpression;
    protected final Object[] ognlExpressions;
    protected boolean url = false;

    public static final Logger logger =
            LogUtil.getLogger(ExpressionGenerator.class);

    //**************************************************************************
    // Static initialization/methods
    //**************************************************************************

    public static ExpressionGenerator create(String expression) {
        List<Object> ognlExpressions = new ArrayList<Object>();
        Matcher m = pattern.matcher(expression);
        int previousEnd = 0;
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (m.find()) {
            int start = m.start();
            sb.append(expression.substring(previousEnd, start));
            sb.append("{");
            sb.append(index);
            sb.append("}");
            int end = m.end();
            String group = m.group();
            String ognlString = group.substring(2, group.length()-1);
            try {
                Object ognlExpression = Ognl.parseExpression(ognlString);
                ognlExpressions.add(ognlExpression);
            } catch (OgnlException e) {
                LogUtil.warningMF(logger,
                        "Could not parse: {0}", e , ognlString);
                return null;
            }
            previousEnd = end;
            index++;
        }
        sb.append(expression.substring(previousEnd, expression.length()));

        String parsedExpression = sb.toString();

        Object[] ognlExpressionsArray = new Object[ognlExpressions.size()];
        ognlExpressions.toArray(ognlExpressionsArray);

        return new ExpressionGenerator(parsedExpression, ognlExpressionsArray);
    }

    public static String generate(String expression, Object root) {
        return create(expression).generate(root);
    }

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public ExpressionGenerator(String parsedExpression,
                               Object[] ognlExpressions) {
        this.parsedExpression = parsedExpression;
        this.ognlExpressions = ognlExpressions;
    }

    //**************************************************************************
    // Generator implementation
    //**************************************************************************

    public String generate(Object root) {
        Map ognlContext = ElementsThreadLocals.getOgnlContext();
        try {
            String[] args = new String[ognlExpressions.length];
            for (int i = 0; i < args.length; i++) {
                Object ognlExpression = ognlExpressions[i];
                String value;
                if (ognlContext == null) {
                    value = (String) Ognl.getValue(
                            ognlExpression, root, String.class);
                } else {
                    value = (String) Ognl.getValue(
                            ognlExpression, ognlContext, root, String.class);
                }
                args[i] = url ? Util.urlencode(value) : value;
            }
            String result = MessageFormat.format(parsedExpression, args);
            if (url) {
                result = Util.getAbsoluteUrl(result);
            }
            return result;
        } catch (Throwable e) {
            LogUtil.warning(logger, "Error during expression generation", e);
            return null;
        }
    }

    //**************************************************************************
    // Getters and setters
    //**************************************************************************

    public boolean isUrl() {
        return url;
    }

    public void setUrl(boolean url) {
        this.url = url;
    }

    public String getParsedExpression() {
        return parsedExpression;
    }

    public Object[] getOgnlExpressions() {
        return ognlExpressions;
    }
}
