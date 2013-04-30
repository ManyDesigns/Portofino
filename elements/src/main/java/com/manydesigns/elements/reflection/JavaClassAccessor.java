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

import com.manydesigns.elements.annotations.Key;
import com.manydesigns.elements.util.ReflectionUtil;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class JavaClassAccessor implements ClassAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Class javaClass;
    protected final PropertyAccessor[] propertyAccessors;
    protected final PropertyAccessor[] keyPropertyAccessors;

    //**************************************************************************
    // Static fields and methods
    //**************************************************************************

    protected static final Map<Class, JavaClassAccessor> classAccessorCache;
    public static final Logger logger =
                LoggerFactory.getLogger(JavaClassAccessor.class);

    static {
        classAccessorCache = new HashMap<Class, JavaClassAccessor>();
    }

    public static JavaClassAccessor getClassAccessor(Class javaClass) {
        JavaClassAccessor cachedResult = classAccessorCache.get(javaClass);
        if (cachedResult == null) {
            logger.debug("Cache miss for: {}", javaClass);
            cachedResult = new JavaClassAccessor(javaClass);
            logger.debug("Caching key: {} - Value: {}",
                    javaClass, cachedResult);
            classAccessorCache.put(javaClass, cachedResult);
        } else {
            logger.debug("Cache hit for: {} - Value: {}",
                    javaClass, cachedResult);
        }
        return cachedResult;
    }


    //**************************************************************************
    // Constructors and initialization
    //**************************************************************************

    protected JavaClassAccessor(Class javaClass) {
        this.javaClass = javaClass;

        List<PropertyAccessor> accessorList = setupPropertyAccessors();
        propertyAccessors = new PropertyAccessor[accessorList.size()];
        accessorList.toArray(propertyAccessors);

        List<PropertyAccessor> keyAccessors = setupKeyPropertyAccessors();
        keyPropertyAccessors = new PropertyAccessor[keyAccessors.size()];
        keyAccessors.toArray(keyPropertyAccessors);
    }

    protected List<PropertyAccessor> setupPropertyAccessors() {
        List<PropertyAccessor> accessorList = new ArrayList<PropertyAccessor>();

        // handle properties through introspection
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(javaClass);
            PropertyDescriptor[] propertyDescriptors =
                    beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor current : propertyDescriptors) {
                accessorList.add(new JavaPropertyAccessor(current));
            }
        } catch (IntrospectionException e) {
            logger.error(e.getMessage(), e);
        }

        // handle public fields
        for (Field field : javaClass.getFields()) {
            if (isPropertyPresent(accessorList, field.getName())) {
                continue;
            }
            accessorList.add(new JavaFieldAccessor(field));
        }

        return accessorList;
    }

    protected List<PropertyAccessor> setupKeyPropertyAccessors() {
        Map<String, List<PropertyAccessor>> keys = new HashMap<String, List<PropertyAccessor>>();
        for(PropertyAccessor propertyAccessor : propertyAccessors) {
            Key key = propertyAccessor.getAnnotation(Key.class);
            if(key != null) {
                List<PropertyAccessor> keyProperties = keys.get(key.name());
                if(keyProperties == null) {
                    keyProperties = new ArrayList<PropertyAccessor>();
                    keys.put(key.name(), keyProperties);
                }
                keyProperties.add(propertyAccessor);
            }
        }

        if(keys.isEmpty()) {
            logger.debug("No primary key configured for {}", javaClass);
            return Collections.emptyList();
        }

        String primaryKeyName;
        List<PropertyAccessor> keyAccessors;
        Key key = getAnnotation(Key.class);
        if(key != null) {
            primaryKeyName = key.name();
        } else {
            primaryKeyName = Key.DEFAULT_NAME;
        }
        keyAccessors = keys.get(primaryKeyName);
        if(keyAccessors == null) {
            keyAccessors = keys.get(Key.DEFAULT_NAME);
        }
        if(keyAccessors == null) {
            logger.debug("Primary key \"" + primaryKeyName + "\" not found in " + javaClass + "; using the first available key.");
            keyAccessors = keys.values().iterator().next();
        }

        Collections.sort(keyAccessors, new Comparator<PropertyAccessor>() {
            public int compare(PropertyAccessor o1, PropertyAccessor o2) {
                Integer ord1 = o1.getAnnotation(Key.class).order();
                Integer ord2 = o2.getAnnotation(Key.class).order();
                return ord1.compareTo(ord2);
            }
        });

        return keyAccessors;
    }

    private boolean isPropertyPresent(List<PropertyAccessor> accessorList,
                                      String name) {
        for (PropertyAccessor current : accessorList) {
            if (current.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    
    //**************************************************************************
    // ClassAccessor implementation
    //**************************************************************************

    public String getName() {
        return javaClass.getName();
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

    public PropertyAccessor[] getKeyProperties() {
        return keyPropertyAccessors.clone();
    }

    public Object newInstance() {
        return ReflectionUtil.newInstance(javaClass);
    }


    //**************************************************************************
    // AnnotatedElement implementation
    //**************************************************************************

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return javaClass.isAnnotationPresent(annotationClass);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        //noinspection unchecked
        return (T) javaClass.getAnnotation(annotationClass);
    }

    public Annotation[] getAnnotations() {
        return javaClass.getAnnotations();
    }

    public Annotation[] getDeclaredAnnotations() {
        return javaClass.getDeclaredAnnotations();
    }

    //**************************************************************************
    // Getters
    //**************************************************************************

    public Class getJavaClass() {
        return javaClass;
    }


    //**************************************************************************
    // Other methods
    //**************************************************************************

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("javaClass", javaClass)
                .toString();
    }
}
