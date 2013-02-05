/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.options;

import com.manydesigns.elements.util.Util;
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final String name;
    protected final int fieldCount;
    protected final List<Row> rows;

    protected DisplayMode displayMode;
    protected SearchDisplayMode searchDisplayMode;

    protected String createNewValueHref;
    protected String createNewValueText;

    public final static Logger logger =
            LoggerFactory.getLogger(DefaultSelectionProvider.class);
    public static final String NON_WORD_CHARACTERS =
            " \t\n\f\r\\||!\"\u00ac\u00a3$\u201a\u00c7\u00a8%&/()='?^[]+*@#<>,;.:-_";

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

    public void sortByLabel() {
        Comparator<Row> comparator = new Comparator<Row>() {
            public int compare(Row r1, Row r2) {
                for(int i = 0; i < r1.labels.length; i++) {
                    int comp = Util.compare(r1.getLabels()[i], r2.getLabels()[i]);
                    if(comp != 0) {
                        return comp;
                    }
                }
                return 0;
            }
        };
        Collections.sort(rows, comparator);
    }

    //**************************************************************************
    // inner class
    //**************************************************************************

    class DefaultSelectionModel implements SelectionModel {
        public static final String copyright =
                "Copyright (c) 2005-2013, ManyDesigns srl";

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

                    //#163 cellLabel != null
                    if (matching && cellLabel != null && matchLabel(cellLabel, labelSearch)) {
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
        String[] searchLabelArray =
                StringUtils.split(labelSearch2, NON_WORD_CHARACTERS);
        for (int i = 0; i <= cellLabelArray.length - searchLabelArray.length; i++) {
            boolean allMatch = true;
            for(int j = 0; j < searchLabelArray.length; j++) {
                allMatch &= cellLabelArray[i + j].startsWith(searchLabelArray[j]);
            }
            if(allMatch) {
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

    public SearchDisplayMode getSearchDisplayMode() {
        return searchDisplayMode;
    }

    public void setSearchDisplayMode(SearchDisplayMode searchDisplayMode) {
        this.searchDisplayMode = searchDisplayMode;
    }

    public String getCreateNewValueHref() {
        return createNewValueHref;
    }

    public void setCreateNewValueHref(String createNewValueHref) {
        this.createNewValueHref = createNewValueHref;
    }

    public String getCreateNewValueText() {
        return createNewValueText;
    }

    public void setCreateNewValueText(String createNewValueText) {
        this.createNewValueText = createNewValueText;
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
