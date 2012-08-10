/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final String datePattern;
    protected final DateTimeFormatter dateTimeFormatter;
    protected final boolean containsTime;
    protected final String jsDatePattern;
    protected final int maxLength;


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
        xb.openElement("tr");
        xb.openElement("th");
        xb.openElement("label");
        xb.addAttribute("for", id);
        xb.write(label);
        xb.closeElement("label");
        xb.closeElement("th");
        xb.openElement("td");
        xb.writeInputText(id, inputName, stringValue, "text", null, null);
        if (!containsTime) {
            String js = MessageFormat.format(
                    "setupDatePicker(''#{0}'', ''{1}'');",
                    StringEscapeUtils.escapeJavaScript(id),
                    StringEscapeUtils.escapeJavaScript(jsDatePattern));
            xb.writeJavaScript(js);
        }        
        xb.closeElement("td");
        xb.closeElement("tr");
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

}
