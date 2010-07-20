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

package com.manydesigns.elements.hyperlinks;

import com.manydesigns.elements.Util;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ExpressionHyperlinkGenerator implements HyperlinkGenerator {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final Pattern pattern = Pattern.compile("\\{[^\\}]*\\}");

    private final String parsedHrefExpression;
    private final List<PropertyAccessor> hrefPropertyAccessors;

    private final String parsedAltExpression;
    private final List<PropertyAccessor> altPropertyAccessors;

    public ExpressionHyperlinkGenerator(ClassAccessor classAccessor,
                                   String hrefExpression,
                                   String altExpression) {
        hrefPropertyAccessors = new ArrayList<PropertyAccessor>();
        parsedHrefExpression =
                parseExpression(classAccessor, hrefExpression,
                        hrefPropertyAccessors);

        // alt can be null
        if (altExpression == null) {
            altPropertyAccessors = null;
            parsedAltExpression = null;
        } else {
            altPropertyAccessors = new ArrayList<PropertyAccessor>();
            parsedAltExpression =
                    parseExpression(classAccessor, altExpression,
                            altPropertyAccessors);
        }
    }

    private String parseExpression(ClassAccessor classAccessor,
                                   String expression,
                                   List<PropertyAccessor> propertyAccessors) {
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
            String name = group.substring(1, group.length()-1);
            PropertyAccessor propertyAccessor;
            try {
                propertyAccessor = classAccessor.getProperty(name);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return null;
            }
            propertyAccessors.add(propertyAccessor);
            previousEnd = end;
        }
        sb.append(expression.substring(previousEnd, expression.length()));
        return sb.toString();
    }

    public String generateHref(Object obj) {
        return Util.getAbsoluteLink(
                runExpression(obj,
                        hrefPropertyAccessors, parsedHrefExpression));
    }

    public String generateAlt(Object obj) {
        return runExpression(obj, altPropertyAccessors, parsedAltExpression);
    }

    protected String runExpression(Object obj,
                                 List<PropertyAccessor> propertyAccessors,
                                 String parsedExpression) {
        if (obj == null) {
            return null;
        }
        try {
            Object[] args = new Object[propertyAccessors.size()];
            for (int i = 0; i < args.length; i++) {
                args[i] = propertyAccessors.get(i).get(obj);
            }
            return MessageFormat.format(parsedExpression, args);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

}
