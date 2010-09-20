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

import junit.framework.TestCase;

import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ArrayOptionProviderTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    Object[][] valuesArray = {
            {1},
            {2},
            {3}
    };

    String[][] labelsArray = {
            {"qui"},
            {"quo"},
            {"qua"}
    };

    ArrayOptionProvider optionProvider;

    public void setUp() {
        optionProvider = new ArrayOptionProvider(1, valuesArray, labelsArray);
    }

    public void testArrayOptionsProvider1() {
        assertEquals(1, optionProvider.getFieldCount());
    }

    public void testArrayOptionsProvider2() {
        assertNull(optionProvider.getValue(0));
        assertTrue(optionProvider.validate());
        Map<Object, String> options1 = optionProvider.getOptions(0);
        checkAllPresent(options1);
    }


    public void testArrayOptionsProvider3() {
        optionProvider.setValue(0, 2);
        Object value = optionProvider.getValue(0);
        assertNotNull(value);
        assertEquals(2, value);

        assertTrue(optionProvider.validate());

        String label = optionProvider.getLabel(0);
        assertNotNull(label);
        assertEquals("quo", label);

        Map<Object, String> options1 = optionProvider.getOptions(0);
        assertEquals(1, options1.size());
        assertTrue(options1.containsKey(2));
        assertEquals("quo", options1.get(2));
    }

    public void testArrayOptionsProvider4() {
        optionProvider.setValue(0, 4);
        Object value = optionProvider.getValue(0);
        assertNotNull(value);
        assertEquals(4, value);

        assertFalse(optionProvider.validate());

        Map<Object, String> options1 = optionProvider.getOptions(0);
        checkAllPresent(options1);
    }

    private void checkAllPresent(Map<Object, String> options1) {
        assertEquals(3, options1.size());
        assertTrue(options1.containsKey(1));
        assertTrue(options1.containsKey(2));
        assertTrue(options1.containsKey(3));
        assertEquals("qui", options1.get(1));
        assertEquals("quo", options1.get(2));
        assertEquals("qua", options1.get(3));
    }
}
