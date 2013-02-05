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

package com.manydesigns.elements.util;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import org.apache.commons.lang.text.StrTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final static String STRING_PATTERN = "\"(([^\"\\\\]|\\\\.)*)\"";

    protected final static Pattern stringPattern =
            Pattern.compile(STRING_PATTERN);

    protected final static Pattern stringArrayPattern =
            Pattern.compile("\\s*\\{\\s*(" + STRING_PATTERN +
                    "\\s*(,\\s*" + STRING_PATTERN + "\\s*)*)?\\}\\s*");

    protected final static Pattern pattern = Pattern.compile("\\p{Alpha}*:");

    protected final static Pattern BAD_UNICODE_CHARS_PATTERN = Pattern.compile(
            "[\u2013\u2014\u2018\u2019\u0092\u201C\u201D\u2022\u2026\u0093\u0094]",
            Pattern.CASE_INSENSITIVE);

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
    // cioe' "pippo1" < "pippo2" < "pippo10"
    // il confronto e' case insensitive
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

    public static <T> T[] copyOfRange(T[] original, int from, int to) {
        return copyOfRange(original, from, to, (Class<T[]>) original.getClass());
    }

    public static <T,U> T[] copyOfRange(U[] original, int from, int to, Class<? extends T[]> newType) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        T[] copy = ((Object)newType == (Object)Object[].class)
                ? (T[]) new Object[newLength]
                : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, from, copy, 0,
                Math.min(original.length - from, newLength));
        return copy;
    }

    public static StringBuilder replaceBadUnicodeCharacters(String source, StringBuilder dest) {
        Matcher m = BAD_UNICODE_CHARS_PATTERN.matcher(source);
        int index = 0;
        while (m.find()) {
            String g = m.group();
            dest.append(source.substring(index, m.end() - 1));
            index = m.end();
            if (g.equals("\u2013") || g.equals("\u2014")) {
                dest.append('-');
            } else if (g.equals("\u2018") || g.equals("\u2019")|| g.equals("\u0092")) {
                dest.append('\'');
            } else if (g.equals("\u201C") || g.equals("\u201D")) {
                dest.append('"');
            } else if (g.equals("\u2022")) {
                dest.append('*');
            } else if (g.equals("\u2026")) {
                dest.append("...");
            } else if (g.equals("\u0093")) {
                dest.append("<<");
            } else if (g.equals("\u0094")) {
                dest.append(">>");
            }
        }
        dest.append(source.substring(index, source.length()));
        return dest;
    }

    public static StringBuilder replaceBadUnicodeCharactersWithHtmlEntities(String source, StringBuilder dest) {
        Matcher m = BAD_UNICODE_CHARS_PATTERN.matcher(source);
        int index = 0;
        while (m.find()) {
            String g = m.group();
            dest.append(source.substring(index, m.end() - 1));
            index = m.end();
            if(g.equals("\u2013")) {
                dest.append("&ndash;");
            } else if(g.equals("\u2014")) {
                dest.append("&mdash;");
            } else if(g.equals("\u2018")) {
                dest.append("&lsquo;");
            } else if(g.equals("\u2019")) {
                dest.append("&rsquo;");
            } else if(g.equals("\u0092")) {
                dest.append("'");
            } else if (g.equals("\u201C")) {
                dest.append("&ldquo;");
            } else if(g.equals("\u201D")) {
                dest.append("&rdquo;");
            } else if (g.equals("\u2022")) {
                dest.append("&bull;");
            } else if (g.equals("\u2026")) {
                dest.append("&hellip;");
            } else if (g.equals("\u0093")) {
                dest.append("&lt;&lt;");
            } else if (g.equals("\u0094")) {
                dest.append("&gt;&gt;");
            }
        }
        dest.append(source.substring(index, source.length()));
        return dest;
    }

    public static String replaceBadUnicodeCharacters(String source) {
        return replaceBadUnicodeCharacters(source, new StringBuilder()).toString();
    }

    public static String replaceBadUnicodeCharactersWithHtmlEntities(String source) {
        return replaceBadUnicodeCharactersWithHtmlEntities(source, new StringBuilder()).toString();
    }
}
