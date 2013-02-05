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

package com.manydesigns.elements.reflection;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class JavaFieldAccessor implements PropertyAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    private Field field;


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public JavaFieldAccessor(Field field) {
        assert field != null;
        this.field = field;
    }


    //**************************************************************************
    // PropertyAccessor implementation
    //**************************************************************************

    public String getName() {
        return field.getName();
    }

    public Class getType() {
        return field.getType();
    }

    public int getModifiers() {
        return field.getModifiers();
    }

    public Object get(Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(
                    String.format("Cannot get property: %s", getName()), e);
        }
    }

    public void set(Object obj, Object value) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(
                    String.format("Cannot set property: %s", getName()), e);
        }
    }

    
    //**************************************************************************
    // AnnotatedElement implementation
    //**************************************************************************

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    public Annotation[] getAnnotations() {
        return field.getAnnotations();
    }

    public Annotation[] getDeclaredAnnotations() {
        return field.getDeclaredAnnotations();
    }


    //**************************************************************************
    // Overrides
    //**************************************************************************

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", field.getName())
                .toString();
    }
}
