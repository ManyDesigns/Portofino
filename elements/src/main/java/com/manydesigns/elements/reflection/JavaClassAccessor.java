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

package com.manydesigns.elements.reflection;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class JavaClassAccessor implements ClassAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected final Class javaClass;
    protected final PropertyAccessor[] propertyAccessors;

    public JavaClassAccessor(Class javaClass) {
        this.javaClass = javaClass;

        List<PropertyAccessor> accessorList =
                new ArrayList<PropertyAccessor>();

        // handle properties through introspection
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(javaClass);
            PropertyDescriptor[] propertyDescriptors =
                    beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor current : propertyDescriptors) {
                accessorList.add(new JavaPropertyAccessor(current));
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        // handle public fields
        for (Field field : javaClass.getFields()) {
            if (isPropertyPresent(accessorList, field.getName())) {
                continue;
            }
            accessorList.add(new JavaFieldAccessor(field));
        }
        propertyAccessors = new PropertyAccessor[accessorList.size()];
        accessorList.toArray(propertyAccessors);
    }

    private boolean isPropertyPresent(List<PropertyAccessor> accessorList, String name) {
        for (PropertyAccessor current : accessorList) {
            if (current.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public PropertyAccessor getProperty(String propertyName)
            throws NoSuchFieldException {
        for (PropertyAccessor current : propertyAccessors) {
            if (current.getName().equals(propertyName)) {
                return current;
            }
        }
        throw new NoSuchFieldException(propertyName);
    }

    public PropertyAccessor[] getProperties() {
        return propertyAccessors.clone();
    }

    public Class getJavaClass() {
        return javaClass;
    }
}
