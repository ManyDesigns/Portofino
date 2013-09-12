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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@SuppressWarnings({"UnusedDeclaration"})
public class PropertiesAccessor implements ClassAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final PropertiesEntryAccessor[] accessors;

    public PropertiesAccessor(Properties properties) {
        accessors = new PropertiesEntryAccessor[properties.size()];
        int i = 0;
        for (Object current : properties.keySet()) {
            String name = (String)current;
            accessors[i] = new PropertiesEntryAccessor(name);
            i++;
        }

        // sort alphabetically
        Arrays.sort(accessors, new Comparator<PropertiesEntryAccessor>() {

            public int compare(PropertiesEntryAccessor o1,
                               PropertiesEntryAccessor o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    public String getName() {
        return null;
    }

    public PropertyAccessor getProperty(String propertyName) throws NoSuchFieldException {
        for (PropertiesEntryAccessor current : accessors) {
            if (current.getName().equals(propertyName)) {
                return current;
            }
        }
        throw new NoSuchFieldException(propertyName);
    }

    public PropertyAccessor[] getProperties() {
        return accessors.clone();
    }


    public PropertyAccessor[] getKeyProperties() {
        return new PropertyAccessor[0];
    }

    public Object newInstance() {
        return null;
    }

    public boolean isAnnotationPresent(
            Class<? extends Annotation> annotationClass) {
        return false;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    public Annotation[] getDeclaredAnnotations() {
        return new Annotation[0];
    }
}
