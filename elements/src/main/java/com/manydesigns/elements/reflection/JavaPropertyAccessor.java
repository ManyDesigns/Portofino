/*
 * Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.reflection;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class JavaPropertyAccessor extends AbstractAnnotatedAccessor implements PropertyAccessor {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";


    //**************************************************************************
    // Fields
    //**************************************************************************

    private final PropertyDescriptor propertyDescriptor;
    private final Method getter;
    private final Method setter;

    public final static Logger logger =
            LoggerFactory.getLogger(JavaPropertyAccessor.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public JavaPropertyAccessor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
        getter = propertyDescriptor.getReadMethod();
        setter = propertyDescriptor.getWriteMethod();
        try {
            Field field = getter.getDeclaringClass().getDeclaredField(propertyDescriptor.getName());
            for(Annotation ann : field.getAnnotations()) {
                annotations.put(ann.annotationType(), ann);
            }
        } catch (NoSuchFieldException e) {
            logger.debug("No field for " + propertyDescriptor.getName(), e);
        }
        for(Annotation ann : getter.getAnnotations()) {
            annotations.put(ann.annotationType(), ann);
        }
        if (setter == null) {
            logger.debug("Setter not available for: {}", propertyDescriptor.getName());
        } else {
            for(Annotation ann : setter.getAnnotations()) {
                annotations.put(ann.annotationType(), ann);
            }
        }
    }


    //**************************************************************************
    // PropertyAccessor implementation
    //**************************************************************************

    public String getName() {
        return propertyDescriptor.getName();
    }

    public Class getType() {
        return getter.getReturnType();
    }

    public int getModifiers() {
        return getter.getModifiers();
    }

    public Object get(Object obj) {
        try {
            return getter.invoke(obj);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(
                    String.format("Cannot get property: %s", getName()), e);
        } catch (InvocationTargetException e) {
            throw new ReflectionException(
                    String.format("Cannot get property: %s", getName()), e);
        }
    }

    public void set(Object obj, Object value) {
        if (setter == null) {
            throw new ReflectionException(String.format(
                    "Setter not available for property: %s", getName()));
        } else {
            try {
                setter.invoke(obj, value);
            } catch (IllegalAccessException e) {
                throw new ReflectionException(
                        String.format("Cannot set property: %s", getName()), e);

            } catch (InvocationTargetException e) {
                throw new ReflectionException(
                        String.format("Cannot set property: %s", getName()), e);
            }
        }
    }

    //**************************************************************************
    // Overrides
    //**************************************************************************

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", getName())
                .toString();
    }
}
