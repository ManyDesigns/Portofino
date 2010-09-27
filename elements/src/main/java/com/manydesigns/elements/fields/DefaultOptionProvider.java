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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class DefaultOptionProvider implements OptionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Object[] values;
    protected final Object[][] valuesArray;
    protected final String[][] labelsArray;
    protected final Map<Object, String>[] optionsArray;
    protected boolean needsValidation;

    public final static Logger logger =
            LogUtil.getLogger(DefaultOptionProvider.class);

    //**************************************************************************
    // Static builders
    //**************************************************************************

    public static DefaultOptionProvider create(int fieldCount,
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

        return create(fieldCount, valuesArray, labelsArray);
    }


    public static DefaultOptionProvider create(int fieldCount,
                                               Object[][] valuesArray,
                                               String[][] labelsArray) {
        return new DefaultOptionProvider(fieldCount, valuesArray, labelsArray);
    }

    public static DefaultOptionProvider create(Collection<Object> objects,
                                               ClassAccessor classAccessor) {
        PropertyAccessor[] keyProperties = classAccessor.getKeyProperties();
        return create(objects, keyProperties);
    }

    protected static DefaultOptionProvider create(Collection<Object> objects,
                                                  Class objectClass,
                                                  String... propertyNames) {
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
                LogUtil.warning(logger, msg, e);
                throw new IllegalArgumentException(msg, e);
            }
        }
        return create(objects, propertyAccessors);
    }

    protected static DefaultOptionProvider create(
            Collection<Object> objects,
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
                try {
                    Object value = property.get(current);
                    String label =
                            (String) Util.convertValue(value, String.class);
                    values[j] = value;
                    labels[j] = label;
                } catch (Throwable e) {
                    String msg = MessageFormat.format(
                            "Could not access property: {0}",
                            property.getName());
                    LogUtil.warning(logger, msg, e);
                    throw new IllegalArgumentException(msg, e);
                }
                j++;
            }
            valuesArray[i] = values;
            labelsArray[i] = labels;
            i++;
        }
        return new DefaultOptionProvider(fieldsCount, valuesArray, labelsArray);
    }

    //**************************************************************************
    // Constructor
    //**************************************************************************

    protected DefaultOptionProvider(int fieldCount,
                                 Object[][] valuesArray,
                                 String[][] labelsArray) {
        this.valuesArray = valuesArray;
        this.labelsArray = labelsArray;
        values = new Object[fieldCount];
        //noinspection unchecked
        optionsArray = new Map[fieldCount];
        for (int i = 0; i < fieldCount; i++) {
            optionsArray[i] = new LinkedHashMap<Object, String>();
        }
        needsValidation = true;
    }


    //**************************************************************************
    // OptionProvider implementation
    //**************************************************************************

    public int getFieldCount() {
        return values.length;
    }

    public void setValue(int index, Object value) {
        values[index] = value;
        needsValidation = true;
    }

    public Object getValue(int index) {
        validate();
        return values[index];
    }

    public Map<Object, String> getOptions(int index) {
        validate();
        return optionsArray[index];
    }

    //**************************************************************************
    // inetrnal-use methods
    //**************************************************************************


    protected void validate() {
        if (!needsValidation) {
            return;
        }

        needsValidation = false;
        resetOptionsArray();

        // normalize null in values (only null values after first null)
        boolean foundNull = false;
        for (int j = 0; j < getFieldCount(); j++) {
            if (foundNull) {
                values[j] = null;
            } else if (values[j] == null) {
                foundNull = true;
            }
        }

        int maxMatchingIndex = -1;
        for (int i = 0; i < valuesArray.length; i++) {
            Object[] currentValueRow = valuesArray[i];
            String[] currentLabelRow = labelsArray[i];
            boolean matching = true;
            for (int j = 0; j < getFieldCount(); j++) {
                Object cellValue = currentValueRow[j];
                String cellLabel = currentLabelRow[j];
                Object value = values[j];

                Map<Object, String> options = optionsArray[j];
                if (matching) {
                    options.put(cellValue, cellLabel);
                }

                if (matching && value != null && value.equals(cellValue)) {
                    if (j > maxMatchingIndex) {
                        maxMatchingIndex = j;
                    }
                } else {
                    matching = false;
                }
            }
        }

        for (int i = maxMatchingIndex + 1; i < getFieldCount(); i++) {
            values[i] = null;
        }
    }

    public void resetOptionsArray() {
        for (int i = 0; i < values.length; i++) {
            optionsArray[i].clear();
        }
    }
}
