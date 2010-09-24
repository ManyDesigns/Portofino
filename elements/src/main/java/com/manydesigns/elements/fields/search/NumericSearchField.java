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

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class NumericSearchField extends AbstractSearchField {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected String minValue;

    //**************************************************************************
    // Costruttori
    //**************************************************************************

    public NumericSearchField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public NumericSearchField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);
    }


    //**************************************************************************
    // Element implementation
    //**************************************************************************

    public void toXhtml(XhtmlBuffer xb) {
        xb.writeLabel(StringUtils.capitalize(label),
                id, "attr_name");
        xb.writeInputText(id, inputName, minValue, "text", "18");
    }


    public void readFromRequest(HttpServletRequest req) {
        minValue = StringUtils.trimToNull(req.getParameter(inputName));
    }

    public boolean validate() {
        return true;
    }

    public Mode getMode() {
        return Mode.VIEW;
    }

    public void setMode(Mode mode) {}


    public void toSearchString(StringBuilder sb) {
        if (minValue != null) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(inputName);
            sb.append("=");
            sb.append(minValue);
        }
    }

    public void configureCriteria(Criteria criteria) {
        if (minValue != null) {
            criteria.eq(accessor, minValue);
        }
    }
}
