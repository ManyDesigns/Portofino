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

package com.manydesigns.elements.fields.search;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.annotations.DateFormat;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Date;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DateSearchField extends RangeSearchField {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String datePattern;
    protected DateTimeFormatter dateTimeFormatter;
    protected boolean containsTime;
    protected String jsDatePattern;
    protected int maxLength;


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public DateSearchField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public DateSearchField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);

        DateFormat dateFormatAnnotation =
                accessor.getAnnotation(DateFormat.class);
        if (dateFormatAnnotation != null) {
            datePattern = dateFormatAnnotation.value();
        } else {
            Configuration elementsConfiguration =
                    ElementsProperties.getConfiguration();
            datePattern = elementsConfiguration.getString(
                    ElementsProperties.FIELDS_DATE_FORMAT);
        }
        dateTimeFormatter = DateTimeFormat.forPattern(datePattern);
        maxLength = dateTimeFormatter.getParser().estimateParsedLength();

        containsTime = datePattern.contains("HH")
                || datePattern.contains("mm")
                || datePattern.contains("ss");

        String tmpPattern = datePattern;
        if (tmpPattern.contains("yyyy")) {
            tmpPattern = tmpPattern.replaceAll("yyyy", "yy");
        }
        if (tmpPattern.contains("MM")) {
            tmpPattern = tmpPattern.replaceAll("MM", "mm");
        }
        if (tmpPattern.contains("dd")) {
            tmpPattern = tmpPattern.replaceAll("dd", "dd");
        }
        jsDatePattern = tmpPattern;
    }

    //**************************************************************************
    // Element overrides
    //**************************************************************************

    @Override
    public void rangeEndToXhtml(XhtmlBuffer xb, String id,
                                String inputName, String stringValue, String label) {
        xb.openElement("div");
        xb.addAttribute("class", "input-prepend");
        xb.openElement("label");
        xb.addAttribute("class", "add-on");
        xb.addAttribute("for", id);
        xb.write(label);
        xb.closeElement("label");
        xb.writeInputText(id, inputName, stringValue, "text", null, null);
        if (!containsTime) {
            String js = MessageFormat.format(
                    "setupDatePicker(''#{0}'', ''{1}'');",
                    StringEscapeUtils.escapeJavaScript(id),
                    StringEscapeUtils.escapeJavaScript(jsDatePattern));
            xb.writeJavaScript(js);
        }        
        xb.closeElement("div");
        xb.write(" ");
    }

    @Override
    public void readFromRequest(HttpServletRequest req) {
        minStringValue = StringUtils.trimToNull(req.getParameter(minInputName));
        try {
            DateTime dateTime = dateTimeFormatter.parseDateTime(minStringValue);
            minValue = new Date(dateTime.getMillis());
        } catch (Throwable e) {
            minValue = null;
        }

        maxStringValue = StringUtils.trimToNull(req.getParameter(maxInputName));
        try {
            DateTime dateTime = dateTimeFormatter.parseDateTime(maxStringValue);
            maxValue = new Date(dateTime.getMillis());
        } catch (Throwable e) {
            maxValue = null;
        }

        searchNullValue = (NULL_VALUE.equals(minStringValue)
                || NULL_VALUE.equals(maxStringValue));
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************


    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public boolean isContainsTime() {
        return containsTime;
    }

    public void setContainsTime(boolean containsTime) {
        this.containsTime = containsTime;
    }

    public String getJsDatePattern() {
        return jsDatePattern;
    }

    public void setJsDatePattern(String jsDatePattern) {
        this.jsDatePattern = jsDatePattern;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}
