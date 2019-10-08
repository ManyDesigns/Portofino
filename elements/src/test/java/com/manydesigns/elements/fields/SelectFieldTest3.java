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
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionModel;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Test
public class SelectFieldTest3 extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    private SelectField selectField;

    private Long[] valuesArray = {
            1000L,
            2000L,
            3000L
    };
    private String[] labelsArray = {
            "label1",
            "label2",
            "label3"
    };

    protected DefaultSelectionProvider selectionProvider;
    protected SelectionModel selectionModel;

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();

        selectionProvider = new DefaultSelectionProvider("selectionProvider");
        for(int i = 0; i < valuesArray.length; i++) {
            selectionProvider.appendRow(valuesArray[i], labelsArray[i], true);
        }
        selectionModel = selectionProvider.createSelectionModel();
    }

    private void setupSelectFields(Mode mode) {
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(SelectBean3.class);
        PropertyAccessor myPropertyAccessor =
                null;
        try {
            myPropertyAccessor = classAccessor.getProperty("longValue");
        } catch (NoSuchFieldException e) {
            fail(e.getMessage(), e);
        }

        // impostiamo selectField1
        selectField = new SelectField(myPropertyAccessor, mode, null);
        selectField.setSelectionModel(selectionModel);
    }

    public void testSimpleRadio() {
        setupSelectFields(Mode.EDIT);

        String text = Util.elementToString(selectField);
        assertEquals(
                "<div class=\"form-group readwrite no-value required\"><label for=\"longValue\" class=\"control-label\">Long value</label><div><div class=\"radio\"><input type=\"radio\" id=\"longValue_0\" name=\"longValue\" value=\"1000\" /><label for=\"longValue_0\">label1</label></div><div class=\"radio\"><input type=\"radio\" id=\"longValue_1\" name=\"longValue\" value=\"2000\" /><label for=\"longValue_1\">label2</label></div><div class=\"radio\"><input type=\"radio\" id=\"longValue_2\" name=\"longValue\" value=\"3000\" /><label for=\"longValue_2\">label3</label></div></div></div>",
                text);

        assertFalse(selectField.validate());
        req.setParameter("longValue", "1000");
        selectField.readFromRequest(req);
        assertEquals(1000L, selectField.getValue());
        assertTrue(selectField.validate());
        assertEquals(1000L, selectField.getValue());
    }
}
