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

package com.manydesigns.elements.util;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.logging.LogUtil;
import ognl.OgnlContext;
import ognl.TypeConverter;
import org.apache.commons.lang.text.StrTokenizer;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Util {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected final static String STRING_PATTERN = "\"(([^\"\\\\]|\\\\.)*)\"";

    protected final static Pattern stringPattern =
            Pattern.compile(STRING_PATTERN);

    protected final static Pattern stringArrayPattern =
            Pattern.compile("\\s*\\{\\s*(" + STRING_PATTERN +
                    "\\s*(,\\s*" + STRING_PATTERN + "\\s*)*)?\\}\\s*");

    protected final static Pattern pattern = Pattern.compile("\\p{Alpha}*:");

    public static final Logger logger = LogUtil.getLogger(Util.class);

    public static String getAbsoluteUrl(HttpServletRequest req,
                                        String url) {
        StringBuilder sb = new StringBuilder();

        Matcher matcher = pattern.matcher(url);
        if (matcher.lookingAt()) {
            return url;
        }

        if (!"/".equals(req.getContextPath())) {
            sb.append(req.getContextPath());
        }

        if (!url.startsWith("/")) {
            sb.append("/");
        }

        sb.append(url);

        return sb.toString();
    }

    public static String getAbsoluteUrl(String url) {
        HttpServletRequest req =
                ElementsThreadLocals.getHttpServletRequest();

        return getAbsoluteUrl(req, url);
    }

    public static String camelCaseToWords(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (first) {
                first = false;
                sb.append(c);
            } else {
                if (Character.isUpperCase(c)) {
                    sb.append(' ');
                    sb.append(Character.toLowerCase(c));
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }


    public static String urlencode(String s) {
        if (s == null) {
            return null;
        } else {
            try {
                return URLEncoder.encode(s, "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                throw new Error(e);
            }
        }
    }

    public static void printMatcher(Matcher matcher) {
        for (int i = 0; i <= matcher.groupCount(); i++) {
            LogUtil.fineMF(logger, "group {0}: {1}", i, matcher.group(i));
        }
    }


    public static String[] matchStringArray(String text) {
        StrTokenizer strTokenizer = StrTokenizer.getCSVInstance(text);
        return strTokenizer.getTokenArray();
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


    public static boolean isNumericType(Class type) {
        return Number.class.isAssignableFrom(type)
                || type == Integer.TYPE
                || type == Byte.TYPE
                || type == Short.TYPE
                || type == Long.TYPE
                || type == Float.TYPE
                || type == Double.TYPE;
    }
}
