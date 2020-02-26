/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.impl.LabelImpl;

import java.lang.annotation.Annotation;
import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PropertiesEntryAccessor implements PropertyAccessor {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    protected final String name;
    protected final Label labelAnnotation;

    public PropertiesEntryAccessor(String name) {
        this.name = name;
        labelAnnotation = new LabelImpl(name);
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return String.class;
    }

    public int getModifiers() {
        return 0;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (annotationClass == Label.class) {
            //noinspection unchecked
            return (T) labelAnnotation;
        }
        return null;
    }

    public Annotation[] getAnnotations() {
        Annotation[] result = new Annotation[1];
        result[0] = labelAnnotation;
        return result;
    }

    public Annotation[] getDeclaredAnnotations() {
        return getAnnotations();
    }

    public String get(Object obj) {
        return ((Properties)obj).getProperty(name);
    }

    public void set(Object obj, Object value) {
        ((Properties)obj).setProperty(name, (String)value);
    }
}
