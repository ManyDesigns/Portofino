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
import ognl.ClassResolver;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class AbstractOgnlFormat {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    public static final Pattern pattern = Pattern.compile("%\\{[^\\}]*\\}");

    protected final String formatString;
    protected final String[] ognlExpressions;
    protected final Object[] parsedOgnlExpressions;

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractOgnlFormat.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    protected AbstractOgnlFormat(String ognlFormat) {
        List<String> ognlExpressionList = new ArrayList<String>();
        List<Object> parsedOgnlExpressionList = new ArrayList<Object>();
        Matcher m = pattern.matcher(ognlFormat);
        int previousEnd = 0;
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (m.find()) {
            int start = m.start();
            sb.append(ognlFormat.substring(previousEnd, start));
            String group = m.group();
            String ognlExpression = group.substring(2, group.length()-1);
            replaceOgnlExpression(sb, index, ognlExpression);
            int end = m.end();
            try {
                Object parsedOgnlExpression =
                        Ognl.parseExpression(ognlExpression);
                ognlExpressionList.add(ognlExpression);
                parsedOgnlExpressionList.add(parsedOgnlExpression);
            } catch (OgnlException e) {
                String msg = MessageFormat.format(
                        "Could not parse: {0}", ognlExpression);
                logger.warn(msg, e);
                throw new IllegalArgumentException(msg);
            }
            previousEnd = end;
            index++;
        }
        sb.append(ognlFormat.substring(previousEnd, ognlFormat.length()));

        formatString = sb.toString();

        ognlExpressions = new String[ognlExpressionList.size()];
        ognlExpressionList.toArray(ognlExpressions);

        parsedOgnlExpressions =
                new Object[parsedOgnlExpressionList.size()];
        parsedOgnlExpressionList.toArray(parsedOgnlExpressions);
    }

    protected abstract void replaceOgnlExpression(StringBuilder sb,
                                       int index,
                                       String ognlExpression);

    public Object[] evaluateOgnlExpressions(Object root) {
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        ClassResolver cr = ognlContext.getClassResolver();
        Object[] result = new Object[parsedOgnlExpressions.length];
        for (int i = 0; i < result.length; i++) {
            evaluateOneOgnlExpression(root, ognlContext, result, i);
        }
        return result;
    }

    protected void evaluateOneOgnlExpression(Object root,
                                           Map ognlContext,
                                           Object[] result,
                                           int i) {
        String ognlExpression = ognlExpressions[i];
        Object parsedOgnlExpression = parsedOgnlExpressions[i];
        try {
            if (ognlContext == null) {
                result[i] =
                        Ognl.getValue(parsedOgnlExpression, root);
            } else {
                result[i] =
                        Ognl.getValue(parsedOgnlExpression, ognlContext, root);
            }
        } catch (Throwable e) {
            logger.warn("Error during evaluation of ognl expression: " +
                    ognlExpression, e);
        }
    }

    //**************************************************************************
    // Getters and setters
    //**************************************************************************

    public String getFormatString() {
        return formatString;
    }

    public String[] getOgnlExpressions() {
        return ognlExpressions;
    }

    public Object[] getParsedOgnlExpressions() {
        return parsedOgnlExpressions;
    }

}
