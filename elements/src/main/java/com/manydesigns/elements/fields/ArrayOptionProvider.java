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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ArrayOptionProvider implements OptionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final int fieldCount;
    protected final Object[][] valuesArray;
    protected final String[][] labelsArray;
    protected final Object[] values;
    protected final List<Integer> matchingRowIndexes;

    public final static Logger logger =
            LogUtil.getLogger(ArrayOptionProvider.class);


    //**************************************************************************
    // Constructor
    //**************************************************************************

    public ArrayOptionProvider(int fieldCount,
                               Object[][] valuesArray,
                               String[][] labelsArray) {
        this.fieldCount = fieldCount;
        this.valuesArray = valuesArray;
        this.labelsArray = labelsArray;
        values = new Object[fieldCount];
        matchingRowIndexes = new ArrayList<Integer>();
        resetValues();
    }


    //**************************************************************************
    // OptionProvider implementation
    //**************************************************************************

    public int getFieldCount() {
        return values.length;
    }

    public void resetValues() {
        for (int i = 0; i < values.length; i++) {
            values[i] = null;
        }
        for (int i = 0; i < valuesArray.length; i++) {
            matchingRowIndexes.add(i);
        }
    }

    public void setValue(int index, Object value) {
        values[index] = value;
    }

    public Object getValue(int index) {
        return values[index];
    }

    public boolean validate() {
        boolean valid = true;
        matchingRowIndexes.clear();
        boolean foundNull = false;
        for (int j = 0; j < fieldCount; j++) {
            Object value = values[j];
            if (value == null) {
                foundNull = true;
            } else if (foundNull) {
                valid = false;
            }
        }

        if (!valid) {
            resetValues();
            return false;
        }

        for (int i = 0; i < valuesArray.length; i++) {
            Object[] currentRow = valuesArray[i];
            boolean matches = true;
            for (int j = 0; j < fieldCount; j++) {
                Object a = values[j];
                Object b = currentRow[j];
                if (a != null && !a.equals(b)) {
                    matches = false;
                }
            }
            if (matches) {
                matchingRowIndexes.add(i);
            }
        }
        if (matchingRowIndexes.size() == 0 && valuesArray.length > 0) {
            resetValues();
            return false;
        }
        return true;
    }

    public String getLabel(int index) {
        if (matchingRowIndexes.size() == 0) {
            return null;
        }
        int i = matchingRowIndexes.get(0);
        String[] row = labelsArray[i];
        return row[index];
    }

    public Map<Object, String> getOptions(int index) {
        Map<Object, String> result = new HashMap<Object, String>();
        for (int i : matchingRowIndexes) {
            Object value = valuesArray[i][index];
            String label = labelsArray[i][index];
            if (!result.containsKey(value)) {
                result.put(value, label);
            }
        }
        return result;
    }
}
