package com.manydesigns.elements.fields;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;

import java.math.BigDecimal;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class DecimalFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public BigDecimal myDecimal;
    private NumericField decimalField;



    @Override
    protected void setUp() throws Exception {
        super.setUp();

        myDecimal = null;

        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(this.getClass());
        PropertyAccessor myPropertyAccessor =
                classAccessor.getProperty("myDecimal");
        decimalField = new NumericField(myPropertyAccessor);

    }

    public void testSimple() {
        String text = elementToString(decimalField);
        assertEquals("<th><label for=\"myDecimal\" class=\"field\">" +
                "My decimal:</label></th><td><input type=\"text\" class=\"text\"" +
                " id=\"myDecimal\" name=\"myDecimal\"></input></td>", text);

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
        String text = elementToString(decimalField);
        assertEquals("<th><label for=\"myDecimal\" class=\"field\">" +
                "My decimal:</label></th><td><input type=\"text\" class=\"text\"" +
                " id=\"myDecimal\" name=\"myDecimal\" value=\"10.02\"></input></td>", text);
    }


    public void testWrongValue() {
        decimalField.setStringValue("10g.0f2");
        String text = elementToString(decimalField);
        assertEquals("<th><label for=\"myDecimal\" class=\"field\">" +
                "My decimal:</label></th><td><input type=\"text\" class=\"text\"" +
                " id=\"myDecimal\" name=\"myDecimal\" value=\"10g.0f2\"></input></td>", text);
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