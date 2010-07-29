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
import com.manydesigns.elements.Util;
import com.manydesigns.elements.annotations.Id;
import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.hyperlinks.HyperlinkGenerator;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableFormColumn extends AbstractCompositeElement<Field> {
    public static final String copyright =
            "Copyright (c) 2005-2009, ManyDesigns srl";

    protected final PropertyAccessor propertyAccessor;
    protected String label;
    protected HyperlinkGenerator hyperlinkGenerator;

    //--------------------------------------------------------------------------
    // Costruttori
    //--------------------------------------------------------------------------

    public TableFormColumn(PropertyAccessor propertyAccessor, int nRows) {
        this(propertyAccessor, nRows, null);
    }

    public TableFormColumn(PropertyAccessor propertyAccessor,
                           int nRows, String prefix) {
        super(nRows);
        this.propertyAccessor = propertyAccessor;

        String localId;
        if (propertyAccessor.isAnnotationPresent(Id.class)) {
            localId = propertyAccessor.getAnnotation(Id.class).value();
        } else {
            localId = propertyAccessor.getName();
        }
        Object[] idArgs = {prefix, localId};
        id = StringUtils.join(idArgs);

        if (propertyAccessor.isAnnotationPresent(Label.class)) {
            label = propertyAccessor.getAnnotation(Label.class).value();
        } else {
            label = Util.camelCaseToWords(propertyAccessor.getName());
        }
    }


    //--------------------------------------------------------------------------
    // Implementazione di Element
    //--------------------------------------------------------------------------

    public void toXhtml(XhtmlBuffer xhtmlBuffer) {

    }

    @Override
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
            for (int i = arrayLength; i < size(); i++) {
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

            for (; i < size(); i++) {
                readFromObject(i, null);
            }
        }
    }

    protected void readFromObject(int rowIndex, Object obj) {
        Field field = get(rowIndex);
        if (hyperlinkGenerator != null) {
            field.setHref(hyperlinkGenerator.generateHref(obj));
            field.setAlt(hyperlinkGenerator.generateAlt(obj));
        }
        field.readFromObject(obj);
    }


    @Override
    public void writeToObject(Object obj) {
        Class clazz = obj.getClass();
        if (clazz.isArray()) { // Tratta obj come un array
            // Scorre tutti gli ellementi dell'array obj,
            // indipendentemente da quante righe ci sono nell table form.
            // Eventualmente lancia Eccezione.
            final int arrayLength = Array.getLength(obj);
            for (int i = 0; i < arrayLength; i++) {
                Object currentObj = Array.get(obj, i);
                get(i).writeToObject(currentObj);
            }
        }
    }

    //--------------------------------------------------------------------------
    // Getter/setter
    //--------------------------------------------------------------------------

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
        Field field = this.get(rowIndex);
        field.valueToXhtml(xb);
    }

    public List<String> getErrors(int rowIndex) {
        Field field = this.get(rowIndex);
        return field.getErrors();
    }

    public HyperlinkGenerator getHyperlinkGenerator() {
        return hyperlinkGenerator;
    }

    public void setHyperlinkGenerator(HyperlinkGenerator hyperlinkGenerator) {
        this.hyperlinkGenerator = hyperlinkGenerator;
    }
}
