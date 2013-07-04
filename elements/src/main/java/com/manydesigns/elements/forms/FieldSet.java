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

package com.manydesigns.elements.forms;

import com.manydesigns.elements.FormElement;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.composites.AbstractCompositeElement;
import com.manydesigns.elements.fields.MultipartRequestField;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.jetbrains.annotations.NotNull;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class FieldSet extends AbstractCompositeElement<FormElement> {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final int nColumns;
    protected final Mode mode;

    protected String name;

    protected int currentColumn;
    protected boolean rowOpened;

    public FieldSet(String name, int nColumns, Mode mode) {
        this.name = name;
        this.nColumns = nColumns;
        this.mode = mode;
        if(nColumns > 4 && nColumns != 6) {
            throw new IllegalArgumentException("nColumns = " + nColumns + " but only 1, 2, 3, 4, 6 columns are supported");
        }
    }

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        if (mode.isHidden()) {
            for (FormElement current : this) {
                current.toXhtml(xb);
            }
        } else {
            xb.openElement("fieldset");
            if (name != null) {
                xb.writeLegend(name, null);
            }

            currentColumn = 0;
            rowOpened = false;

            for (FormElement current : this) {
                int colSpan = Math.min(current.getColSpan(), nColumns);
                if (current.isForceNewRow()
                        || currentColumn + colSpan > nColumns) {
                    closeCurrentRow(xb);
                }

                if (currentColumn == 0) {
                    xb.openElement("div");
                    xb.addAttribute("class", "row-fluid");
                    rowOpened = true;
                }

                xb.openElement("div");
                xb.addAttribute("class", "span" + (colSpan * (12 / nColumns)));
                current.toXhtml(xb);
                xb.closeElement("div");

                currentColumn = currentColumn + colSpan;

                if (currentColumn >= nColumns) {
                    closeCurrentRow(xb);
                }
            }

            closeCurrentRow(xb);
            xb.closeElement("fieldset");
        }
    }

    protected void closeCurrentRow(XhtmlBuffer xb) {
        if (!rowOpened) {
            return;
        }
        if (currentColumn < nColumns) {
            xb.openElement("div");
            xb.addAttribute("class", "span" + ((nColumns - currentColumn) * (12 / nColumns)));
            xb.closeElement("div");
        }
        xb.closeElement("div");
        currentColumn = 0;
        rowOpened = false;
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
        for (FormElement current : this) {
            if (current.hasRequiredFields()) {
                return true;
            }
        }
        return false;
    }

    public boolean isMultipartRequest() {
        for (FormElement current : this) {
            if (current instanceof MultipartRequestField) {
                MultipartRequestField field = (MultipartRequestField) current;
                if(!field.getMode().isView
                        (field.isInsertable(), field.isUpdatable())) {
                    return true;
                }
            }
        }
        return false;
    }
}
