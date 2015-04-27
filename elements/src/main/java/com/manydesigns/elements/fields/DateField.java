/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.DateFormat;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringEscapeUtils;
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
public class DateField extends AbstractTextField {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final String datePattern;
    protected DateTimeFormatter dateTimeFormatter;
    protected final boolean containsTime;

    protected Date dateValue;
    protected boolean dateFormatError;


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public DateField(PropertyAccessor accessor, Mode mode) {
        this(accessor, mode, null);
    }

    public DateField(PropertyAccessor accessor, Mode mode, String prefix) {
        super(accessor, mode, prefix);

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
        setSize(dateTimeFormatter.getParser().estimateParsedLength());

        containsTime = datePattern.contains("HH")
                || datePattern.contains("mm")
                || datePattern.contains("ss");
    }


    //**************************************************************************
    // Element implementation
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
        dateFormatError = false;
        dateValue = null;

        if (stringValue.length() == 0) {
            return;
        }

        try {
            dateValue = Util.parseDateTime(dateTimeFormatter, stringValue, containsTime);
        } catch (Throwable e) {
            dateFormatError = true;
            logger.debug("Cannot parse date: {}", stringValue);
        }
    }

    @Override
    public boolean validate() {
        if (mode.isView(insertable, updatable)
                || (mode.isBulk() && !bulkChecked)) {
            return true;
        }

        if (!super.validate()) {
            return false;
        }

        if (dateFormatError) {
            errors.add(getText("elements.error.field.date.format"));
            return false;
        }

        return true;
    }

    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        if (obj == null) {
            dateValue = null;
        } else {
            Object value = accessor.get(obj);
            if (value == null) {
                dateValue = null;
            } else {
                dateValue = (Date)value;
            }
        }
        if (dateValue == null) {
            stringValue = null;
        } else {
            DateTime dateTime = new DateTime(dateValue);
            stringValue = dateTimeFormatter.print(dateTime);
        }
    }

    public void writeToObject(Object obj) {
        writeToObject(obj, dateValue);
    }

    //**************************************************************************
    // AbstractTextField overrides
    //**************************************************************************

    @Override
    public void valueToXhtmlEdit(XhtmlBuffer xb) {
        xb.writeInputText(id, inputName, stringValue, labelPlaceholder ? label : null,
                          EDITABLE_FIELD_CSS_CLASS, size, maxLength);

        xb.openElement("span");
        xb.addAttribute("class", "help-block");
        xb.write("(");
        xb.write(datePattern);
        xb.write(") ");
        xb.closeElement("span");

        String js = MessageFormat.format(
                "setupDatePicker(''#{0}'', ''{1}'');",
                StringEscapeUtils.escapeJavaScript(id),
                StringEscapeUtils.escapeJavaScript(datePattern));
        xb.writeJavaScript(js);

        if(mode.isBulk()) {
            xb.writeJavaScript(
                    "$(function() { " +
                        "configureBulkEditDateField('" + id + "', '" + bulkCheckboxName + "'); " +
                    "});");
        }
    }

    //**************************************************************************
    // Getters/getters
    //**************************************************************************

    public Date getValue() {
        return dateValue;
    }

    public void setValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }
}
