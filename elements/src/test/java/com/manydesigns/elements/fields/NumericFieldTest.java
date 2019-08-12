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
import com.manydesigns.elements.annotations.DecimalFormat;
import com.manydesigns.elements.annotations.PrecisionScale;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.testng.Assert.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Test
public class NumericFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2019, ManyDesigns srl";

    public BigDecimal myDecimal;
    @DecimalFormat("'aaa'#,##0.00'bbb'")
    public BigDecimal myFormattedDecimal;
    @DecimalFormat(value = "'aaa'#,##0.00'bbb'", multiplier = 10)
    @PrecisionScale(precision = 4, scale = 1)
    public BigDecimal myFormattedDecimalWithModifiers;
    private NumericField decimalField;

    @Override
    @BeforeMethod
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
        assertEquals(
                text,
                "<div class=\"form-group readwrite no-value\"><label for=\"myDecimal\" class=\"control-label\">My decimal</label><div><input id=\"myDecimal\" type=\"text\" name=\"myDecimal\" class=\"form-control\" /></div></div>");

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
        assertEquals(
                text,
                "<div class=\"form-group readwrite\"><label for=\"myDecimal\" class=\"control-label\">My decimal</label><div><input id=\"myDecimal\" type=\"text\" name=\"myDecimal\" value=\"10.02\" class=\"form-control\" /></div></div>");
    }


    public void testWrongValue() {
        decimalField.setStringValue("10g.0f2");
        String text = Util.elementToString(decimalField);
        assertEquals(
                "<div class=\"form-group readwrite\"><label for=\"myDecimal\" class=\"control-label\">My decimal</label><div><input id=\"myDecimal\" type=\"text\" name=\"myDecimal\" value=\"10g.0f2\" class=\"form-control\" /></div></div>",
                text);
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

    public void testFormatted() throws NoSuchFieldException {
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(this.getClass());
        PropertyAccessor myPropertyAccessor = classAccessor.getProperty("myFormattedDecimal");
        decimalField = new NumericField(myPropertyAccessor, Mode.EDIT, null);
        decimalField.setStringValue("aaa1,234.5bbb");
        assertEquals(decimalField.getValue(), new BigDecimal("1234"));
        assertTrue(decimalField.validate());
        decimalField.setStringValue("aaa1.234,5bbb");
        assertFalse(decimalField.validate());

        req.locales.remove(0); //Leave only Italian locale
        decimalField = new NumericField(myPropertyAccessor, Mode.EDIT, null);
        decimalField.setStringValue("aaa1,234.5bbb");
        assertFalse(decimalField.validate());
        decimalField.setStringValue("aaa1.234,5bbb");
        assertTrue(decimalField.validate());
        assertEquals(decimalField.getValue(), new BigDecimal("1234"));
    }

    public void testFormattedWithModifiers() throws NoSuchFieldException {
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(this.getClass());
        PropertyAccessor myPropertyAccessor = classAccessor.getProperty("myFormattedDecimalWithModifiers");
        decimalField = new NumericField(myPropertyAccessor, Mode.EDIT, null);
        decimalField.setStringValue("aaa1,234.5bbb");
        assertTrue(decimalField.validate());
        assertEquals(decimalField.getValue(), new BigDecimal("123.4"));
        decimalField.setStringValue("aaa1.234,5bbb");
        assertFalse(decimalField.validate());
        decimalField.setStringValue("aaa1,234,567.8bbb"); //Out of range, max is 10^precision
        assertFalse(decimalField.validate());

        req.locales.remove(0); //Leave only Italian locale
        decimalField = new NumericField(myPropertyAccessor, Mode.EDIT, null);
        decimalField.setStringValue("aaa1,234.5bbb");
        assertFalse(decimalField.validate());
        decimalField.setStringValue("aaa1.234,5bbb");
        assertTrue(decimalField.validate());
        assertEquals(decimalField.getValue(), new BigDecimal("123.4"));
    }

}
