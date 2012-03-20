/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.elements.forms;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.composites.AbstractCompositeElement;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.MultipartRequestField;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.jetbrains.annotations.NotNull;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class FieldSet extends AbstractCompositeElement<Field> {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

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

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        if (mode.isHidden()) {
            for (Field current : this) {
                current.toXhtml(xb);
            }
        } else {
            xb.openElement("fieldset");
            if (name == null) {
                xb.addAttribute("class", "mde-form-fieldset mde-no-legend");
            } else {
                xb.addAttribute("class", "mde-form-fieldset");
                xb.writeLegend(name, null);
            }
            xb.openElement("table");
            xb.addAttribute("class", "mde-form-table");

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

    public boolean isMultipartRequest() {
        for (Field current : this) {
            if (current instanceof MultipartRequestField &&
                !current.getMode().isView
                        (current.isInsertable(), current.isUpdatable())) {
                return true;
            }
        }
        return false;
    }
}
