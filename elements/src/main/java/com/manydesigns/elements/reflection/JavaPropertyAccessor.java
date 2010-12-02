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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class JavaPropertyAccessor implements PropertyAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";


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
        if (setter == null) {
            logger.debug("Setter not available for: {}",
                    propertyDescriptor.getName());
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
    // AnnotatedElement implementation
    //**************************************************************************

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getter.isAnnotationPresent(annotationClass);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return getter.getAnnotation(annotationClass);
    }

    public Annotation[] getAnnotations() {
        return getter.getAnnotations();
    }

    public Annotation[] getDeclaredAnnotations() {
        return getter.getDeclaredAnnotations();
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
