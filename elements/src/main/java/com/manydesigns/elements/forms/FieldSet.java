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

package com.manydesigns.elements.forms;

import com.manydesigns.elements.composites.AbstractCompositeElement;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.MultipartFormDataField;
import com.manydesigns.elements.xml.XhtmlBuffer;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class FieldSet extends AbstractCompositeElement<Field> {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected final int nColumns;
    protected final Mode mode;

    protected String name;

    protected int currentColumn;
    protected boolean trOpened;

    public FieldSet(String name, int nColumns, Mode mode) {
        this.name = name;
        this.nColumns = nColumns;
        this.mode = mode;
    }

    public void toXhtml(XhtmlBuffer xb) {
        if (mode.isHidden()) {
            for (Field current : this) {
                current.toXhtml(xb);
            }
        } else {
            xb.openElement("fieldset");
            if (name == null) {
                xb.addAttribute("class", "nolegend");
            } else {
                xb.writeLegend(name, null);
            }
            xb.openElement("table");
            xb.addAttribute("class", "details");

            currentColumn = 0;
            trOpened = false;

            for (Field current : this) {
                if (current.isForceNewRow()
                        || currentColumn + current.getColSpan() > nColumns) {
                    closeCurrentRow(xb);
                }

                if (currentColumn == 0) {
                    xb.openElement("tr");
                    trOpened = true;
                }
                current.toXhtml(xb);

                currentColumn = currentColumn + current.getColSpan();

                if (currentColumn >= nColumns) {
                    closeCurrentRow(xb);
                }
            }
            closeCurrentRow(xb);

            xb.closeElement("table");
            xb.closeElement("fieldset");
        }
    }

    protected void closeCurrentRow(XhtmlBuffer xb) {
        if (!trOpened) {
            return;
        }

        if (currentColumn < nColumns) {
            xb.openElement("td");
            xb.addAttribute("colspan",
                    Integer.toString((nColumns - currentColumn) * 2) );
            xb.closeElement("td");
        }
        xb.closeElement("tr");
        currentColumn = 0;
        trOpened = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isRequiredFieldsPresent() {
        for (Field current : this) {
            if (current.isRequiredField()) {
                return true;
            }
        }
        return false;
    }

    public boolean isMultipartFormDataFieldsPresent() {
        for (Field current : this) {
            if (current instanceof MultipartFormDataField) {
                return true;
            }
        }
        return false;
    }
}
