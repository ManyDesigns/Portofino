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

package com.manydesigns.elements.composites;

import com.manydesigns.elements.Element;
import com.manydesigns.elements.Mode;
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
*/
public abstract class AbstractReflectiveCompositeElement implements Element {
    public static final String copyright =
            "Copyright (c) 2005-2009, ManyDesigns srl";

    protected String _id;
    protected Mode _mode = Mode.EDIT;
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

    public Mode getMode() {
        return _mode;
    }

    public void setMode(Mode mode) {
        _mode = mode;
        for (Element current : elements()) {
            current.setMode(mode);
        }
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
