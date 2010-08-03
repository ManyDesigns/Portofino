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

package com.manydesigns.elements.forms;

import com.manydesigns.elements.annotations.FieldSet;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.helpers.FieldHelperManager;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class FormBuilder {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static int DEFAULT_N_COLUMNS = 1;

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final ClassAccessor classAccessor;

    protected List<ArrayList<PropertyAccessor>> groupedPropertyAccessors;
    protected List<String> fieldSetNames;
    protected String prefix;
    protected int nColumns = DEFAULT_N_COLUMNS;

    public static final Logger logger = LogUtil.getLogger(FormBuilder.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public FormBuilder(Class clazz) {
        this(new JavaClassAccessor(clazz));
    }

    public FormBuilder(ClassAccessor classAccessor) {
        LogUtil.entering(logger, "FormBuilder", classAccessor);

        this.classAccessor = classAccessor;

        LogUtil.exiting(logger, "FormBuilder");
    }

    //**************************************************************************
    // Builder configuration
    //**************************************************************************

    public FormBuilder configFields(String... fieldNames) {
        LogUtil.fineMF(logger, "Configuring fields to: {0}", fieldNames);

        String[][] groupedFieldNames = new String[1][];
        groupedFieldNames[0] = fieldNames;
        return configFields(groupedFieldNames);
    }

    public FormBuilder configFields(String[]... groupedFieldNames) {
        LogUtil.entering(logger, "configFields", groupedFieldNames);

        groupedPropertyAccessors = new ArrayList<ArrayList<PropertyAccessor>>();
        for (String[] currentNameGroup : groupedFieldNames) {
            ArrayList<PropertyAccessor> currentPropertyGroup =
                    new ArrayList<PropertyAccessor>();
            groupedPropertyAccessors.add(currentPropertyGroup);
            for (String currentField : currentNameGroup) {
                try {
                    PropertyAccessor accessor =
                            classAccessor.getProperty(currentField);
                    currentPropertyGroup.add(accessor);
                } catch (NoSuchFieldException e) {
                    LogUtil.warningMF(logger, "Field not found: {0}", e,
                            currentField);
                }
            }
        }

        LogUtil.exiting(logger, "configFields");
        return this;
    }

    public FormBuilder configPrefix(String prefix) {
        LogUtil.fineMF(logger, "Configuring prefix to: {0}", prefix);

        this.prefix = prefix;
        return this;
    }

    public FormBuilder configNColumns(int nColumns) {
        LogUtil.fineMF(logger, "Configuring nColumns to: {0}", nColumns);

        this.nColumns = nColumns;
        return this;
    }

    public FormBuilder configFieldSetNames(String... fieldSetNames) {
        LogUtil.fineMF(logger,
                "Configuring configFieldSetNames to: {0}", fieldSetNames);

        this.fieldSetNames = Arrays.asList(fieldSetNames);
        return this;
    }

    public FormBuilder configReflectiveFields() {
        LogUtil.entering(logger, "configReflectiveFields");

        groupedPropertyAccessors = new ArrayList<ArrayList<PropertyAccessor>>();
        fieldSetNames = new ArrayList<String>();

        ArrayList<PropertyAccessor> currentGroup = null;
        String currentGroupName = null;
        for (PropertyAccessor current : classAccessor.getProperties()) {
            if (Modifier.isStatic(current.getModifiers())) {
                continue;
            }
            
            String groupName = null;
            if (current.isAnnotationPresent(
                    com.manydesigns.elements.annotations.FieldSet.class)) {
                groupName = current.getAnnotation(FieldSet.class).value();
            }

            if ((currentGroup == null)
                    || (groupName != null && !groupName.equals(currentGroupName))) {
                currentGroup = new ArrayList<PropertyAccessor>();
                groupedPropertyAccessors.add(currentGroup);
                fieldSetNames.add(groupName);
                currentGroupName = groupName;
            }
            currentGroup.add(current);
        }

        LogUtil.exiting(logger, "configReflectiveFields");
        return this;
    }

    //**************************************************************************
    // Building
    //**************************************************************************

    public Form build() {
        LogUtil.entering(logger, "build");

        Form form = new Form();
        FieldHelperManager manager = FieldHelperManager.getManager();


        if (groupedPropertyAccessors == null) {
            configReflectiveFields();
        }

        for (int i = 0; i < groupedPropertyAccessors.size(); i++) {
            ArrayList<PropertyAccessor> group = groupedPropertyAccessors.get(i);
            String fieldSetName;
            if (fieldSetNames == null) {
                fieldSetName = null;
            } else {
                fieldSetName = fieldSetNames.get(i);
            }
            com.manydesigns.elements.forms.FieldSet fieldSet =
                    new com.manydesigns.elements.forms.FieldSet(
                            fieldSetName, nColumns);
            form.add(fieldSet);
            for (PropertyAccessor propertyAccessor : group) {
                Field field = manager.tryToInstantiateField(
                        classAccessor, propertyAccessor, prefix);

                if (field == null) {
                    LogUtil.warningMF(logger,
                            "Cannot instanciate field for property {0}",
                            propertyAccessor);
                    continue;
                }
                fieldSet.add(field);
            }
        }

        LogUtil.exiting(logger, "build");
        return form;
    }
}
