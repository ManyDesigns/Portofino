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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;

/**
 * Accessor for Java maps.
 *
 * @author Alessio Stalla – alessiostalla@gmail.com
 */
@SuppressWarnings({"UnusedDeclaration"})
public class MapAccessor implements ClassAccessor {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    protected final MapEntryAccessor[] accessors;

    public MapAccessor(Map template) {
        accessors = new MapEntryAccessor[template.size()];
        int i = 0;
        for (Object current : template.keySet()) {
            String name = (String)current;
            accessors[i] = new MapEntryAccessor(name);
            i++;
        }

        // sort alphabetically
        Arrays.sort(accessors, Comparator.comparing(MapEntryAccessor::getName));
    }

    public String getName() {
        return null;
    }

    @Override
    public Class<?> getType() {
        return Properties.class;
    }

    public MapEntryAccessor getProperty(String propertyName) throws NoSuchFieldException {
        for (MapEntryAccessor current : accessors) {
            if (current.getName().equals(propertyName)) {
                return current;
            }
        }
        throw new NoSuchFieldException(propertyName);
    }

    public MapEntryAccessor[] getProperties() {
        return accessors.clone();
    }


    public MapEntryAccessor[] getKeyProperties() {
        return new MapEntryAccessor[0];
    }

    public Object newInstance() {
        return null;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
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
