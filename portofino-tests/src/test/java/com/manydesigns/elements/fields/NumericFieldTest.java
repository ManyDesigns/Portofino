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
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;

import java.math.BigDecimal;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class NumericFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public BigDecimal myDecimal;
    private NumericField decimalField;



    @Override
    public void setUp() throws Exception {
        super.setUp();

        myDecimal = null;

        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(this.getClass());
        PropertyAccessor myPropertyAccessor =
                classAccessor.getProperty("myDecimal");
        decimalField = new NumericField(myPropertyAccessor, Mode.EDIT, null);
    }

    public void testSimple() {
        String text = Util.elementToString(decimalField);
        assertEquals("<th><label for=\"myDecimal\" class=\"mde-field-label\">" +
                "My decimal:</label></th><td><input id=\"myDecimal\" type=\"text\"" +
                " name=\"myDecimal\" class=\"mde-numeric-field\" /></td>", text);

        assertEquals(Mode.EDIT, decimalField.getMode());
        assertNull(decimalField.getStringValue());
        assertFalse(decimalField.isRequired());
        assertFalse(decimalField.isAutoCapitalize());
        assertNull(decimalField.getHelp());
        assertEquals("myDecimal", decimalField.getId());
        assertEquals("myDecimal", decimalField.getInputName());
        assertNull(decimalField.getMaxLength());

    }

    public void testValue() {
        decimalField.setStringValue("10.02");
        String text = Util.elementToString(decimalField);
        assertEquals("<th><label for=\"myDecimal\" class=\"mde-field-label\">" +
                "My decimal:</label></th><td><input id=\"myDecimal\" type=\"text\"" +
                " name=\"myDecimal\" value=\"10.02\" class=\"mde-numeric-field\" /></td>", text);
    }


    public void testWrongValue() {
        decimalField.setStringValue("10g.0f2");
        String text = Util.elementToString(decimalField);
        assertEquals("<th><label for=\"myDecimal\" class=\"mde-field-label\">" +
                "My decimal:</label></th><td><input id=\"myDecimal\" type=\"text\"" +
                " name=\"myDecimal\" value=\"10g.0f2\" class=\"mde-numeric-field\" /></td>", text);
    }

    public void testWriteToObject() {
        assertNull(myDecimal);
        req.setParameter("myDecimal", "0.g21");
        decimalField.readFromRequest(req);
        assertFalse(decimalField.validate());
        assertEquals("0.g21", decimalField.getStringValue());
        assertEquals(1, decimalField.getErrors().size());
        assertEquals("Invalid decimal", decimalField.getErrors().get(0));
    }
}