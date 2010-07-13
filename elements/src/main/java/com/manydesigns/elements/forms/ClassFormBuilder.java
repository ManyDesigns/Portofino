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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Field;
import com.manydesigns.elements.FieldHelper;
import com.manydesigns.elements.hyperlinks.HyperlinkGenerator;
import com.manydesigns.elements.annotations.FieldSet;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;

import java.lang.reflect.Modifier;
import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ClassFormBuilder {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static int DEFAULT_N_COLUMNS = 1;

    private final ClassAccessor classAccessor;

    protected List<ArrayList<PropertyAccessor>> groupedPropertyAccessors;
    protected List<String> fieldSetNames;
    protected Map<String, HyperlinkGenerator> hyperlinkGenerators;
    protected String prefix;
    protected int nColumns = DEFAULT_N_COLUMNS;

    public ClassFormBuilder(Class clazz) {
        this(new JavaClassAccessor(clazz));
    }

    public ClassFormBuilder(ClassAccessor classAccessor) {
        this.classAccessor = classAccessor;
        hyperlinkGenerators = new HashMap<String, HyperlinkGenerator>();
    }

    public ClassFormBuilder configFields(String... fieldNames) {
        String[][] groupedFieldNames = new String[1][];
        groupedFieldNames[0] = fieldNames;
        return configFields(groupedFieldNames);
    }

    public ClassFormBuilder configFields(String[]... groupedFieldNames) {
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
                    // rethrow as Error, so no exception is declared
                    // by the method. This is important so the builder
                    // can be used in inline field initializations,
                    // without the need for a constructor. E.g.:
                    // public final Element myElement =
                    //   new ClassFormBuilder(MyClass.class)
                    //   .configFields("field1", "field2").build();
                    
                    throw new Error(e);
                }
            }
        }
        return this;
    }

    public ClassFormBuilder configPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public ClassFormBuilder configNColumns(int nColumns) {
        this.nColumns = nColumns;
        return this;
    }

    public ClassFormBuilder configFieldSetNames(String... fieldSetNames) {
        this.fieldSetNames = Arrays.asList(fieldSetNames);
        return this;
    }

    public ClassFormBuilder configReflectiveFields() {
        groupedPropertyAccessors = new ArrayList<ArrayList<PropertyAccessor>>();
        fieldSetNames = new ArrayList<String>();

        ArrayList<PropertyAccessor> currentGroup = null;
        String currentGroupName = null;
        for (PropertyAccessor current : classAccessor.getProperties()) {
            if (Modifier.isStatic(current.getModifiers())) {
                continue;
            }
            
            String groupName = null;
            if (current.isAnnotationPresent(com.manydesigns.elements.annotations.FieldSet.class)) {
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
        return this;
    }

    public ClassFormBuilder configHyperlinkGenerator(
            String fieldName, HyperlinkGenerator hyperlinkGenerator) {
        hyperlinkGenerators.put(fieldName, hyperlinkGenerator);
        return this;
    }

    public Form build() {
        Form form = new Form();
        FieldHelper fieldHelper = ElementsThreadLocals.getFieldHelper();


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
                Field field = fieldHelper.tryToInstantiate(
                        classAccessor, propertyAccessor, prefix);

                if (field == null) {
                    continue;
                }
                fieldSet.add(field);

                HyperlinkGenerator hyperlinkGenerator =
                        hyperlinkGenerators.get(propertyAccessor.getName());
                field.setHyperlinkGenerator(hyperlinkGenerator);
            }
        }
        return form;
    }
}
