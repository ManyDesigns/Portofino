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
            "Copyright (c) 2005-2011, ManyDesigns srl";

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
                " name=\"myDecimal\" class=\"text\" /></td>", text);

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
                " name=\"myDecimal\" value=\"10.02\" class=\"text\" /></td>", text);
    }


    public void testWrongValue() {
        decimalField.setStringValue("10g.0f2");
        String text = Util.elementToString(decimalField);
        assertEquals("<th><label for=\"myDecimal\" class=\"mde-field-label\">" +
                "My decimal:</label></th><td><input id=\"myDecimal\" type=\"text\"" +
                " name=\"myDecimal\" value=\"10g.0f2\" class=\"text\" /></td>", text);
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