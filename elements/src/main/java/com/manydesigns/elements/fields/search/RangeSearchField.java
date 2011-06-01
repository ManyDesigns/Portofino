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

package com.manydesigns.elements.fields.search;

import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class RangeSearchField extends AbstractSearchField {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static String MIN_SUFFIX = "_min";
    public final static String MAX_SUFFIX = "_max";

    public final static String NULL_VALUE = "-";

    protected String minId;
    protected String minInputName;
    protected String minStringValue;
    protected Object minValue;

    protected String maxId;
    protected String maxInputName;
    protected String maxStringValue;
    protected Object maxValue;

    protected boolean searchNullValue;

    //**************************************************************************
    // Costruttori
    //**************************************************************************

    public RangeSearchField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public RangeSearchField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);

        minId = id + MIN_SUFFIX;
        minInputName = inputName + MIN_SUFFIX;

        maxId = id + MAX_SUFFIX;
        maxInputName = inputName + MAX_SUFFIX;
    }


    //**************************************************************************
    // Element implementation
    //**************************************************************************

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.openElement("fieldset");
        xb.writeLegend(StringUtils.capitalize(label), ATTR_NAME_HTML_CLASS);

        xb.openElement("table");
        xb.addAttribute("class", "range");

        rangeEndToXhtml(xb, minId, minInputName, minStringValue,
                getText("elements.search.range.from"));
        rangeEndToXhtml(xb, maxId, maxInputName, maxStringValue,
                getText("elements.search.range.to"));

        xb.closeElement("table");

        xb.closeElement("fieldset");
    }

    public void rangeEndToXhtml(XhtmlBuffer xb, String id,
                                String inputName, String stringValue,
                                String label) {
        xb.openElement("tr");
        xb.openElement("th");
        xb.openElement("label");
        xb.addAttribute("for", id);
        xb.write(label);
        xb.closeElement("label");
        xb.closeElement("th");
        xb.openElement("td");
        xb.writeInputText(id, inputName, stringValue, "text", null, null);
        xb.closeElement("td");
        xb.closeElement("tr");
    }


    public void readFromRequest(HttpServletRequest req) {
        Class type = accessor.getType();

        minStringValue = StringUtils.trimToNull(req.getParameter(minInputName));
        try {
            minValue = OgnlUtils.convertValue(minStringValue, type);
        } catch (Throwable e) {
            minValue = null;
        }

        maxStringValue = StringUtils.trimToNull(req.getParameter(maxInputName));
        try {
            maxValue = OgnlUtils.convertValue(maxStringValue, type);
        } catch (Throwable e) {
            maxValue = null;
        }

        searchNullValue = (NULL_VALUE.equals(minStringValue)
                || NULL_VALUE.equals(maxStringValue));
    }

    public boolean validate() {
        return true;
    }

    public void toSearchString(StringBuilder sb) {
        if (minStringValue != null) {
            appendToSearchString(sb, minInputName, minStringValue);
        }
        if (maxStringValue != null) {
            appendToSearchString(sb, maxInputName, maxStringValue);
        }
    }

    public void configureCriteria(Criteria criteria) {
        if (searchNullValue) {
            criteria.isNull(accessor);
        } else if (minValue != null && maxValue != null) {
            criteria.between(accessor, minValue, maxValue);
        } else if (minValue != null) {
            criteria.ge(accessor, minValue);
        } else if (maxValue != null) {
            criteria.le(accessor, maxValue);
        }
    }
}
