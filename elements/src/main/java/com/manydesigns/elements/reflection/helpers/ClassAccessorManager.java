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

package com.manydesigns.elements.reflection.helpers;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.util.InstanceBuilder;
import com.manydesigns.elements.util.ReflectionUtil;

import java.util.*;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ClassAccessorManager implements ClassAccessorHelper {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Static fields
    //**************************************************************************

    protected static final Properties elementsProperties;
    protected static final ClassAccessorManager manager;

    public static final Logger logger =
            LogUtil.getLogger(ClassAccessorManager.class);

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<ClassAccessorHelper> helperList;
    protected final Map<Object, ClassAccessor> classCache;
    protected boolean classCacheEnabled = true;


    //**************************************************************************
    // Static initialization and methods
    //**************************************************************************

    static {
        elementsProperties = ElementsProperties.getProperties();
        String managerClassName =
                elementsProperties.getProperty(
                        ElementsProperties.CLASS_ACCESSOR_MANAGER_PROPERTY);
        InstanceBuilder<ClassAccessorManager> builder =
                new InstanceBuilder<ClassAccessorManager>(
                        ClassAccessorManager.class,
                        ClassAccessorManager.class,
                        logger);
        manager = builder.createInstance(managerClassName);
    }

    public static ClassAccessorManager getManager() {
        return manager;
    }


    //**************************************************************************
    // Constructors and initialization
    //**************************************************************************

    public ClassAccessorManager() {
        helperList = new ArrayList<ClassAccessorHelper>();
        classCache = new WeakHashMap<Object, ClassAccessor>();

        String listString = elementsProperties.getProperty(
                ElementsProperties.CLASS_ACCESSOR_HELPERS_LIST_PROPERTY);
        if (listString == null) {
            logger.finer("Empty list");
            return;
        }

        String[] helperClassArray = listString.split(",");
        for (String current : helperClassArray) {
            addFieldHelper(current.trim());
        }
    }

    public void addFieldHelper(String helperClassName) {
        LogUtil.finerMF(logger,
                "Adding class accessor helper: {0}", helperClassName);
        ClassAccessorHelper helper =
                (ClassAccessorHelper)ReflectionUtil
                        .newInstance(helperClassName);
        if (helper == null) {
            LogUtil.warningMF(logger,
                    "Failed to add class accessor helper: {0}", helperClassName);
        } else {
            helperList.add(helper);
            LogUtil.finerMF(logger,
                    "Added class accessor helper: {0}", helper);
        }
    }

    //**************************************************************************
    // ClassAccessorHelper implementation
    //**************************************************************************

    public ClassAccessor tryToInstantiateFromClass(Object aClass) {
        if (classCacheEnabled) {
            ClassAccessor cachedResult = classCache.get(aClass);
            if (cachedResult != null) {
                LogUtil.finerMF(logger, "Cache hit for: {0} - Value: {1}",
                        aClass, cachedResult);
                return cachedResult;
            } else {
                LogUtil.finerMF(logger, "Cache miss for: {0}", aClass);
            }
        }
        for (ClassAccessorHelper current : helperList) {
            ClassAccessor result = current.tryToInstantiateFromClass(aClass);
            if (result != null) {
                if (classCacheEnabled) {
                    LogUtil.finerMF(logger, "Caching key: {0} - Value: {1}",
                            aClass, result);
                    classCache.put(aClass, result);
                }
                return result;
            }
        }
        LogUtil.warningMF(logger,
                "Could not instantiate class accessor for: {0}", aClass);
        return null;
    }

    //**************************************************************************
    // Getters and setters
    //**************************************************************************

    public boolean isClassCacheEnabled() {
        return classCacheEnabled;
    }

    public void setClassCacheEnabled(boolean classCacheEnabled) {
        this.classCacheEnabled = classCacheEnabled;
    }

    //**************************************************************************
    // Other methods
    //**************************************************************************

    public void invalidateCache() {
        classCache.clear();
    }
}
