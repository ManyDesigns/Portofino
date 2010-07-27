/*
 * Copyright (C) 2005-2009 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.composites;

import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.helpers.FieldHelperManager;
import com.manydesigns.elements.hyperlinks.HyperlinkGenerator;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableFormBuilder {
    public static final String copyright =
            "Copyright (c) 2005-2009, ManyDesigns srl";

    public final static int DEFAULT_N_ROWS = 1;

    private final ClassAccessor classAccessor;

    protected List<PropertyAccessor> propertyAccessors;
    protected Map<String, HyperlinkGenerator> hyperlinkGenerators;
    protected String prefix;
    protected int nRows = DEFAULT_N_ROWS;

    public TableFormBuilder(Class clazz) {
        this(new JavaClassAccessor(clazz));
    }

    public TableFormBuilder(ClassAccessor classAccessor) {
        this.classAccessor = classAccessor;
        hyperlinkGenerators = new HashMap<String, HyperlinkGenerator>();
    }

    public TableFormBuilder configFields(String... fieldNames) {
        propertyAccessors = new ArrayList<PropertyAccessor>();
        for (String currentField : fieldNames) {
            try {
                PropertyAccessor accessor =
                        classAccessor.getProperty(currentField);
                propertyAccessors.add(accessor);
            } catch (NoSuchFieldException e) {
                // rethrow as Error, so no exception is declared
                // by the method. This is important so the builder
                // can be used in inline field initializations,
                // without the need for a constructor. E.g.:
                // public final Element myElement =
                //   new ClassTableFormBuilder(MyClass.class)
                //   .configFields("field1", "field2").build();

                throw new Error(e);
            }
        }
        return this;
    }

    public TableFormBuilder configPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public TableFormBuilder configNRows(int nRows) {
        this.nRows = nRows;
        return this;
    }

    public void configReflectiveFields() {
        propertyAccessors = new ArrayList<PropertyAccessor>();

        for (PropertyAccessor current : classAccessor.getProperties()) {
            if (Modifier.isStatic(current.getModifiers())) {
                continue;
            }
            propertyAccessors.add(current);
        }
    }

    public TableFormBuilder configHyperlinkGenerator(
            String fieldName, HyperlinkGenerator hyperlinkGenerator) {
        hyperlinkGenerators.put(fieldName, hyperlinkGenerator);
        return this;
    }

    public TableForm build() {
        TableForm tableForm = new TableForm(nRows);
        FieldHelperManager manager = FieldHelperManager.getManager();

        if (propertyAccessors == null) {
            configReflectiveFields();
        }

        String[] rowPrefix = new String[nRows];
        for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
            Object[] idArgs = {prefix, "row", rowIndex, "."};
            rowPrefix[rowIndex] = StringUtils.join(idArgs);
        }

        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            TableFormColumn column =
                    new TableFormColumn(propertyAccessor, nRows);
            tableForm.add(column);

            HyperlinkGenerator hyperlinkGenerator =
                    hyperlinkGenerators.get(propertyAccessor.getName());
            column.setHyperlinkGenerator(hyperlinkGenerator);

            for (int i = 0; i < nRows; i++) {
                Field field = manager.tryToInstantiateField(
                        classAccessor, propertyAccessor, rowPrefix[i]);

                if (field == null) {
                    throw new Error("Cannot instanciate field for: " +
                            propertyAccessor.getName());
                }
                column.add(field);
            }
        }

        return tableForm;
    }

}
