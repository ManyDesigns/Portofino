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

package com.manydesigns.elements.options;

import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.TextFormat;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class DefaultSelectionProvider implements SelectionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final String name;
    protected final int fieldCount;
    protected final Object[][] valuesArray;
    protected final String[][] labelsArray;

    public final static Logger logger =
            LoggerFactory.getLogger(DefaultSelectionProvider.class);
    public static final String NON_WORD_CHARACTERS =
            " \t\n\f\r\\||!\"£$%&/()='?^[]+*@#<>,;.:-_";

    //**************************************************************************
    // Static builders
    //**************************************************************************

    public static DefaultSelectionProvider create(String name,
                                                  Object[] values,
                                                  String[] labels) {
        Object[][] valuesArray = new Object[values.length][];
        for (int i = 0; i < values.length; i++) {
            valuesArray[i] = new Object[1];
            valuesArray[i][0] = values[i];
        }

        String[][] labelsArray = new String[labels.length][];
        for (int i = 0; i < labels.length; i++) {
            labelsArray[i] = new String[1];
            labelsArray[i][0] = labels[i];
        }

        return create(name, 1, valuesArray, labelsArray);
    }

    public static DefaultSelectionProvider create(String name,
                                               int fieldCount,
                                               Class[] valueTypes,
                                               Collection<Object[]> valuesAndLabels) {
        int size = valuesAndLabels.size();
        Object[][] valuesArray = new Object[size][];
        String[][] labelsArray = new String[size][];
        int i = 0;
        for (Object[] valueAndLabel : valuesAndLabels) {
            Object[] values = new Object[fieldCount];
            String[] labels = new String[fieldCount];
            valuesArray[i] = values;
            labelsArray[i] = labels;

            for (int j = 0; j < fieldCount; j++) {
                Class valueType = valueTypes[j];
                values[j] = OgnlUtils.convertValue(valueAndLabel[j*2], valueType);
                labels[j] = OgnlUtils.convertValueToString(valueAndLabel[j*2+1]);
            }

            i++;
        }


        return create(name, fieldCount, valuesArray, labelsArray);
    }


    public static DefaultSelectionProvider create(String name,
                                               int fieldCount,
                                               Object[][] valuesArray,
                                               String[][] labelsArray) {
        return new DefaultSelectionProvider(name, fieldCount,
                valuesArray, labelsArray);
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
                                                  TextFormat textFormat,
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
        Object[][] valuesArray = new Object[objects.size()][fieldsCount];
        String[][] labelsArray = new String[objects.size()][fieldsCount];
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
            valuesArray[i] = values;
            labelsArray[i] = labels;
            i++;
        }
        return new DefaultSelectionProvider(
                name, fieldsCount, valuesArray, labelsArray);
    }

    //**************************************************************************
    // Constructor
    //**************************************************************************

    protected DefaultSelectionProvider(String name,
                                    int fieldCount,
                                    Object[][] valuesArray,
                                    String[][] labelsArray) {
        this.name = name;
        this.fieldCount = fieldCount;
        this.valuesArray = valuesArray;
        this.labelsArray = labelsArray;
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

    //**************************************************************************
    // inner class
    //**************************************************************************

    class DefaultSelectionModel implements SelectionModel {
        public static final String copyright =
                "Copyright (c) 2005-2010, ManyDesigns srl";

        private final Object[] values;
        private final String[] labelSearches;
        private final Map<Object,String>[] optionsArray;

        private boolean needsValidation;

        public DefaultSelectionModel() {
            values = new Object[fieldCount];
            labelSearches = new String[fieldCount];
            //noinspection unchecked
            optionsArray = new Map[fieldCount];
            for (int i = 0; i < fieldCount; i++) {
                optionsArray[i] = new LinkedHashMap<Object, String>();
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

        public Map<Object, String> getOptions(int index) {
            validate();
            return optionsArray[index];
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
            for (int i = 0; i < valuesArray.length; i++) {
                Object[] currentValueRow = valuesArray[i];
                String[] currentLabelRow = labelsArray[i];
                boolean matching = true;
                for (int j = 0; j < fieldCount; j++) {
                    Object cellValue = currentValueRow[j];
                    String cellLabel = currentLabelRow[j];
                    Object value = values[j];
                    String labelSearch = labelSearches[j];

                    if (matching && matchLabel(cellLabel, labelSearch)) {
                        optionsArray[j].put(cellValue, cellLabel);
                    }

                    if (matching && value != null
                            && value.equals(cellValue)) {
                        if (j > maxMatchingIndex) {
                            maxMatchingIndex = j;
                        }
                    } else if (matching && value!=null &&
                            value instanceof Object[]
                            && ArrayUtils.contains((Object[]) value, cellValue)) {
                         if (j > maxMatchingIndex) {
                            maxMatchingIndex = j;
                         }
                    }
                    else {
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
}
