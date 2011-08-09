package com.manydesigns.elements.fields;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.HighlightLinks;
import com.manydesigns.elements.annotations.MaxLength;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class TextFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public String myText;

    @Multiline
    @HighlightLinks
    @MaxLength(5)
    public String annotatedText;

    private TextField textField;
    private TextField annotatedTextField;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        myText = null;
        annotatedText = null;
    }

    private void setupFields(Mode mode) {
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(this.getClass());
        try {
            PropertyAccessor myPropertyAccessor =
                    classAccessor.getProperty("myText");
            textField = new TextField(myPropertyAccessor, mode, null);

            myPropertyAccessor =
                    classAccessor.getProperty("annotatedText");
            annotatedTextField = new TextField(myPropertyAccessor, mode, null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testSimple() {
        setupFields(Mode.EDIT);

        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><input id=\"myText\" type=\"text\" " +
                "name=\"myText\" class=\"text\" /></td>", text);

        assertEquals(Mode.EDIT, textField.getMode());
        assertNull(textField.getStringValue());
        assertFalse(textField.isRequired());
        assertFalse(textField.isHighlightLinks());
        assertFalse(textField.isAutoCapitalize());
        assertNull(textField.getHelp());
        assertEquals("myText", textField.getId());
        assertEquals("myText", textField.getInputName());
        assertNull(textField.getMaxLength());
        assertFalse(textField.isMultiline());
        assertEquals(70, textField.getTextAreaWidth());
        assertEquals(4, textField.getTextAreaMinRows());
        assertEquals(70, textField.getSize());
    }

    public void testValue() {
        setupFields(Mode.EDIT);

        textField.setStringValue("myValue");
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><input id=\"myText\" type=\"text\" " +
                "name=\"myText\" value=\"myValue\" class=\"text\" />" +
                "</td>", text);
    }

    public void testRequired() {
        setupFields(Mode.EDIT);

        textField.setRequired(true);
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">" +
                "<span class=\"required\">*</span>&nbsp;My text:" +
                "</label></th><td><input id=\"myText\" type=\"text\" " +
                "name=\"myText\" class=\"text\" />" +
                "</td>", text);
    }

    public void testModeView() {
        setupFields(Mode.VIEW);

        textField.setStringValue("www.manydesigns.com");
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><div class=\"value\" id=\"myText\">" +
                "www.manydesigns.com</div></td>", text);
    }

    public void testHighlightLinks() {
        setupFields(Mode.VIEW);

        textField.setRequired(true);
        textField.setStringValue("www.manydesigns.com - http://www.google.com - info@manydesigns.com");
        textField.setHighlightLinks(true);
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><div class=\"value\" id=\"myText\">" +
                "<a href=\"http://www.manydesigns.com\">www.manydesigns.com" +
                "</a> - <a href=\"http://www.google.com\">" +
                "http://www.google.com</a> - <a " +
                "href=\"mailto:info@manydesigns.com\">info@manydesigns.com</a>" +
                "</div></td>", text);
    }

    public void testAutoCapitalize() {
        setupFields(Mode.EDIT);

        req.setParameter("myText", "myValue");
        textField.setAutoCapitalize(true);
        textField.readFromRequest(req);
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><input id=\"myText\" type=\"text\" " +
                "name=\"myText\" value=\"MYVALUE\" class=\"text\" />" +
                "</td>", text);
    }

    public void testHelp() {
        setupFields(Mode.EDIT);

        textField.setHelp("myHelp");
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><input id=\"myText\" type=\"text\" " +
                "name=\"myText\" class=\"text\" />" +
                "<div class=\"inputdescription\">myHelp</div></td>", text);
    }

    public void testHtmlId() {
        setupFields(Mode.EDIT);

        textField.setId("myId");
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myId\" class=\"field\">My text:" +
                "</label></th><td><input id=\"myId\" type=\"text\" " +
                "name=\"myText\" class=\"text\" /></td>", text);
    }

    public void testInputName() {
        setupFields(Mode.EDIT);

        textField.setInputName("myInput");
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><input id=\"myText\" type=\"text\" " +
                "name=\"myInput\" class=\"text\" /></td>", text);
    }

    public void testLabel() {
        setupFields(Mode.EDIT);

        textField.setLabel("myLabel");
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">MyLabel:" +
                "</label></th><td><input id=\"myText\" type=\"text\" " +
                "name=\"myText\" class=\"text\" /></td>", text);
    }

    public void testValidateRequired() {
        setupFields(Mode.EDIT);

        textField.setRequired(true);
        textField.readFromRequest(req);
        assertNull(textField.getStringValue());
        assertFalse(textField.validate());
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">" +
                "<span class=\"required\">*</span>&nbsp;My text:" +
                "</label></th><td><input id=\"myText\" type=\"text\" " +
                "name=\"myText\" class=\"text\" />" +
                "<ul class=\"errors\">" +
                "<li>Required field" +
                "</li></ul></td>", text);
    }

    public void testMaxLength() {
        setupFields(Mode.EDIT);

        textField.setMaxLength(3);
        req.setParameter("myText", "myValue");
        textField.readFromRequest(req);
        assertFalse(textField.validate());
        assertEquals("myValue", textField.getStringValue());
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><input id=\"myText\" type=\"text\" " +
                "name=\"myText\" value=\"myValue\" class=\"text\" " +
                "size=\"3\" maxlength=\"3\" /><ul class=\"errors\">" +
                "<li>Max permitted length is 3 characters" +
                "</li></ul></td>", text);
    }

    public void testMaxLengthWithTrim() {
        setupFields(Mode.EDIT);

        textField.setMaxLength(3);
        req.setParameter("myText", " 123 ");
        textField.readFromRequest(req);
        textField.validate();
        assertEquals("123", textField.getStringValue());
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><input id=\"myText\" type=\"text\" " +
                "name=\"myText\" value=\"123\" class=\"text\" " +
                "size=\"3\" maxlength=\"3\" /></td>", text);
    }

    public void testMultilineEditDefaults() {
        setupFields(Mode.EDIT);

        textField.setMultiline(true);
        req.setParameter("myText", "myValue");
        textField.readFromRequest(req);
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><textarea id=\"myText\" name=\"myText\" " +
                "cols=\"70\" rows=\"4\">myValue" +
                "</textarea></td>", text);
    }

    public void testMultilineEditArea() {
        setupFields(Mode.EDIT);

        textField.setMultiline(true);
        textField.setTextAreaMinRows(5);
        textField.setTextAreaWidth(60);
        req.setParameter("myText", "myValue");
        textField.readFromRequest(req);
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><textarea id=\"myText\" name=\"myText\" " +
                "cols=\"60\" rows=\"5\">myValue" +
                "</textarea></td>", text);
    }

    public void testMultilineView() {
        setupFields(Mode.VIEW);

        textField.setMultiline(true);
        myText = "\tmy\nValue";
        textField.readFromObject(this);
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><div class=\"value\" id=\"myText\">" +
                "&nbsp;&nbsp;&nbsp;&nbsp;my<br />Value" +
                "</div></td>", text);
    }

    public void testReadFromNullObject() {
        setupFields(Mode.VIEW);

        textField.readFromObject(null);
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><div class=\"value\" id=\"myText\">" +
                "</div></td>", text);
    }

    public void testTextInputMaxLength() {
        setupFields(Mode.EDIT);

        textField.setMaxLength(100);
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><input id=\"myText\" type=\"text\" " +
                "name=\"myText\" class=\"text\" size=\"70\" maxlength=\"100\" />" +
                "</td>", text);
    }

    public void testTextInputMaxLength2() {
        setupFields(Mode.EDIT);

        textField.setMaxLength(100);
        textField.setSize(20);
        String text = elementToString(textField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><input id=\"myText\" type=\"text\" " +
                "name=\"myText\" class=\"text\" size=\"20\" maxlength=\"100\" />" +
                "</td>", text);
    }

    public void testAnnotations() throws NoSuchFieldException {
        setupFields(Mode.EDIT);

        assertTrue(annotatedTextField.isMultiline());
        assertTrue(annotatedTextField.isHighlightLinks());
        assertNotNull(annotatedTextField.getMaxLength());
        assertEquals(5, annotatedTextField.getMaxLength().intValue());
    }

    public void testAdjustTextNull() {
        assertNull(TextField.adjustText(null));
    }

    public void testHighlightLinksNull() {
        assertNull(TextField.highlightLinks(null));
    }

    public void testWriteToObject() {
        setupFields(Mode.EDIT);

        assertNull(myText);
        textField.setStringValue("myValue");
        textField.writeToObject(this);
        assertEquals("myValue", myText);
    }
}
