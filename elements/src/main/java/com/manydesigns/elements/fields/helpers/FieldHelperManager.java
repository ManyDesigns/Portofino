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

package com.manydesigns.elements.fields.helpers;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.search.SearchField;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.InstanceBuilder;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class FieldHelperManager implements FieldHelper {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected static final Properties elementsProperties;
    protected static final FieldHelperManager manager;

    public static final Logger logger =
            LogUtil.getLogger(FieldHelperManager.class);

    protected ArrayList<FieldHelper> fieldHelperList;

    static {
        elementsProperties = ElementsProperties.getProperties();
        String managerClassName =
                elementsProperties.getProperty(
                        ElementsProperties.MANAGER_PROPERTY);
        InstanceBuilder<FieldHelperManager> builder =
                new InstanceBuilder<FieldHelperManager>(
                        FieldHelperManager.class,
                        FieldHelperManager.class,
                        logger);
        manager = builder.createInstance(managerClassName);
    }

    public static FieldHelperManager getManager() {
        return manager;
    }

    public FieldHelperManager() {
        fieldHelperList = new ArrayList<FieldHelper>();
        String listString = elementsProperties.getProperty(
                ElementsProperties.LIST_PROPERTY);
        if (listString == null) {
            logger.finer("Empty list");
            return;
        }

        String[] helperClassArray = listString.split(",");
        for (String current : helperClassArray) {
            addFieldHelper(current);
        }
    }

    protected void addFieldHelper(String fieldHelperClassName) {
        ClassLoader cl = FieldHelperManager.class.getClassLoader();

        String helperClassName = fieldHelperClassName.trim();
        LogUtil.finerMF(logger,
                    "Adding field helper: {0}", helperClassName);
        try {
            Class helperClass = cl.loadClass(helperClassName);
            Constructor constructor = helperClass.getConstructor();
            FieldHelper helper = (FieldHelper)constructor.newInstance();
            fieldHelperList.add(helper);
        } catch (Throwable e) {
            LogUtil.warningMF(logger, "Cannot load or instanciate: {0}", e,
                    helperClassName);
        }
    }

    public Field tryToInstantiateField(ClassAccessor classAccessor,
                                  PropertyAccessor propertyAccessor,
                                  String prefix) {
        for (FieldHelper current : fieldHelperList) {
            Field result = current.tryToInstantiateField(classAccessor,
                    propertyAccessor, prefix);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public SearchField tryToInstantiateSearchField(
            ClassAccessor classAccessor,
            PropertyAccessor propertyAccessor,
            String prefix) {
        for (FieldHelper current : fieldHelperList) {
            SearchField result =
                    current.tryToInstantiateSearchField(
                            classAccessor, propertyAccessor, prefix);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
