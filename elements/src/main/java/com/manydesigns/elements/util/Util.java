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

package com.manydesigns.elements.util;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import org.apache.commons.lang.text.StrTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Util {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected final static String STRING_PATTERN = "\"(([^\"\\\\]|\\\\.)*)\"";

    protected final static Pattern stringPattern =
            Pattern.compile(STRING_PATTERN);

    protected final static Pattern stringArrayPattern =
            Pattern.compile("\\s*\\{\\s*(" + STRING_PATTERN +
                    "\\s*(,\\s*" + STRING_PATTERN + "\\s*)*)?\\}\\s*");

    protected final static Pattern pattern = Pattern.compile("\\p{Alpha}*:");

    public static final Logger logger = LoggerFactory.getLogger(Util.class);

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

    public static String guessToWords(String s) {
        if (s == null) {
            return null;
        }
        if (isAllLowerCase(s) || isAllUpperCase(s)) {
            return letterOrDigitToWords(s);
        } else {
            return camelCaseToWords(s);
        }
    }

    public static String letterOrDigitToWords(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    static boolean isAllUpperCase(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c) && Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }

    static boolean isAllLowerCase(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c) && Character.isUpperCase(c)) {
                return false;
            }
        }
        return true;
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
            logger.debug("group {}: {}", i, matcher.group(i));
        }
    }


    public static String[] matchStringArray(String text) {
        StrTokenizer strTokenizer = StrTokenizer.getCSVInstance(text);
        return strTokenizer.getTokenArray();
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

    public static String elementToString(XhtmlFragment element) {
        XhtmlBuffer xb = new XhtmlBuffer();
        element.toXhtml(xb);
        return xb.toString();
    }

// confronto fra due strighe che tiene conto dei numeri.
    // cioè "pippo1" < "pippo2" < "pippo10"
    // il confronto è case insensitive
    public static int compare(
            String one,
            String two) {

        if(one == null && two == null) {
            return 0;
        }
        if (two == null)
            return -1;
        if (one == null)
            return 1;

        int lenone = one.length();
        int lentwo = two.length();
        StringBuilder numberOne = new StringBuilder();
        StringBuilder numberTwo = new StringBuilder();
        int i = 0, j = 0;

        for (; i < lenone && j < lentwo; i++, j++) {

            if (Character.isDigit(one.charAt(i))) {
                if (Character.isDigit(two.charAt(j))) {
                    numberOne.setLength(0);
                    numberTwo.setLength(0);

                    while ((i < lenone) && Character.isDigit(one.charAt(i))) {
                        numberOne.append(one.charAt(i));
                        i++;
                    }
                    while ((j < lentwo) && Character.isDigit(two.charAt(j))) {
                        numberTwo.append(two.charAt(j));
                        j++;
                    }
                    long diff = Long.parseLong(numberOne.toString()) -
                            Long.parseLong(numberTwo.toString());
                    if (diff > 0) {
                        return 1;
                    } else if (diff < 0) {
                        return -1;
                    }
                } else {
                    return -1;
                }
            } else if (Character.isDigit(two.charAt(j))) {
                return 1;
            } else {
                int diff = Character.toUpperCase(one.charAt(i)) -
                        Character.toUpperCase(two.charAt(j));
                if (diff != 0)
                    return diff;
            }
        }

        return lenone - lentwo;
    }

}
