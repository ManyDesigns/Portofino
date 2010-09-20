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

import com.manydesigns.elements.forms.FieldSet;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import junit.framework.TestCase;

import java.util.Collection;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class SelectFieldTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    Object[][] valuesArray = {
            {1, 1},
            {1, 2},
            {1, 3},
            {2, 4}
    };

    String[][] labelsArray = {
            {"paperino", "qui"},
            {"paperino", "quo"},
            {"paperino", "qua"},
            {"cip", "ciop"}
    };

    ArrayOptionProvider optionProvider;

    Form form;
    SelectField selectField1;
    SelectField selectField2;

    public void setUp() {
        optionProvider = new ArrayOptionProvider(2, valuesArray, labelsArray);

        form = new FormBuilder(Bean.class)
                .configOptionProvider(optionProvider, "p1", "p2")
                .build();
        FieldSet fieldSet = form.get(0);
        selectField1 = (SelectField) fieldSet.get(0);
        selectField2 = (SelectField) fieldSet.get(1);
    }

    public void testSelectField1() {
        Collection<OptionProvider> formOptionProviders =
                form.getOptionProviders();
        assertEquals(1, formOptionProviders.size());
        assertTrue(formOptionProviders.contains(optionProvider));
        
        assertEquals(optionProvider, selectField1.getOptionProvider());
        assertEquals(0, selectField1.getOptionProviderIndex());

        assertEquals(optionProvider, selectField2.getOptionProvider());
        assertEquals(1, selectField2.getOptionProviderIndex());
    }

    public void testSelectField2() {
        Bean bean = new Bean(null, null);
        form.readFromObject(bean);
        assertTrue(form.validate());

        assertNull(optionProvider.getValue(0));
        assertNull(optionProvider.getValue(1));
    }

    public void testSelectField3() {
        Bean bean = new Bean(1, null);
        form.readFromObject(bean);
        assertTrue(form.validate());

        assertNotNull(optionProvider.getValue(0));
        assertEquals(1, optionProvider.getValue(0));
        assertNull(optionProvider.getValue(1));
    }


    public void testSelectField4() {
        Bean bean = new Bean(null, 1);
        form.readFromObject(bean);
        assertFalse(form.validate());

        assertNull(optionProvider.getValue(0));
        assertNull(optionProvider.getValue(1));
    }

    public static class Bean {
        public Integer p1;
        public Integer p2;

        public Bean(Integer p1, Integer p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }
}
