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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.annotations.HighlightLinks;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TextField extends AbstractTextField {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static Pattern linkPattern =
            Pattern.compile("(http://|www\\.)\\S+", Pattern.CASE_INSENSITIVE);
    public final static Pattern emailPattern =
            Pattern.compile("[a-z0-9\\-_]++(\\.[a-z0-9\\-_]++)*@[a-z0-9\\-_]++" +
            "(\\.[a-z0-9\\-_]++)++", Pattern.CASE_INSENSITIVE);

    protected boolean highlightLinks = false;
    protected boolean multiline = false;
    protected int textAreaWidth = 70;
    protected int textAreaMinRows = 4;

    //--------------------------------------------------------------------------
    // Costruttori
    //--------------------------------------------------------------------------
    public TextField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public TextField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);
        if (accessor.isAnnotationPresent(HighlightLinks.class)) {
            setHighlightLinks(true);
        }
        if (accessor.isAnnotationPresent(Multiline.class)) {
            setMultiline(true);
        }
    }

    //--------------------------------------------------------------------------
    // Implementazione di Element
    //--------------------------------------------------------------------------
    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);

        if (mode.isView(immutable)) {
            return;
        }

        String reqValue = req.getParameter(inputName);
        if (reqValue == null) {
            return;
        }

        stringValue = reqValue.trim();
        if (autoCapitalize) {
            stringValue = stringValue.toUpperCase();
        }
    }

    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        try {
            if (obj == null) {
                stringValue = null;
            } else {
                stringValue = (String)accessor.get(obj);
            }
        } catch (IllegalAccessException e) {
            throw new Error(e);
        } catch (InvocationTargetException e) {
            throw new Error(e);
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
            xb.addAttribute("cols", Integer.toString(textAreaWidth));
            xb.addAttribute("rows", Integer.toString(
                    numRowTextArea(stringValue, textAreaWidth)));
            xb.addAttribute("onkeyup", "verifyLine(this);");
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
        xb.addAttribute("class", "value");
        xb.addAttribute("id", id);
        String escapedText = StringEscapeUtils.escapeHtml(stringValue);
        if (href != null) {
            xb.openElement("a");
            xb.addAttribute("href", href);
            xb.addAttribute("alt", alt);
        } else  if (highlightLinks) {
            escapedText = highlightLinks(escapedText);
        }
        xb.writeNoHtmlEscape(adjustText(escapedText));
        if (href != null) {
            xb.closeElement("a");
        }
        xb.closeElement("div");
    }

    //--------------------------------------------------------------------------
    // Other methods
    //--------------------------------------------------------------------------

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
// commentate le due righe successive perché creavano troppe righe che
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
            if (linkMatcher.group(1).equals("http://")) {
                linkMatcher.appendReplacement(sb, "<a href=\"" + linkMatcher.group(0) +
                        "\">" + text + "</a>");
            } else { // vuol dire che inizia con www e nn e' presente http://
                linkMatcher.appendReplacement(sb, "<a href=\"http://" +
                        linkMatcher.group(0) + "\">" + text + "</a>");
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

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------
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
