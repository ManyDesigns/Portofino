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
import com.manydesigns.elements.util.ReflectionUtil;

import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class FieldsManager implements FieldHelper {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Static fields
    //**************************************************************************

    protected static final Properties elementsProperties;
    protected static final FieldsManager manager;

    public static final Logger logger =
            LogUtil.getLogger(FieldsManager.class);

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final ArrayList<FieldHelper> helperList;


    //**************************************************************************
    // Static initialization and methods
    //**************************************************************************

    static {
        elementsProperties = ElementsProperties.getProperties();
        String managerClassName =
                elementsProperties.getProperty(
                        ElementsProperties.FIELDS_MANAGER_PROPERTY);
        InstanceBuilder<FieldsManager> builder =
                new InstanceBuilder<FieldsManager>(
                        FieldsManager.class,
                        FieldsManager.class,
                        logger);
        manager = builder.createInstance(managerClassName);
    }

    public static FieldsManager getManager() {
        return manager;
    }


    //**************************************************************************
    // Constructors and initialization
    //**************************************************************************

    public FieldsManager() {
        helperList = new ArrayList<FieldHelper>();
        String listString = elementsProperties.getProperty(
                ElementsProperties.FIELDS_LIST_PROPERTY);
        if (listString == null) {
            logger.finer("Empty list");
            return;
        }

        String[] helperClassArray = listString.split(",");
        for (String current : helperClassArray) {
            addFieldHelper(current);
        }
    }

    public void addFieldHelper(String fieldHelperClassName) {
        String helperClassName = fieldHelperClassName.trim();
        LogUtil.finerMF(logger,
                    "Adding field helper: {0}", helperClassName);
        FieldHelper helper =
                (FieldHelper) ReflectionUtil.newInstance(helperClassName);
        if (helper == null) {
            LogUtil.warningMF(logger,
                        "Failed to add field helper: {0}", helperClassName);
        } else {
            helperList.add(helper);
            LogUtil.finerMF(logger,
                        "Added field helper: {0}", helper);
        }
    }

    //**************************************************************************
    // FieldHelper implementation
    //**************************************************************************

    public Field tryToInstantiateField(ClassAccessor classAccessor,
                                  PropertyAccessor propertyAccessor,
                                  String prefix) {
        for (FieldHelper current : helperList) {
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
        for (FieldHelper current : helperList) {
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
