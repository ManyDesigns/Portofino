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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.HighlightLinks;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.annotations.RichText;
import com.manydesigns.elements.annotations.Status;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class TextField extends AbstractTextField {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public final static Pattern linkPattern =
            Pattern.compile("(http://|https://|ftp://|www\\.)\\S+", Pattern.CASE_INSENSITIVE);
    public final static Pattern emailPattern =
            Pattern.compile("[a-z0-9\\-_]++(\\.[a-z0-9\\-_]++)*@[a-z0-9\\-_]++" +
                    "(\\.[a-z0-9\\-_]++)++", Pattern.CASE_INSENSITIVE);

    protected boolean highlightLinks = false;
    protected boolean multiline = false;
    protected boolean richText = false;
    protected Integer textAreaWidth;
    protected int textAreaMinRows = 4;
    protected String[] red;
    protected String[] amber;
    protected String[] green;

    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public TextField(PropertyAccessor accessor, Mode mode) {
        this(accessor, mode, null);
    }

    public TextField(PropertyAccessor accessor, Mode mode, String prefix) {
        super(accessor, mode, prefix);

        HighlightLinks highlightLinksAnnotation =
                accessor.getAnnotation(HighlightLinks.class);
        if (highlightLinksAnnotation != null) {
            highlightLinks = highlightLinksAnnotation.value();
            logger.debug("HighlightLinks annotation present with value: {}",
                    highlightLinks);
        }

        Multiline multilineAnnotation = accessor.getAnnotation(Multiline.class);
        if (multilineAnnotation != null) {
            multiline = multilineAnnotation.value();
            logger.debug("Multiline annotation present with value: {}",
                    multiline);
        }

        RichText richTextAnnotation = accessor.getAnnotation(RichText.class);
        if (richTextAnnotation != null) {
            multiline = richTextAnnotation.value();
            richText = richTextAnnotation.value();
            logger.debug("RichText annotation present with value: {}",
                    multiline);
        }
        
        if (accessor.isAnnotationPresent(Status.class)) {
            Status annotation = accessor.getAnnotation(Status.class);
            red = annotation.red();
            amber = annotation.amber();
            green = annotation.green();
        }
    }

    //**************************************************************************
    // Implementazione/override di AbstractTextField
    //**************************************************************************
    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);

        if (mode.isView(insertable, updatable)) {
            return;
        }

        String reqValue = req.getParameter(inputName);
        if (reqValue == null) {
            return;
        }

        stringValue = reqValue.trim();
        if(replaceBadUnicodeCharacters) {
            stringValue = Util.replaceBadUnicodeCharacters(stringValue);
        }
        if (autoCapitalize) {
            stringValue = stringValue.toUpperCase();
        }
    }

    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        if (obj == null) {
            stringValue = null;
        } else {
            stringValue = (String)accessor.get(obj);
        }
    }

    public void writeToObject(Object obj) {
        writeToObject(obj, stringValue);
    }

    protected void valueToXhtmlEdit(XhtmlBuffer xb) {
        if (multiline) {
            xb.openElement("textarea");
            xb.addAttribute("id", id);
            xb.addAttribute("name", inputName);
            String htmlClass = fieldCssClass;
            if(textAreaWidth != null) {
                xb.addAttribute("cols", Integer.toString(textAreaWidth));
                xb.addAttribute("rows", Integer.toString(numRowTextArea(stringValue, textAreaWidth)));
                htmlClass += " mde-text-field-with-explicit-size";
            } else {
                xb.addAttribute("rows", Integer.toOctalString(textAreaMinRows));
            }
            if(richText) {
                htmlClass += " mde-form-rich-text";
            }
            if(!StringUtils.isEmpty(htmlClass)) {
                xb.addAttribute("class", htmlClass);
            }
            xb.write(stringValue);
            xb.closeElement("textarea");
        } else {
            super.valueToXhtmlEdit(xb);
        }
    }

    protected void valueToXhtmlPreview(XhtmlBuffer xb) {
        valueToXhtmlView(xb);
        xb.writeInputHidden(inputName, stringValue);
    }

    protected void valueToXhtmlView(XhtmlBuffer xb) {
        xb.openElement("div");
        String cssClass = "value";
        if (ArrayUtils.contains(red, stringValue)) {
            cssClass += " status_red";
        } else if (ArrayUtils.contains(amber, stringValue)) {
            cssClass += " status_amber";
        } else if (ArrayUtils.contains(green, stringValue)) {
            cssClass += " status_green";
        }
        xb.addAttribute("class", cssClass);
        xb.addAttribute("id", id);
        String escapedText = getDisplayValue();
        if (href != null) {
            xb.openElement("a");
            xb.addAttribute("href", href);
            xb.addAttribute("alt", title);
        }
        xb.writeNoHtmlEscape(escapedText);
        if (href != null) {
            xb.closeElement("a");
        }
        xb.closeElement("div");
    }

    public String getDisplayValue() {
        String escapedText;
        if(richText) {
            escapedText = stringValue;
        } else {
            escapedText = StringEscapeUtils.escapeHtml(stringValue);
            if(href == null && highlightLinks) {
                escapedText = highlightLinks(escapedText);
            }
            escapedText = adjustText(escapedText);
        }
        return escapedText;
    }

    public String getValue() {
        return stringValue;
    }

    //**************************************************************************
    // Other methods
    //**************************************************************************

    public static String adjustText(String str) {
        if (str == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        int i;
        for (i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '\n') {
                buf.append("<br />");
// commentate le due righe successive perche' creavano troppe righe che
// eccedevano in lunghezza dal lato destro della pagina.
//            } else if ( ch == ' ' ){
//                buf.append("&nbsp;");
            } else if (ch == '\t') {
                buf.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    public static String highlightLinks(String str) {
        if (str == null) {
            return null;
        }
        // Pattern Matching will be case insensitive.
        Matcher linkMatcher = linkPattern.matcher(str);

        boolean linkTrovato = false;
        StringBuffer sb = new StringBuffer();
        while (linkMatcher.find()) {
            String text = shortenEscaped(linkMatcher.group(0), 22);
            if (linkMatcher.group(1).equalsIgnoreCase("www.")) {
                linkMatcher.appendReplacement(sb, "<a href=\"http://" +
                        linkMatcher.group(0) + "\">" + text + "</a>");
            } else {
                linkMatcher.appendReplacement(sb, "<a href=\"" + linkMatcher.group(0) +
                        "\">" + text + "</a>");
            }
            linkTrovato = true;
        }
        if (linkTrovato) {
            linkMatcher.appendTail(sb);
            str = sb.toString();
            linkTrovato = false;
            sb = new StringBuffer();
        }

        //mail
        Matcher emailMatcher = emailPattern.matcher(str);
        while (emailMatcher.find()) {
            emailMatcher.appendReplacement(sb, "<a href=\"mailto:" +
                    emailMatcher.group(0) + "\">" + emailMatcher.group(0) + "</a>");
            linkTrovato = true;
        }
        if (linkTrovato) {
            emailMatcher.appendTail(sb);
            str = sb.toString();
        }
        return str;
    }

    public static String shortenEscaped(String text, int maxlen) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        int count = 0;
        boolean inEntity = false;
        while (pos < text.length()) {
            char c = text.charAt(pos);
            if (c == '&')
                inEntity = true;
            if (c == ';')
                inEntity = false;
            sb.append(c);
            if (!inEntity)
                count = count + 1;
            pos = pos + 1;
            if (count >= maxlen)
                break;
        }
        if (pos < text.length())
            sb.append("...");
        return sb.toString();
    }

    private int numRowTextArea(String stringValue, int cols) {
        if (stringValue == null)
            return textAreaMinRows;

        String dim[] = stringValue.split("\n");
        int rows = 0;
        for (String aDim : dim) {
            if (aDim.length() >= cols)
                rows += aDim.length() / cols;
        }
        rows += dim.length;
        if (rows < textAreaMinRows)
            rows = textAreaMinRows;
        return rows;
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************
    public boolean isHighlightLinks() {
        return highlightLinks;
    }

    public void setHighlightLinks(boolean highlightLinks) {
        this.highlightLinks = highlightLinks;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public boolean isRichText() {
        return richText;
    }

    public void setRichText(boolean richText) {
        this.richText = richText;
    }

    public int getTextAreaWidth() {
        return textAreaWidth;
    }

    public void setTextAreaWidth(int textAreaWidth) {
        this.textAreaWidth = textAreaWidth;
    }

    public int getTextAreaMinRows() {
        return textAreaMinRows;
    }

    public void setTextAreaMinRows(int textAreaMinRows) {
        this.textAreaMinRows = textAreaMinRows;
    }
}
