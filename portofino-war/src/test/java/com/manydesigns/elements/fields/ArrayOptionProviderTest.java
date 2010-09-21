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

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ArrayOptionProviderTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    String[][] valuesArray = {
            {"qui"},
            {"quo"},
            {"qua"}
    };

    DefaultOptionProvider optionProvider;

    public void setUp() {
        optionProvider = new DefaultOptionProvider(1, valuesArray);
    }

    public void testArrayOptionsProvider1() {
        assertEquals(1, optionProvider.getFieldCount());
    }

    public void testArrayOptionsProvider2() {
        assertNull(optionProvider.getValue(0));
        checkAllPresent();
    }


    public void testArrayOptionsProvider3() {
        optionProvider.setValue(0, "qua");
        Object value = optionProvider.getValue(0);
        assertNotNull(value);
        assertEquals("qua", value);

        checkAllPresent();
    }

    public void testArrayOptionsProvider4() {
        optionProvider.setValue(0, "pippo");
        Object value = optionProvider.getValue(0);
        assertNull(value);

        checkAllPresent();
    }

    private void checkAllPresent() {
        List<Object> options = optionProvider.getOptions(0);
        assertEquals(3, options.size());
        assertTrue(options.contains("qui"));
        assertTrue(options.contains("quo"));
        assertTrue(options.contains("qua"));
    }
}
