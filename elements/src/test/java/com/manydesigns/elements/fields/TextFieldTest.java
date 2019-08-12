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
import com.manydesigns.elements.annotations.HighlightLinks;
import com.manydesigns.elements.annotations.MaxLength;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Test
public class TextFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public String myText;

    @Multiline
    @HighlightLinks
    @MaxLength(5)
    public String annotatedText;

    private TextField textField;
    private TextField annotatedTextField;

    @Override
    @BeforeMethod
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
            fail("No such field", e);
        }
    }

    public void testSimple() {
        setupFields(Mode.EDIT);

        String text = Util.elementToString(textField);
        assertEquals(text,"<div class=\"form-group readwrite no-value\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<input id=\"myText\" type=\"text\" name=\"myText\" class=\"form-control\" />" +
                "</div></div>");

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
        assertNull(textField.getTextAreaWidth());
        assertEquals(4, textField.getTextAreaMinRows());
        assertNull(textField.getSize());
    }

    public void testValue() {
        setupFields(Mode.EDIT);

        textField.setStringValue("myValue");
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<input id=\"myText\" type=\"text\" name=\"myText\" value=\"myValue\" class=\"form-control\" />" +
                "</div></div>", text);
    }

    public void testRequired() {
        setupFields(Mode.EDIT);

        textField.setRequired(true);
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite no-value required\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<input id=\"myText\" type=\"text\" name=\"myText\" class=\"form-control\" />" +
                "</div></div>", text);
    }

    public void testModeView() {
        setupFields(Mode.VIEW);

        textField.setStringValue("www.manydesigns.com");
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readonly\">" +
                "<label class=\"control-label\">My text</label>" +
                "<div>" +
                "<div class=\"form-control-static\" id=\"myText\">www.manydesigns.com</div>" +
                "</div></div>", text);
    }

    public void testHighlightLinks() {
        setupFields(Mode.VIEW);

        textField.setRequired(true);
        textField.setStringValue(
                "www.manydesigns.com - http://www.google.com - info@manydesigns.com - " +
                "https://my.bank.com/account/123456?owner=me&sessionToken=abcdef - ftp://coolstuff.org/~a/b/c.pdf - " +
                "file:/foo - file:///bar");
        textField.setHighlightLinks(true);
        String text = Util.elementToString(textField);
        assertEquals(text,
                "<div class=\"form-group readonly\">" +
                "<label class=\"control-label\">My text</label>" +
                "<div><div class=\"form-control-static\" id=\"myText\">" +
                "<a href=\"http://www.manydesigns.com\">www.manydesigns.com</a> - " +
                "<a href=\"http://www.google.com\">http://www.google.com</a> - " +
                "<a href=\"mailto:info@manydesigns.com\">info@manydesigns.com</a> - " +
                "<a href=\"https://my.bank.com/account/123456?owner=me&amp;sessionToken=abcdef\">https://my.bank.com/ac...</a> - " +
                "<a href=\"ftp://coolstuff.org/~a/b/c.pdf\">ftp://coolstuff.org/~a...</a> - " +
                "<a href=\"file:/foo\">file:/foo</a> - " +
                "<a href=\"file:///bar\">file:///bar</a>" +
                "</div></div></div>");
    }

    public void testAutoCapitalize() {
        setupFields(Mode.EDIT);

        req.setParameter("myText", "myValue");
        textField.setAutoCapitalize(true);
        textField.readFromRequest(req);
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<input id=\"myText\" type=\"text\" name=\"myText\" value=\"MYVALUE\" class=\"form-control\" />" +
                "</div></div>", text);
    }

    public void testHelp() {
        setupFields(Mode.EDIT);

        textField.setHelp("myHelp");
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite no-value\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<input id=\"myText\" type=\"text\" name=\"myText\" class=\"form-control\" />" +
                "<span class=\"help-block\">myHelp</span>" +
                "</div></div>", text);
    }

    public void testHtmlId() {
        setupFields(Mode.EDIT);

        textField.setId("myId");
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite no-value\">" +
                "<label for=\"myId\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<input id=\"myId\" type=\"text\" name=\"myText\" class=\"form-control\" />" +
                "</div></div>", text);
    }

    public void testInputName() {
        setupFields(Mode.EDIT);

        textField.setInputName("myInput");
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite no-value\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<input id=\"myText\" type=\"text\" name=\"myInput\" class=\"form-control\" />" +
                "</div></div>", text);
    }

    public void testLabel() {
        setupFields(Mode.EDIT);

        textField.setLabel("myLabel");
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite no-value\">" +
                "<label for=\"myText\" class=\"control-label\">MyLabel</label>" +
                "<div>" +
                "<input id=\"myText\" type=\"text\" name=\"myText\" class=\"form-control\" />" +
                "</div></div>", text);
    }

    public void testValidateRequired() {
        setupFields(Mode.EDIT);

        textField.setRequired(true);
        textField.readFromRequest(req);
        assertNull(textField.getStringValue());
        assertFalse(textField.validate());
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite no-value has-error required\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<input id=\"myText\" type=\"text\" name=\"myText\" class=\"form-control\" />" +
                "<span class=\"help-block\">Required field<br /></span>" +
                "</div></div>", text);
    }

    public void testMaxLength() {
        setupFields(Mode.EDIT);

        textField.setMaxLength(3);
        req.setParameter("myText", "myValue");
        textField.readFromRequest(req);
        assertFalse(textField.validate());
        assertEquals("myValue", textField.getStringValue());
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite has-error\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<input id=\"myText\" type=\"text\" name=\"myText\" value=\"myValue\" class=\"form-control\" maxlength=\"3\" />" +
                "<span class=\"help-block\">Max permitted length is 3 characters<br /></span>" +
                "</div></div>", text);
    }

    public void testMaxLengthWithTrim() {
        setupFields(Mode.EDIT);

        textField.setMaxLength(3);
        req.setParameter("myText", " 123 ");
        textField.readFromRequest(req);
        textField.validate();
        assertEquals("123", textField.getStringValue());
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<input id=\"myText\" type=\"text\" name=\"myText\" value=\"123\" class=\"form-control\" maxlength=\"3\" />" +
                "</div></div>", text);
    }

    public void testMultilineEditDefaults() {
        setupFields(Mode.EDIT);

        textField.setMultiline(true);
        req.setParameter("myText", "myValue");
        textField.readFromRequest(req);
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<textarea id=\"myText\" name=\"myText\" rows=\"4\" class=\"form-control\">myValue</textarea>" +
                "</div></div>", text);
    }

    public void testMultilineEditArea() {
        setupFields(Mode.EDIT);

        textField.setMultiline(true);
        textField.setTextAreaMinRows(5);
        textField.setTextAreaWidth(60);
        req.setParameter("myText", "myValue");
        textField.readFromRequest(req);
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<textarea id=\"myText\" name=\"myText\" cols=\"60\" rows=\"5\" class=\"form-control mde-text-field-with-explicit-size\">myValue</textarea>" +
                "</div></div>", text);
    }

    public void testMultilineView() {
        setupFields(Mode.VIEW);

        textField.setMultiline(true);
        myText = "\tmy\nValue";
        textField.readFromObject(this);
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readonly\">" +
                "<label class=\"control-label\">My text</label>" +
                "<div>" +
                "<div class=\"form-control-static\" id=\"myText\">\tmy<br />Value</div>" +
                "</div></div>", text);
    }

    public void testReadFromNullObject() {
        setupFields(Mode.VIEW);

        textField.readFromObject(null);
        String text = Util.elementToString(textField);
        assertEquals(
                "<div class=\"form-group readonly no-value\"><label class=\"control-label\">My text</label><div><div class=\"form-control-static\" id=\"myText\"></div></div></div>",
                text);
    }

    public void testTextInputMaxLength() {
        setupFields(Mode.EDIT);

        textField.setMaxLength(100);
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite no-value\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<input id=\"myText\" type=\"text\" name=\"myText\" class=\"form-control\" maxlength=\"100\" />" +
                "</div></div>", text);
    }

    public void testTextInputMaxLength2() {
        setupFields(Mode.EDIT);

        textField.setMaxLength(100);
        textField.setSize(20);
        String text = Util.elementToString(textField);
        assertEquals("<div class=\"form-group readwrite no-value\">" +
                "<label for=\"myText\" class=\"control-label\">My text</label>" +
                "<div>" +
                "<input id=\"myText\" type=\"text\" name=\"myText\" size=\"20\" class=\"form-control mde-text-field-with-explicit-size\" maxlength=\"100\" />" +
                "</div></div>", text);
    }

    public void testAnnotations() throws NoSuchFieldException {
        setupFields(Mode.EDIT);

        assertTrue(annotatedTextField.isMultiline());
        assertTrue(annotatedTextField.isHighlightLinks());
        assertNotNull(annotatedTextField.getMaxLength());
        assertEquals(5, annotatedTextField.getMaxLength().intValue());
    }

    public void testWriteToObject() {
        setupFields(Mode.EDIT);

        assertNull(myText);
        textField.setStringValue("myValue");
        textField.writeToObject(this);
        assertEquals("myValue", myText);
    }
}
