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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.forms.FieldSet;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionModel;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Test
public class SelectFieldTest2 extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    Integer[][] valuesArray = {
            {1, 1},
            {1, 2},
            {1, 3},
            {2, 4},
            {2, null}
    };

    String[][] labelsArray = {
            {"paperino", "qui"},
            {"paperino", "quo"},
            {"paperino", "qua"},
            {"cip", "ciop"},
            {"cip", null}
    };

    DefaultSelectionProvider selectionProvider;

    Form form;
    SelectField selectField1;
    SelectField selectField2;
    SelectionModel selectionModel;

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();

        selectionProvider = new DefaultSelectionProvider("selectionProvider", 2);
        for(int i = 0; i < valuesArray.length; i++) {
            selectionProvider.appendRow(valuesArray[i], labelsArray[i], true);
        }

        form = new FormBuilder(Bean.class)
                .configSelectionProvider(selectionProvider, "p1", "p2")
                .build();
        FieldSet fieldSet = form.get(0);
        selectField1 = (SelectField) fieldSet.get(0);
        selectField2 = (SelectField) fieldSet.get(1);

        selectionModel = selectField1.getSelectionModel();
    }

    public void testSelectField1() {
        assertEquals(selectionModel, selectField1.getSelectionModel());
        assertEquals(0, selectField1.getSelectionModelIndex());
        
        assertEquals(selectionModel, selectField2.getSelectionModel());
        assertEquals(1, selectField2.getSelectionModelIndex());
    }

    public void testSelectField2() {
        Bean bean = new Bean(null, null);
        form.readFromObject(bean);
        assertFalse(form.validate());

        assertNull(selectField1.getValue());
        assertNull(selectField2.getValue());
    }

    public void testSelectField3() {
        Bean bean = new Bean(2, null);
        form.readFromObject(bean);
        assertFalse(form.validate());

        assertNotNull(selectField1.getValue());
        assertEquals(2, selectField1.getValue());
        assertNull(selectField2.getValue());
    }

    public void testSelectField3Req() {
        req.setParameter("p1", "1");
        form.readFromRequest(req);
        assertFalse(form.validate());

        assertNotNull(selectField1.getValue());
        assertEquals(1, selectField1.getValue());
        assertNull(selectField2.getValue());
        Map<Object, SelectionModel.Option> options0 = selectField1.getOptions();
        assertNotNull(options0);
        assertEquals(2, options0.size());
        assertEquals("paperino", options0.get(1).label);
        assertEquals("cip", options0.get(2).label);

        Map<Object, SelectionModel.Option> options1 = selectField2.getOptions();
        assertNotNull(options1);
        assertEquals(3, options1.size());
        assertEquals("qui", options1.get(1).label);
        assertEquals("quo", options1.get(2).label);
        assertEquals("qua", options1.get(3).label);
    }


    public void testSelectField4() {
        Bean bean = new Bean(null, 2);
        form.readFromObject(bean);
        assertFalse(form.validate());

        assertNull(selectField1.getValue());
        assertNull(selectField2.getValue());

        checkOptions1();
    }

    private void checkOptions1() {
        Map<Object, SelectionModel.Option> options0 = selectField1.getOptions();
        assertNotNull(options0);
        assertEquals(2, options0.size());
        assertEquals("paperino", options0.get(1).label);
        assertEquals("cip", options0.get(2).label);

        Map<Object, SelectionModel.Option> options1 = selectField2.getOptions();
        assertNotNull(options1);
        assertEquals(0, options1.size());
    }

    public void testSelectField5() {
        Bean bean = new Bean(3, 1);
        form.readFromObject(bean);
        assertFalse(form.validate());

        assertNull(selectField1.getValue());
        assertNull(selectField2.getValue());

        checkOptions1();
    }

    public void testSelectField6() {
        Bean bean = new Bean(1, 4);
        form.readFromObject(bean);
        assertFalse(form.validate());

        assertNotNull(selectField1.getValue());
        assertEquals(1, selectField1.getValue());
        assertNull(selectField2.getValue());

        checkOptions2();
    }

    private void checkOptions2() {
        Map<Object, SelectionModel.Option> options0 = selectField1.getOptions();
        assertNotNull(options0);
        assertEquals(2, options0.size());
        assertEquals("paperino", options0.get(1).label);
        assertEquals("cip", options0.get(2).label);

        Map<Object, SelectionModel.Option> options1 = selectField2.getOptions();
        assertNotNull(options1);
        assertEquals(3, options1.size());
        assertEquals("qui", options1.get(1).label);
        assertEquals("quo", options1.get(2).label);
        assertEquals("qua", options1.get(3).label);
    }

    public void testSelectField7() {
        Bean bean = new Bean(1, 3);
        form.readFromObject(bean);
        assertTrue(form.validate());

        assertEquals(1, selectField1.getValue());
        assertEquals(3, selectField2.getValue());

        checkOptions2();
    }

    public void testNullInSecondField() {
        Bean bean = new Bean(2, null);
        form.readFromObject(bean);
        assertFalse(form.validate());

        assertEquals(2, selectField1.getValue());
        assertNull(selectField2.getValue());

        Map<Object, SelectionModel.Option> options0 = selectField1.getOptions();
        assertNotNull(options0);
        assertEquals(2, options0.size());
        assertEquals("paperino", options0.get(1).label);
        assertEquals("cip", options0.get(2).label);

        Map<Object, SelectionModel.Option> options1 = selectField2.getOptions();
        assertNotNull(options1);
        assertEquals(1, options1.size());
        assertEquals("ciop", options1.get(4).label);
    }

    public static class Bean {
        @Required
        public Integer p1;

        @Required
        public Integer p2;

        public Bean(Integer p1, Integer p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }
}
