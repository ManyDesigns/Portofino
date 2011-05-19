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

package com.manydesigns.elements.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Constructor;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ReflectionUtil {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";


    //**************************************************************************
    // Static fields and initialization
    //**************************************************************************

    protected final static ClassLoader classLoader;

    public final static Logger logger =
            LoggerFactory.getLogger(ReflectionUtil.class);

    static {
        classLoader = ReflectionUtil.class.getClassLoader();
    }


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
        return classLoader.getResourceAsStream(resourceName);
    }
}
