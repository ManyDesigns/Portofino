/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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
            "Copyright (C) 2005-2021 ManyDesigns srl";
    public static final Comparator<OptionProvider.Option> OPTION_COMPARATOR_BY_LABEL = (r1, r2) -> {
        for (int i = 0; i < r1.labels.length; i++) {
            int comp = Util.compare(r1.getLabels()[i], r2.getLabels()[i]);
            if (comp != 0) {
                return comp;
            }
        }
        return 0;
    };

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final String name;
    protected final int fieldCount;
    protected final OptionProvider optionProvider;

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

    public DefaultSelectionProvider(String name, int fieldCount, OptionProvider optionProvider) {
        this.name = name;
        this.fieldCount = fieldCount;
        this.optionProvider = optionProvider;
    }

    public DefaultSelectionProvider(String name, int fieldCount, OptionProvider.Option[] options) {
        this(name, fieldCount, new ArrayList<>(Arrays.asList(options)));
    }

    public DefaultSelectionProvider(String name, int fieldCount, List<OptionProvider.Option> options) {
        this(name, fieldCount, new StaticOptionProvider(options));
    }

    public DefaultSelectionProvider(String name, int fieldCount) {
        this(name, fieldCount, new OptionProvider.Option[0]);
    }

    public DefaultSelectionProvider(String name) {
        this(name, 1);
    }

    public DefaultSelectionProvider(DefaultSelectionProvider copy) {
        this(copy.getName(), copy.getFieldCount(), copy.optionProvider);
    }

    //**************************************************************************
    // Options
    //**************************************************************************

    public List<OptionProvider.Option> getOptions() {
        return optionProvider.getOptions();
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

    @Deprecated
    public void appendRow(OptionProvider.Option option) {
        appendOption(option);
    }

    public void appendOption(OptionProvider.Option option) {
        if(option.values.length != fieldCount) {
            throw new IllegalArgumentException("Field count mismatch");
        }
        getOptions().add(option);
    }

    @Deprecated
    public void appendRow(Object[] values, String[] labels, boolean active) {
        appendOption(values, labels, active);
    }

    public void appendOption(Object[] values, String[] labels, boolean active) {
        OptionProvider.Option option = new OptionProvider.Option(values, labels, active);
        appendOption(option);
    }

    @Deprecated
    public void appendRow(Object value, String label, boolean active) {
        appendOption(value, label, active);
    }

    public void appendOption(Object value, String label, boolean active) {
        appendOption(new Object[] { value }, new String[] { label }, active);
    }

    public void ensureActive(Object... values) {
        OptionProvider.Option option = null;
        ListIterator<OptionProvider.Option> iterator = getOptions().listIterator();
        while(iterator.hasNext()) {
            OptionProvider.Option current = iterator.next();
            boolean found = true;
            for(int i = 0; i < fieldCount; i++) {
                if(!ObjectUtils.equals(values[i], current.getValues()[i])) {
                    found = false;
                    break;
                }
            }
            if(found) {
                option = new OptionProvider.Option(values, current.getLabels(), true);
                iterator.set(option);
                break;
            }
        }
        if(option == null) {
            String[] labels = new String[fieldCount];
            for(int i = 0; i < fieldCount; i++) {
                labels[i] = ObjectUtils.toString(values[i]);
            }
            option = new OptionProvider.Option(values, labels, true);
            getOptions().add(option);
        }
    }

    public void sortByLabel() {
        getOptions().sort(OPTION_COMPARATOR_BY_LABEL);
    }

    private static class StaticOptionProvider implements OptionProvider {
        private final List<Option> options;

        public StaticOptionProvider(List<Option> options) {
            this.options = options;
        }

        @Override
        public List<Option> getOptions() {
            return options;
        }
    }

    //**************************************************************************
    // inner class
    //**************************************************************************

    class DefaultSelectionModel implements SelectionModel {
        public static final String copyright =
                "Copyright (C) 2005-2021 ManyDesigns srl";

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
                optionsArray[i] = new LinkedHashMap<>();
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
            if(values[index] != null) {
                validate(); //Potentially, this will set it this.values[index] to null, but it will never set it to a non-null value
            }
            return values[index];
        }

        public void setValue(int index, Object value) {
            Object previousValue = this.values[index];
            this.values[index] = value;
            needsValidation = needsValidation || !Objects.equals(previousValue, value);
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
            for (OptionProvider.Option option : DefaultSelectionProvider.this.getOptions()) {
                Object[] currentValueRow = option.getValues();
                String[] currentLabelRow = option.getLabels();
                for (int j = 0; j < fieldCount; j++) {
                    Object cellValue = currentValueRow[j];
                    String cellLabel = currentLabelRow[j];
                    Object value = values[j];
                    String labelSearch = labelSearches[j];

                    //#163 cellLabel != null
                    if (cellLabel != null && matchLabel(cellLabel, labelSearch)) {
                        Option currentOption = optionsArray[j].get(cellValue);
                        if(currentOption == null || !currentOption.active) {
                            optionsArray[j].put(cellValue, new Option(cellValue, cellLabel, option.isActive()));
                        }
                    }

                    if (value != null && value.equals(cellValue)) {
                        if (j > maxMatchingIndex) {
                            maxMatchingIndex = j;
                        }
                    } else if (value instanceof Object[] &&
                               ArrayUtils.contains((Object[]) value, cellValue)) {
                        if (j > maxMatchingIndex) {
                            maxMatchingIndex = j;
                        }
                    } else {
                        break;
                    }
                }
            }

            for (int i = maxMatchingIndex + 1; i < fieldCount; i++) {
                values[i] = null;
            }
        }
    }

    private boolean matchLabel(String cellLabel, String labelSearch) {
        if (labelSearch == null || labelSearch.length() == 0) {
            return true;
        }
        cellLabel = cellLabel.toLowerCase();
        labelSearch = labelSearch.toLowerCase();
        String[] cellLabelArray =
                StringUtils.split(cellLabel, NON_WORD_CHARACTERS);
        String[] searchLabelArray =
                StringUtils.split(labelSearch, NON_WORD_CHARACTERS);
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

}
