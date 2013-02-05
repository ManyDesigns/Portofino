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

package com.manydesigns.elements.composites;

import com.manydesigns.elements.Element;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.reflection.PropertyAccessor;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class AbstractCompositeElement<T extends Element>
        extends ArrayList<T>
        implements Element {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected String id;

    public AbstractCompositeElement(int initialCapacity) {
        super(initialCapacity);
    }

    public AbstractCompositeElement() {}

    public AbstractCompositeElement(Collection<? extends T> c) {
        super(c);
    }

    public void readFromRequest(HttpServletRequest req) {
        for (T current : this) {
            current.readFromRequest(req);
        }
    }

    public boolean validate() {
        boolean result = true;
        for (T current : this) {
            result = current.validate() && result;
        }
        return result;
    }

    public void readFromObject(Object obj) {
        for (T current : this) {
            current.readFromObject(obj);
        }
    }

    public void writeToObject(Object obj) {
        for (T current : this) {
            current.writeToObject(obj);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Field findFieldByPropertyName(String propertyName) {
        for(T element : this) {
            if(element instanceof Field) {
                Field field = (Field) element;
                PropertyAccessor accessor = field.getPropertyAccessor();
                if (accessor.getName().equals(propertyName)) {
                    return field;
                }
            } else if(element instanceof AbstractCompositeElement) {
                Field field = ((AbstractCompositeElement) element).findFieldByPropertyName(propertyName);
                if(field != null) {
                    return field;
                }
            }
        }
        return null;
    }

    public Collection<Field> fields() {
        List<Field> fields = new ArrayList<Field>();
        for(T element : this) {
            if(element instanceof Field) {
                Field field = (Field) element;
                fields.add(field);
            } else if(element instanceof AbstractCompositeElement) {
                fields.addAll(((AbstractCompositeElement) element).fields());
            }
        }
        return fields;
    }

}
