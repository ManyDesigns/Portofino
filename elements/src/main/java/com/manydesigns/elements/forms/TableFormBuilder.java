/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Builder for {@link TableForm}s.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class TableFormBuilder extends AbstractFormBuilder {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public final static int DEFAULT_N_ROWS = 1;

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Map<String, TextFormat> headerTextFormats;
    protected final Map<String, TextFormat> hrefTextFormats;
    protected final Map<String, TextFormat> titleTextFormats;

    protected List<PropertyAccessor> propertyAccessors;

    public static final Logger logger =
            LoggerFactory.getLogger(TableFormBuilder.class);


    public TableFormBuilder(Class aClass) {
        this(JavaClassAccessor.getClassAccessor(aClass));
    }

    public TableFormBuilder(ClassAccessor classAccessor) {
        super(classAccessor);

        headerTextFormats = new HashMap<>();
        hrefTextFormats = new HashMap<>();
        titleTextFormats = new HashMap<>();
    }


    //**************************************************************************
    // Builder configuration
    //**************************************************************************

    public TableFormBuilder configFields(String... fieldNames) {
        propertyAccessors = new ArrayList<>();
        for (String currentField : fieldNames) {
            try {
                PropertyAccessor accessor = classAccessor.getProperty(currentField);
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
        propertyAccessors = new ArrayList<>();
        for (PropertyAccessor current : classAccessor.getProperties()) {
            if (!isPropertyVisible(current)) {
                continue;
            }

            propertyAccessors.add(current);
        }
    }

    public boolean isPropertyVisible(PropertyAccessor current) {
        if (!isPropertyEnabled(current)) {
            return false;
        }

        // check if field is in summary
        InSummary inSummaryAnnotation =
                current.getAnnotation(InSummary.class);
        if (inSummaryAnnotation != null && !inSummaryAnnotation.value()) {
            logger.debug("Skipping non-in-summary field: {}",
                    current.getName());
            return false;
        }
        return true;
    }

    public TableFormBuilder configHeaderTextFormat(
            String fieldName, TextFormat hrefTextFormat) {
        headerTextFormats.put(fieldName, hrefTextFormat);
        return this;
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

        TableForm tableForm = new TableForm(rowBuilder(), propertyAccessorsArray);

        if (StringUtils.isNotBlank(prefix)) {
            tableForm.setPrefix(prefix);
        }

        // set up the columns
        setupColumns(tableForm);

        return tableForm;
    }



    protected void setupColumns(TableForm tableForm) {
        for (TableForm.Column column : tableForm.getColumns()) {
            String propertyName = column.getPropertyAccessor().getName();
            column.setHeaderTextFormat(headerTextFormats.get(propertyName));
            column.setHrefTextFormat(hrefTextFormats.get(propertyName));
            column.setTitleTextFormat(titleTextFormats.get(propertyName));
        }
    }

    protected BiFunction<TableForm, Integer, TableForm.Row> rowBuilder() {
        return (tableForm, index) -> {
            TableForm.Row row = tableForm.newRow(index);

            String rowPrefix =
                    StringUtils.join(new Object[]{prefix, "row", index, "_"});

            for (PropertyAccessor propertyAccessor : propertyAccessors) {
                Field field = buildField(propertyAccessor, rowPrefix);
                if (field == null) {
                    logger.warn("Cannot instantiate field for property {}",
                            propertyAccessor);
                } else {
                    row.add(field);
                }
            }

            // handle cascaded select fields
            for (Map.Entry<String[], SelectionProvider> current :
                    selectionProviders.entrySet()) {
                setupSelectionProvidersForRow(tableForm, row, current);
            }

            return row;
        };
    }

    protected void setupSelectionProvidersForRow(TableForm tableForm, TableForm.Row row,
                                                 Map.Entry<String[], SelectionProvider> current) {
        String[] fieldNames = current.getKey();
        SelectionProvider selectionProvider = current.getValue();
        SelectionModel selectionModel =
                selectionProvider.createSelectionModel();

        SelectField previousField = null;
        for (int i = 0; i < fieldNames.length; i++) {
            int fieldIndex =
                    findFieldIndexByName(tableForm, fieldNames[i]);
            SelectField selectField =
                    (SelectField) row.get(fieldIndex);
            selectField.setSelectionModel(selectionModel);
            selectField.setSelectionModelIndex(i);
            if (previousField != null) {
                selectField.setPreviousSelectField(previousField);
                previousField.setNextSelectField(selectField);
            }
            previousField = selectField;
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

    protected Field buildField(PropertyAccessor propertyAccessor, String rowPrefix) {
        Field field = null;
        String fieldName = propertyAccessor.getName();
        for (Map.Entry<String[], SelectionProvider> current : selectionProviders.entrySet()) {
            String[] fieldNames = current.getKey();
            int index = ArrayUtils.indexOf(fieldNames, fieldName);
            if (index >= 0) {
                field = buildSelectField(propertyAccessor, null, rowPrefix);
                break;
            }
        }
        return buildField(propertyAccessor, field, rowPrefix);
    }

    public List<PropertyAccessor> getPropertyAccessors() {
        return propertyAccessors != null ? Collections.unmodifiableList(propertyAccessors) : null;
    }
}
