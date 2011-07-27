/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.InSummary;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.options.SelectionModel;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.TextFormat;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class TableFormBuilder extends AbstractFormBuilder {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static int DEFAULT_N_ROWS = 1;

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Map<String, TextFormat> hrefTextFormats;
    protected final Map<String, TextFormat> titleTextFormats;

    protected List<PropertyAccessor> propertyAccessors;
    protected int nRows = DEFAULT_N_ROWS;
    protected Mode mode = Mode.EDIT;

    public static final Logger logger =
            LoggerFactory.getLogger(TableFormBuilder.class);


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public TableFormBuilder(Class aClass) {
        this(JavaClassAccessor.getClassAccessor(aClass));
    }

    public TableFormBuilder(ClassAccessor classAccessor) {
        super(classAccessor);

        hrefTextFormats = new HashMap<String, TextFormat>();
        titleTextFormats = new HashMap<String, TextFormat>();
    }


    //**************************************************************************
    // Builder configuration
    //**************************************************************************

    public TableFormBuilder configFields(String... fieldNames) {
        propertyAccessors = new ArrayList<PropertyAccessor>();
        for (String currentField : fieldNames) {
            try {
                PropertyAccessor accessor =
                        classAccessor.getProperty(currentField);
                propertyAccessors.add(accessor);
            } catch (NoSuchFieldException e) {
                logger.warn("Field not found: {}", currentField);
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

    public TableFormBuilder configMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public TableFormBuilder configSelectionProvider(SelectionProvider selectionProvider,
                                            String... fieldNames) {
        selectionProviders.put(fieldNames, selectionProvider);
        return this;
    }

    public void configReflectiveFields() {
        propertyAccessors = new ArrayList<PropertyAccessor>();
        for (PropertyAccessor current : classAccessor.getProperties()) {
            if (skippableProperty(current)) {
                continue;
            }

            // check if field is in summary
            InSummary inSummaryAnnotation =
                    current.getAnnotation(InSummary.class);
            if (inSummaryAnnotation != null && !inSummaryAnnotation.value()) {
                logger.debug("Skipping non-in-summary field: {}",
                        current.getName());
                continue;
            }

            propertyAccessors.add(current);
        }
    }

    public TableFormBuilder configHrefTextFormat(
            String fieldName, TextFormat hrefTextFormat) {
        hrefTextFormats.put(fieldName, hrefTextFormat);
        return this;
    }

    public TableFormBuilder configTitleTextFormat(
            String fieldName, TextFormat titleTextFormat) {
        titleTextFormats.put(fieldName, titleTextFormat);
        return this;
    }


    //**************************************************************************
    // Building
    //**************************************************************************

    public TableForm build() {
        if (propertyAccessors == null) {
            configReflectiveFields();
        }

        // remove unused (or partially used) selection providers
        removeUnusedSelectionProviders(propertyAccessors);

        PropertyAccessor[] propertyAccessorsArray =
                new PropertyAccessor[propertyAccessors.size()];
        propertyAccessors.toArray(propertyAccessorsArray);

        TableForm tableForm = new TableForm(nRows, propertyAccessorsArray);

        if (null!=prefix && prefix.length()>0) {
            tableForm.setPrefix(prefix);
        }

        // set up the columns
        setupColumns(tableForm);

        // set up the rows
        setupRows(tableForm);

        return tableForm;
    }



    protected void setupColumns(TableForm tableForm) {
        for (TableForm.Column column : tableForm.getColumns()) {
            String propertyName = column.getPropertyAccessor().getName();
            column.setHrefTextFormat(hrefTextFormats.get(propertyName));
            column.setTitleTextFormat(titleTextFormats.get(propertyName));
        }
    }

    protected void setupRows(TableForm tableForm) {
        int index = 0;
        for (TableForm.Row row : tableForm.getRows()) {
            String rowPrefix =
                    StringUtils.join(new Object[] {prefix, "row", index, "_"});

            for (int j = 0; j < propertyAccessors.size(); j++) {
                PropertyAccessor propertyAccessor = propertyAccessors.get(j);
                Field field = buildField(propertyAccessor, rowPrefix);
                if (field == null) {
                    logger.warn("Cannot instanciate field for property {}",
                            propertyAccessor);
                    break;
                }
                row.fields[j] = field;
            }

            // handle cascaded select fields
            for (Map.Entry<String[], SelectionProvider> current :
                    selectionProviders.entrySet()) {
                String[] fieldNames = current.getKey();
                SelectionProvider selectionProvider = current.getValue();
                SelectionModel selectionModel =
                        selectionProvider.createSelectionModel();

                SelectField previousField = null;
                for (int i = 0; i < fieldNames.length; i++) {
                    int fieldIndex =
                            findFieldIndexByName(tableForm, fieldNames[i]);
                    SelectField selectField =
                            (SelectField) row.getFields()[fieldIndex];
                    selectField.setSelectionModel(selectionModel);
                    selectField.setSelectionModelIndex(i);
                    if (previousField != null) {
                        selectField.setPreviousSelectField(previousField);
                        previousField.setNextSelectField(selectField);
                    }
                    previousField = selectField;
                }
            }

            index++;
        }
    }

    private int findFieldIndexByName(TableForm tableForm, String fieldName) {
        TableForm.Column[] columns = tableForm.getColumns();
        for (int index = 0; index < columns.length; index++) {
            TableForm.Column column  = columns[index];
            if (column.getPropertyAccessor().getName().equals(fieldName)) {
                return index;
            }
        }
        return -1;
    }

    private Field buildField(PropertyAccessor propertyAccessor,
                             String rowPrefix) {
        Field field = null;
        String fieldName = propertyAccessor.getName();
        for (Map.Entry<String[], SelectionProvider> current
                : selectionProviders.entrySet()) {
            String[] fieldNames = current.getKey();
            int index = ArrayUtils.indexOf(fieldNames, fieldName);
            if (index >= 0) {
                field = new SelectField(propertyAccessor, mode, rowPrefix);
                break;
            }
        }
        if (field == null) {
            field = manager.tryToInstantiateField(
                    classAccessor, propertyAccessor, mode, rowPrefix);
        }

        return field;
    }

}
