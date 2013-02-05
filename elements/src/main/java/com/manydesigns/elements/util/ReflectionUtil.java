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

package com.manydesigns.elements.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Constructor;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ReflectionUtil {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";


    //**************************************************************************
    // Static fields and initialization
    //**************************************************************************

    //protected final static ClassLoader classLoader;

    public final static Logger logger =
            LoggerFactory.getLogger(ReflectionUtil.class);

    /*static {
        classLoader = ReflectionUtil.class.getClassLoader();
    }*/


    //**************************************************************************
    // Utility methods
    //**************************************************************************

    public static Class loadClass(String className) {
        try {
            // loadClass() non sa gestire nomi di classi tipo "[B" (byte array)
            // Class.forName() ce la fa.
//            Class<?> aClass = classLoader.loadClass(className);
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
}
