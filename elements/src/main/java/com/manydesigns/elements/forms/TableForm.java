/*
 * Copyright (C) 2005-2009 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.AbstractCompositeElement;
import com.manydesigns.elements.text.Generator;
import com.manydesigns.elements.xml.XhtmlBuffer;

import java.lang.reflect.Array;
import java.util.Collection;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableForm extends AbstractCompositeElement<TableFormColumn> {
    public static final String copyright =
            "Copyright (c) 2005-2009, ManyDesigns srl";

    protected String selectInputName = "select";

    protected final int nRows;
    protected String caption;
    protected boolean selectable = false;
    protected final String[] rowKeys;
    protected final boolean[] selected;
    protected Generator keyGenerator;

    //**************************************************************************
    // Costruttori
    //**************************************************************************

    public TableForm(int nRows) {
        this.nRows = nRows;
        rowKeys = new String[nRows];
        selected = new boolean[nRows];
    }

    //**************************************************************************
    // Implementazione di Element
    //**************************************************************************

    public void toXhtml(XhtmlBuffer xb) {
        xb.openElement("table");
        if (caption != null) {
            xb.writeCaption(caption);
        }
        xb.openElement("thead");
        xb.openElement("tr");

        if (selectable) {
            xb.openElement("th");
            xb.writeNbsp();
            xb.closeElement("th");
        }

        for (TableFormColumn current : this) {
            xb.openElement("th");
            current.labelToXhtml(xb);
            xb.closeElement("th");
        }
        xb.closeElement("tr");
        xb.closeElement("thead");

        xb.openElement("tbody");
        for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
            xb.openElement("tr");
            if (selectable) {
                xb.openElement("td");
                xb.writeInputCheckbox(null, "selection",
                        rowKeys[rowIndex], false);
                xb.closeElement("td");
            }

            for (TableFormColumn currentColumn : this) {
                xb.openElement("td");
                if (!currentColumn.getErrors(rowIndex).isEmpty()) {
                    xb.addAttribute("class", "tableform-error");
                }
                currentColumn.valueToXhtml(xb, rowIndex);
                xb.closeElement("td");
            }
            xb.closeElement("tr");
        }
        xb.closeElement("tbody");

        xb.closeElement("table");
    }

    @Override
    public void readFromObject(Object obj) {
        super.readFromObject(obj);

        Class clazz = obj.getClass();
        if (clazz.isArray()) { // Tratta obj come un array
            // Scorre tutti gli ellementi dell'array obj,
            // indipendentemente da quante righe ci sono nell table form.
            // Eventualmente lancia Eccezione.
            final int arrayLength = Array.getLength(obj);
            for (int i = 0; i < arrayLength; i++) {
                Object currentObj = Array.get(obj, i);
                readFromObject(i, currentObj);
            }

            // Scorre le rimanenti righe del table form,
            // passano null come ottetto di bind.
            for (int i = arrayLength; i < nRows; i++) {
                readFromObject(i, null);
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            // Tratta obj come collection
            Collection collection = (Collection)obj;

            int i = 0;
            for (Object currentObj : collection) {
                readFromObject(i, currentObj);
                i++;
            }

            for (; i < nRows; i++) {
                readFromObject(i, null);
            }
        }

    }

    protected void readFromObject(int index, Object obj) {
        String key = Integer.toString(index);
        if (keyGenerator != null) {
            key = keyGenerator.generate(obj);
        }
        rowKeys[index] = key;
    }

    //**************************************************************************
    // Getter/setter
    //**************************************************************************

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public int getNRows() {
        return nRows;
    }

    public Generator getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(Generator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }
}
