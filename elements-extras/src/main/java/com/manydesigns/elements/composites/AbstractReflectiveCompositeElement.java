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

package com.manydesigns.elements.composites;

import com.manydesigns.elements.Element;
import com.manydesigns.elements.annotations.Id;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class AbstractReflectiveCompositeElement implements Element {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected String _id;
    protected List<Field> _fields;

    //**************************************************************************
    // Costruttori
    //**************************************************************************
    protected AbstractReflectiveCompositeElement() {
        this(null);
    }

    @SuppressWarnings({"unchecked"})
    protected AbstractReflectiveCompositeElement(String prefix) {
        Class cls = this.getClass();

        // build full id
        String localId;
        if (cls.isAnnotationPresent(Id.class)) {
            localId = ((Id)cls.getAnnotation(Id.class)).value();
        } else {
            localId = cls.getName();
        }
        Object[] idArgs = {prefix, localId};
        _id = StringUtils.join(idArgs);

        // find Element fields by reflection
        _fields = new ArrayList<Field>();
        for (Field field : cls.getFields()) {
            int modifiers = field.getModifiers();
            if (Element.class.isAssignableFrom(field.getType())
                    && !Modifier.isStatic(modifiers)) {
                _fields.add(field);
            }
        }
    }

    //**************************************************************************
    // Implementazioni di Element
    //**************************************************************************

    public void readFromRequest(HttpServletRequest req) {
        for (Element component : elements()) {
            component.readFromRequest(req);
        }
    }

    public void readFromObject(Object obj) {
        for (Element component : elements()) {
            component.readFromObject(obj);
        }
    }

    public void writeToObject(Object obj) {
        for (Element component : elements()) {
            component.writeToObject(obj);
        }
    }

    public boolean validate() {
        boolean result = true;
        for (Element current : elements()) {
            result = current.validate() && result;
        }
        return result;
    }

    //**************************************************************************
    // Altri metodi
    //**************************************************************************

    public List<Element> elements() {
        List<Element> components = new ArrayList<Element>();
        for (Field field : _fields) {
            try {
                components.add((Element)field.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return components;
    }

    public Element findComponentByName(String name) {
        for (Field field : _fields) {
            if (field.getName().equals(name)) {
                try {
                    return (Element)field.get(this);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        throw new ArrayIndexOutOfBoundsException(name);
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
}
