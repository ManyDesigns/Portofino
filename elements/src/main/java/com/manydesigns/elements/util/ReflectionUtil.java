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

import com.manydesigns.elements.logging.LogUtil;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.logging.Logger;

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

    public final static Logger logger = LogUtil.getLogger(ReflectionUtil.class);

    static {
        classLoader = ReflectionUtil.class.getClassLoader();
    }


    //**************************************************************************
    // Utility methods
    //**************************************************************************

    public static Class loadClass(String className) {
        try {
            Class<?> aClass = classLoader.loadClass(className);
            LogUtil.finerMF(logger,
                    "Loaded class: {0}", aClass);
            return aClass;
        } catch (Throwable e) {
            LogUtil.warningMF(logger,
                    "Could not load class: {0}", e, className);
            return null;
        }
    }

    public static Constructor getConstructor(String className) {
        return getConstructor(loadClass(className));
    }

    public static Constructor getConstructor(Class aClass) {
        try {
            Constructor constructor = aClass.getConstructor();
            LogUtil.finerMF(logger,
                    "Found constructor: {0}", constructor);
            return constructor;
        } catch (Throwable e) {
            LogUtil.warningMF(logger,
                    "Could not find empty construtor for class: {0}",
                    e, aClass);
            return null;
        }
    }

    public static Object newInstance(String className) {
        return newInstance(loadClass(className));
    }

    public static Object newInstance(Class aClass) {
        Constructor constructor = getConstructor(aClass);
        try {
            return constructor.newInstance();
        } catch (Throwable e) {
            LogUtil.warningMF(logger,
                    "Could not instanciate class: {0}", e, aClass);
            return null;
        }
    }

    public static InputStream getResourceAsStream(String resourceName) {
        return classLoader.getResourceAsStream(resourceName);
    }
}
