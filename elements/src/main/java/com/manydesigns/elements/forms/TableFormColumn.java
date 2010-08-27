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

import com.manydesigns.elements.Element;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.Generator;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XhtmlBuffer;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableFormColumn implements Element {
    public static final String copyright =
            "Copyright (c) 2005-2009, ManyDesigns srl";

    protected final Field[] fields;
    protected final PropertyAccessor propertyAccessor;

    protected Mode mode = Mode.EDIT;

    protected String label;
    protected Generator hrefGenerator;
    protected Generator altGenerator;

    //**************************************************************************
    // Costruttori
    //**************************************************************************

    public TableFormColumn(PropertyAccessor propertyAccessor, int nRows) {
        this(propertyAccessor, nRows, null);
    }

    public TableFormColumn(PropertyAccessor propertyAccessor,
                           int nRows, String prefix) {
        fields = new Field[nRows];
        this.propertyAccessor = propertyAccessor;

        if (propertyAccessor.isAnnotationPresent(Label.class)) {
            label = propertyAccessor.getAnnotation(Label.class).value();
        } else {
            label = Util.camelCaseToWords(propertyAccessor.getName());
        }
    }


    //**************************************************************************
    // Implementazione di Element
    //**************************************************************************

    public void readFromRequest(HttpServletRequest req) {
        for (Field current : fields) {
            current.readFromRequest(req);
        }
    }


    public boolean validate() {
        boolean result = true;
        for (Field current : fields) {
            result = current.validate() && result;
        }
        return result;
    }

    public void toXhtml(XhtmlBuffer xhtmlBuffer) {

    }

    public void readFromObject(Object obj) {
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
            for (int i = arrayLength; i < fields.length; i++) {
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

            for (; i < fields.length; i++) {
                readFromObject(i, null);
            }
        }
    }

    protected void readFromObject(int rowIndex, Object obj) {
        Field field = fields[rowIndex];
        if (hrefGenerator != null) {
            field.setHref(hrefGenerator.generate(obj));
            if (altGenerator != null) {
                field.setAlt(altGenerator.generate(obj));
            }
        }
        field.readFromObject(obj);
    }


    public void writeToObject(Object obj) {
        Class clazz = obj.getClass();
        if (clazz.isArray()) { // Tratta obj come un array
            // Scorre tutti gli ellementi dell'array obj,
            // indipendentemente da quante righe ci sono nell table form.
            // Eventualmente lancia Eccezione.
            final int arrayLength = Array.getLength(obj);
            for (int i = 0; i < arrayLength; i++) {
                Object currentObj = Array.get(obj, i);
                fields[i].writeToObject(currentObj);
            }
        }
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        for (Field current : fields) {
            current.setMode(mode);
        }
    }
    //**************************************************************************
    // Getter/setter
    //**************************************************************************

    public PropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void labelToXhtml(XhtmlBuffer xb) {
        xb.write(label);
    }

    public void valueToXhtml(XhtmlBuffer xb, int rowIndex) {
        Field field = fields[rowIndex];
        field.valueToXhtml(xb);
    }

    public List<String> getErrors(int rowIndex) {
        Field field = fields[rowIndex];
        return field.getErrors();
    }

    public Generator getHrefGenerator() {
        return hrefGenerator;
    }

    public void setHrefGenerator(Generator hrefGenerator) {
        this.hrefGenerator = hrefGenerator;
    }

    public Generator getAltGenerator() {
        return altGenerator;
    }

    public void setAltGenerator(Generator altGenerator) {
        this.altGenerator = altGenerator;
    }

    public Field[] getFields() {
        return fields;
    }
}
