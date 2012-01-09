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

package com.manydesigns.elements.options;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DefaultSelectionProvider implements SelectionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final String name;
    protected final int fieldCount;
    protected final List<Row> rows;

    protected DisplayMode displayMode;

    public final static Logger logger =
            LoggerFactory.getLogger(DefaultSelectionProvider.class);
    public static final String NON_WORD_CHARACTERS =
            " \t\n\f\r\\||!\"£$%&/()='?^[]+*@#<>,;.:-_";

    //**************************************************************************
    // Static builders
    //**************************************************************************

    /*public static DefaultSelectionProvider create(String name,
                                                  Object[] values,
                                                  String[] labels) {
        Row[] rows = new Row[values.length];
        for (int i = 0; i < values.length; i++) {
            Row row = new Row(
                    new Object[] {values[i]},
                    new String[] {labels[i]},
                    true
            );
            rows[i] = row;
        }

        return new DefaultSelectionProvider(name, 1, rows);
    }

    public static DefaultSelectionProvider create(String name,
                                               int fieldCount,
                                               Class[] valueTypes,
                                               Collection<Object[]> valuesAndLabels) {
        int size = valuesAndLabels.size();
        Row[] rows = new Row[size];
        int i = 0;
        for (Object[] valueAndLabel : valuesAndLabels) {
            Object[] values = new Object[fieldCount];
            String[] labels = new String[fieldCount];

            for (int j = 0; j < fieldCount; j++) {
                Class valueType = valueTypes[j];
                values[j] = OgnlUtils.convertValue(valueAndLabel[j*2], valueType);
                labels[j] = OgnlUtils.convertValueToString(valueAndLabel[j*2+1]);
            }

            boolean active = true;
            if(valueAndLabel.length > fieldCount) {
                active = (Boolean) OgnlUtils.convertValue(valueAndLabel[fieldCount], Boolean.class);
            }

            rows[i] = new Row(values, labels, active);
            i++;
        }

        return new DefaultSelectionProvider(name, fieldCount, rows);
    }


    public static DefaultSelectionProvider create(String name,
                                               int fieldCount,
                                               Object[][] valuesArray,
                                               String[][] labelsArray) {
        int size = valuesArray.length;
        Row[] rows = new Row[size];
        for (int i = 0; i < size; i++) {
            Object[] values = valuesArray[i];
            String[] labels = labelsArray[i];
            rows[i] = new Row(values, labels, true);
        }

        return new DefaultSelectionProvider(name, fieldCount, rows);
    }

    public static DefaultSelectionProvider create(String name,
                                               Collection objects,
                                               ClassAccessor classAccessor,
                                               TextFormat textFormat) {
        return create(name, objects, classAccessor,
                new TextFormat[] {textFormat});
    }

    public static DefaultSelectionProvider create(String name,
                                               Collection objects,
                                               ClassAccessor classAccessor,
                                               TextFormat[] textFormats) {
        PropertyAccessor[] keyProperties = classAccessor.getKeyProperties();
        return create(name, objects, textFormats, keyProperties);
    }

    public static DefaultSelectionProvider create(String name,
                                                  Collection objects,
                                                  Class objectClass,
                                                  @Nullable TextFormat textFormat,
                                                  String propertyName) {
        return create(name, objects, objectClass,
                new TextFormat[] {textFormat},
                new String[] {propertyName});
    }

    public static DefaultSelectionProvider create(String name,
                                                  Collection objects,
                                                  Class objectClass,
                                                  TextFormat[] textFormats,
                                                  String[] propertyNames) {
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(objectClass);
        PropertyAccessor[] propertyAccessors =
                new PropertyAccessor[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            String currentName = propertyNames[i];
            try {
                PropertyAccessor propertyAccessor =
                        classAccessor.getProperty(currentName);
                propertyAccessors[i] = propertyAccessor;
            } catch (Throwable e) {
                String msg = MessageFormat.format(
                        "Could not access property: {0}", currentName);
                logger.warn(msg, e);
                throw new IllegalArgumentException(msg, e);
            }
        }
        return create(name, objects, textFormats, propertyAccessors);
    }

    public static DefaultSelectionProvider create(
            String name,
            Collection objects,
            TextFormat textFormat,
            PropertyAccessor propertyAccessor
    ) {
        return create(name, objects,
                new TextFormat[] {textFormat},
                new PropertyAccessor[] {propertyAccessor});
    }

    public static DefaultSelectionProvider create(
            String name,
            Collection objects,
            TextFormat[] textFormats,
            PropertyAccessor[] propertyAccessors
    ) {
        int fieldsCount = propertyAccessors.length;
        Row[] rows = new Row[objects.size()];
        int i = 0;
        for (Object current : objects) {
            Object[] values = new Object[fieldsCount];
            String[] labels = new String[fieldsCount];
            int j = 0;
            for (PropertyAccessor property : propertyAccessors) {
                Object value = property.get(current);
                values[j] = value;
                if (textFormats == null || textFormats[j] == null) {
                    String label = OgnlUtils.convertValueToString(value);
                    labels[j] = label;
                } else {
                    TextFormat textFormat = textFormats[j];
                    labels[j] = textFormat.format(current);
                }
                j++;
            }
            rows[i] = new Row(values, labels, true);
            i++;
        }
        return new DefaultSelectionProvider(name, fieldsCount, rows);
    }*/

    //**************************************************************************
    // Constructor
    //**************************************************************************

    public DefaultSelectionProvider(String name,
                                    int fieldCount,
                                    Row[] rows) {
        this(name, fieldCount, new ArrayList<Row>(Arrays.asList(rows)));
    }

    public DefaultSelectionProvider(String name,
                                    int fieldCount,
                                    List<Row> rows) {
        this.name = name;
        this.fieldCount = fieldCount;
        this.rows = rows;
    }

    public DefaultSelectionProvider(String name,
                                    int fieldCount) {
        this(name, fieldCount, new Row[0]);
    }

    public DefaultSelectionProvider(String name) {
        this(name, 1);
    }

    public DefaultSelectionProvider(DefaultSelectionProvider copy) {
        this(copy.getName(), copy.getFieldCount(), new ArrayList<Row>(copy.rows));
    }

    //**************************************************************************
    // SelectionProvider implementation
    //**************************************************************************

    public String getName() {
        return name;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public SelectionModel createSelectionModel() {
        return new DefaultSelectionModel();
    }

    public void appendRow(Row row) {
        if(row.values.length != fieldCount) {
            throw new IllegalArgumentException("Field count mismatch");
        }
        rows.add(row);
    }

    public void appendRow(Object[] values, String[] labels, boolean active) {
        Row row = new Row(values, labels, active);
        appendRow(row);
    }

    public void appendRow(Object value, String label, boolean active) {
        appendRow(new Object[] { value }, new String[] { label }, active);
    }

    public void ensureActive(Object... values) {
        Row row = null;
        ListIterator<Row> iterator = rows.listIterator();
        while(iterator.hasNext()) {
            Row current = iterator.next();
            boolean found = true;
            for(int i = 0; i < fieldCount; i++) {
                if(!ObjectUtils.equals(values[i], current.getValues()[i])) {
                    found = false;
                    break;
                }
            }
            if(found) {
                row = new Row(values, current.getLabels(), true);
                iterator.set(row);
                break;
            }
        }
        if(row == null) {
            String[] labels = new String[fieldCount];
            for(int i = 0; i < fieldCount; i++) {
                labels[i] = ObjectUtils.toString(values[i]);
            }
            row = new Row(values, labels, true);
            rows.add(row);
        }
    }

    /*public static SelectionProvider create(String name, Class<? extends Enum> enumeration) {
        try {
            Method valuesMethod = enumeration.getMethod("values");
            Enum[] values = (Enum[]) valuesMethod.invoke(null);
            String[] labels = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                labels[i] = values[i].name();
            }
            return create(name, values, labels);
        } catch (Exception e) {
            logger.error("Cannot create Selection provider from enumeration", e);
            throw new Error(e);
        }
    }*/

    //**************************************************************************
    // inner class
    //**************************************************************************

    class DefaultSelectionModel implements SelectionModel {
        public static final String copyright =
                "Copyright (c) 2005-2011, ManyDesigns srl";

        private final Object[] values;
        private final String[] labelSearches;
        private final Map<Object, Option>[] optionsArray;

        private boolean needsValidation;

        public DefaultSelectionModel() {
            values = new Object[fieldCount];
            labelSearches = new String[fieldCount];
            //noinspection unchecked
            optionsArray = new Map[fieldCount];
            for (int i = 0; i < fieldCount; i++) {
                optionsArray[i] = new LinkedHashMap<Object, Option>();
            }
            needsValidation = true;
        }

        public SelectionProvider getSelectionProvider() {
            return DefaultSelectionProvider.this;
        }

        public String getName() {
            return name;
        }

        public Object getValue(int index) {
            validate();
            return values[index];
        }

        public void setValue(int index, Object value) {
            this.values[index] = value;
            needsValidation = true;
        }

        public void setLabelSearch(int index, String labelSearch) {
            this.labelSearches[index] = labelSearch;
            needsValidation = true;
        }

        public String getLabelSearch(int index) {
            return labelSearches[index];
        }

        public Map<Object, Option> getOptions(int index) {
            validate();
            return optionsArray[index];
        }

        public String getOption(int index, Object value, boolean includeInactive) {
            Map<Object, SelectionModel.Option> options = getOptions(index);
            SelectionModel.Option option = options.get(value);
            if(option != null) {
                if(option.active || includeInactive) {
                    return option.label;
                }
            }
            return null;
        }

        private void validate() {
            if (!needsValidation) {
                return;
            }
            needsValidation = false;

            // normalize null in values (only null values after first null)
            boolean foundNull = false;
            for (int j = 0; j < fieldCount; j++) {
                if (foundNull) {
                    values[j] = null;
                } else if (values[j] == null) {
                    foundNull = true;
                }
                // clean options
                optionsArray[j].clear();
            }

            int maxMatchingIndex = -1;
            for (Row row : rows) {
                Object[] currentValueRow = row.getValues();
                String[] currentLabelRow = row.getLabels();
                boolean matching = true;
                for (int j = 0; j < fieldCount; j++) {
                    Object cellValue = currentValueRow[j];
                    String cellLabel = currentLabelRow[j];
                    Object value = values[j];
                    String labelSearch = labelSearches[j];

                    if (matching && matchLabel(cellLabel, labelSearch)) {
                        Option currentOption = optionsArray[j].get(cellValue);
                        if(currentOption == null || !currentOption.active) {
                            optionsArray[j].put(cellValue, new Option(cellValue, cellLabel, row.isActive()));
                        }
                    }

                    if (matching && value != null
                            && value.equals(cellValue)) {
                        if (j > maxMatchingIndex) {
                            maxMatchingIndex = j;
                        }
                    } else if (matching && value != null &&
                            value instanceof Object[]
                            && ArrayUtils.contains((Object[]) value, cellValue)) {
                        if (j > maxMatchingIndex) {
                            maxMatchingIndex = j;
                        }
                    } else {
                        matching = false;
                    }
                }
            }

            for (int i = maxMatchingIndex + 1; i < fieldCount; i++) {
                values[i] = null;
            }
        }
    }

    private boolean matchLabel(String cellLabel, String labelSearch2) {
        if (labelSearch2 == null || labelSearch2.length() == 0) {
            return true;
        }
        cellLabel = cellLabel.toLowerCase();
        labelSearch2 = labelSearch2.toLowerCase();
        String[] cellLabelArray =
                StringUtils.split(cellLabel, NON_WORD_CHARACTERS);
        for (String current : cellLabelArray) {
            if (current.startsWith(labelSearch2)) {
                return true;
            }
        }
        return false;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public static class Row {
        final Object[] values;
        final String[] labels;
        final boolean active;

        public Row(Object[] values, String[] labels, boolean active) {
            this.values = values;
            this.labels = labels;
            this.active = active;
        }

        public Object[] getValues() {
            return values;
        }

        public String[] getLabels() {
            return labels;
        }

        public boolean isActive() {
            return active;
        }
    }
}
