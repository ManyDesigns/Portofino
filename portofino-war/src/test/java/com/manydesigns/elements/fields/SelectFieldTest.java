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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.forms.FieldSet;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.text.BasicTextProvider;
import junit.framework.TestCase;

import java.util.Locale;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class SelectFieldTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    String[][] valuesArray = {
            {"paperino", "qui"},
            {"paperino", "quo"},
            {"paperino", "qua"},
            {"cip", "ciop"}
    };

    DefaultOptionProvider optionProvider;

    Form form;
    SelectField selectField1;
    SelectField selectField2;

    public void setUp() {
        ElementsThreadLocals.setTextProvider(new BasicTextProvider(Locale.ENGLISH));

        optionProvider = new DefaultOptionProvider(2, valuesArray);

        form = new FormBuilder(Bean.class)
                .configOptionProvider(optionProvider, "p1", "p2")
                .build();
        FieldSet fieldSet = form.get(0);
        selectField1 = (SelectField) fieldSet.get(0);
        selectField2 = (SelectField) fieldSet.get(1);
    }

    public void testSelectField1() {
        assertEquals(optionProvider, selectField1.getOptionProvider());
        assertEquals(0, selectField1.getOptionProviderIndex());

        assertEquals(optionProvider, selectField2.getOptionProvider());
        assertEquals(1, selectField2.getOptionProviderIndex());
    }

    public void testSelectField2() {
        Bean bean = new Bean(null, null);
        form.readFromObject(bean);
        assertFalse(form.validate());

        assertNull(optionProvider.getValue(0));
        assertNull(optionProvider.getValue(1));
    }

    public void testSelectField3() {
        Bean bean = new Bean("cip", null);
        form.readFromObject(bean);
        assertFalse(form.validate());

        assertNotNull(optionProvider.getValue(0));
        assertEquals("cip", optionProvider.getValue(0));
        assertNull(optionProvider.getValue(1));
    }


    public void testSelectField4() {
        Bean bean = new Bean(null, "quo");
        form.readFromObject(bean);
        assertFalse(form.validate());

        assertNull(optionProvider.getValue(0));
        assertNull(optionProvider.getValue(1));
    }

    public void testSelectField5() {
        Bean bean = new Bean("bla", "qui");
        form.readFromObject(bean);
        assertFalse(form.validate());

        assertNull(optionProvider.getValue(0));
        assertNull(optionProvider.getValue(1));
    }

    public void testSelectField6() {
        Bean bean = new Bean("paperino", "ciop");
        form.readFromObject(bean);
        assertFalse(form.validate());

        assertEquals(null, optionProvider.getValue(0));
        assertEquals(null, optionProvider.getValue(1));
    }

    public void testSelectField7() {
        Bean bean = new Bean("paperino", "qua");
        form.readFromObject(bean);
        assertTrue(form.validate());

        assertEquals("paperino", optionProvider.getValue(0));
        assertEquals("qua", optionProvider.getValue(1));
    }

    public static class Bean {
        @Required
        public String p1;

        @Required
        public String p2;

        public Bean(String p1, String p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }
}
