/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.util;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ReflectionUtil {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";


    //**************************************************************************
    // Static fields and initialization
    //**************************************************************************

    public final static Logger logger =
            LoggerFactory.getLogger(ReflectionUtil.class);

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    public static Class loadClass(String className) {
        try {
            Class<?> aClass = Class.forName(className);
            logger.debug("Loaded class: {}", aClass);
            return aClass;
        } catch (Throwable e) {
            logger.debug("Could not load class: {}", className);
            return null;
        }
    }

    public static Constructor getConstructor(String className,
                                             Class... argClasses) {
        return getConstructor(loadClass(className), argClasses);
    }

    public static Constructor getConstructor(Class aClass,
                                             Class... argClasses) {
        try {
            Constructor constructor = aClass.getConstructor(argClasses);
            logger.debug("Found constructor: {}", constructor);
            return constructor;
        } catch (Throwable e) {
            logger.debug("Could not find construtor for class: {}", aClass);
            return null;
        }
    }

    public static Constructor getBestMatchConstructor(Class aClass,
                                                      Class... argClasses) {
        for (Constructor current : aClass.getConstructors()) {
            Class[] parameterTypes = current.getParameterTypes();
            if (parameterTypes.length != argClasses.length) {
                continue;
            }
            boolean matches = true;
            for (int i = 0; i < argClasses.length; i++) {
                Class paramaterType = parameterTypes[i];
                Class argClass = argClasses[i];
                matches = matches && paramaterType.isAssignableFrom(argClass);
            }
            if (matches) {
                return current;
            }
        }
        logger.debug("Could not find best match construtor for class: {}", aClass);
        return null;
    }

    public static Object newInstance(String className) {
        return newInstance(loadClass(className));
    }

    public static Object newInstance(Class aClass) {
        Constructor constructor = getConstructor(aClass);
        return newInstance(constructor);
    }

    public static Object newInstance(Constructor constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (Throwable e) {
            logger.debug("Could not instanciate class constructor: {}",
                    constructor);
            return null;
        }
    }

    public static InputStream getResourceAsStream(String resourceName) {
        return ReflectionUtil.class.getClassLoader().getResourceAsStream(resourceName);
    }

    public static JSONWriter propertyAccessorToJson(JSONStringer js, PropertyAccessor accessor) {
        js.object();
        js.key("name").value(accessor.getName());
        js.key("type").value(accessor.getType().getName());
        js.key("modifiers").array();
        int modifiers = accessor.getModifiers();
        if(Modifier.isAbstract(modifiers)) {
            js.value("abstract");
        }
        if(Modifier.isFinal(modifiers)) {
            js.value("final");
        }
        if(Modifier.isPrivate(modifiers)) {
            js.value("private");
        }
        if(Modifier.isProtected(modifiers)) {
            js.value("protected");
        }
        if(Modifier.isPublic(modifiers)) {
            js.value("public");
        }
        js.endArray();
        js.key("annotations").array();
        for(Annotation ann : accessor.getAnnotations()) {
            annotationToJson(js, ann);
        }
        js.endArray();
        return js.endObject();
    }

    public static void annotationToJson(JSONStringer js, Annotation ann) {
        js.object();
        Class<? extends Annotation> annotationType = ann.annotationType();
        js.key("type").value(annotationType.getName());
        js.key("properties").object();
        for (Method method : annotationType.getDeclaredMethods()) {
            try {
                Object propertyValue = method.invoke(ann);
                js.key(method.getName());
                annotationPropertyValueAsJson(js, propertyValue);
            } catch (Exception e) {
                logger.warn("Exception reading annotation property, skipping", e);
            }
        }
        js.endObject();
        js.endObject();
    }

    public static void annotationPropertyValueAsJson(JSONStringer js, Object propertyValue) {
        if(propertyValue instanceof Annotation) {
            annotationToJson(js, (Annotation) propertyValue);
        } else if(propertyValue instanceof Class) {
            js.value(propertyValue.toString());
        } else if(propertyValue != null && propertyValue.getClass().isArray()) {
            js.array();
            //TODO handle primitive arrays. Elements annotations only have String[] arrays currently.
            for (Object element : (Object[]) propertyValue) {
                annotationPropertyValueAsJson(js, element);
            }
            js.endArray();
        } else {
            js.value(propertyValue);
        }
    }

    public static JSONWriter classAccessorToJson(JSONStringer js, ClassAccessor accessor) {
        js.object();
        js.key("name").value(accessor.getName());
        js.key("keyProperties").array();
        for(PropertyAccessor p : accessor.getKeyProperties()) {
            js.value(p.getName());
        }
        js.endArray();
        js.key("properties").array();
        for(PropertyAccessor p : accessor.getProperties()) {
            propertyAccessorToJson(js, p);
        }
        js.endArray();
        js.key("annotations").array();
        for(Annotation ann : accessor.getAnnotations()) {
            annotationToJson(js, ann);
        }
        js.endArray();
        return js.endObject();
    }
}
