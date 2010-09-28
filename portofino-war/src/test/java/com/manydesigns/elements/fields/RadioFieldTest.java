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

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class RadioFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public String myText;

    private RadioField radioField;
    private RadioField radioField2;

    private String[][] valuesArray = {
            {"value1"},
            {"value2"},
            {"value3"}
    };
    private String[][] labelsArray = {
            {"label1"},
            {"label2"},
            {"label3"}
    };

    private String[][] valuesArray2 = {
            {"value1"}
    };
    private String[][] labelsArray2 = {
            {"label1"}
    };

    private OptionProvider optionProvider;
    private OptionProvider optionProvider2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        myText = null;

        // impostiamo selectField
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(RadioFieldTest.class);
        PropertyAccessor myPropertyAccessor =
                classAccessor.getProperty("myText");
        radioField = new RadioField(myPropertyAccessor);

        optionProvider = DefaultOptionProvider.create(
                "optionProvider", 1, valuesArray, labelsArray);
        radioField.setOptionProvider(optionProvider);

        // impostiamo selectField2
        radioField2 = new RadioField(myPropertyAccessor);

        optionProvider2 = DefaultOptionProvider.create(
                "optionProvider2", 1, valuesArray2, labelsArray2);
        radioField2.setOptionProvider(optionProvider2);
    }

    public void testSimple() {
        assertEquals(optionProvider, radioField.getOptionProvider());
        assertEquals(optionProvider2, radioField2.getOptionProvider());
        assertNull(radioField.getStringValue());
        assertNotNull(radioField.getErrors());
        assertEquals(0, radioField.getErrors().size());
        assertNull(radioField.getHelp());
        assertEquals("myText", radioField.getId());
        assertEquals("myText", radioField.getInputName());
        assertEquals("My text", radioField.getLabel());
        assertEquals(Mode.EDIT, radioField.getMode());
        assertFalse(radioField.isRequired());
        assertEquals(3, optionProvider.getOptions(0).size());
        assertEquals(1, optionProvider2.getOptions(0).size());
    }

    //--------------------------------------------------------------------------
    // Mode.EDIT
    //--------------------------------------------------------------------------
/*
    public void testEditNull() {
        String text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:</label>" +
                "</th><td><div id=\"myText\"><label for=\"myText_0\" class=\"radio\">" +
                "<input type=\"radio\" id=\"myText_0\" name=\"myText\"" +
                " value=\"value1\"></input>label1</label><label for=\"myText_1\"" +
                " class=\"radio\"><input type=\"radio\" id=\"myText_1\" name=\"myText\"" +
                " value=\"value2\"></input>label2</label><label for=\"myText_2\"" +
                " class=\"radio\"><input type=\"radio\" id=\"myText_2\" name=\"myText\"" +
                " value=\"value3\"></input>label3</label></div></td>", text);
    }

    public void testEditNullRequired() {
        radioField.setRequired(true);
        String text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\"><span " +
                "class=\"required\">*</span>&nbsp;My text:</label></th><td>" +
                "<div id=\"myText\"><label for=\"myText_0\" class=\"radio\">" +
                "<input type=\"radio\" id=\"myText_0\" name=\"myText\" value=\"value1\">" +
                "</input>label1</label><label for=\"myText_1\" " +
                "class=\"radio\"><input type=\"radio\" id=\"myText_1\" " +
                "name=\"myText\" value=\"value2\"></input>label2</label>" +
                "<label for=\"myText_2\" class=\"radio\"><input type=\"radio\" " +
                "id=\"myText_2\" name=\"myText\" value=\"value3\">" +
                "</input>label3</label></div></td>", text);

        assertFalse(radioField.validate());
        text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\"><span class=" +
                "\"required\">*</span>&nbsp;My text:</label></th><td>" +
                "<div id=\"myText\"><label for=\"myText_0\" class=\"radio\">" +
                "<input type=\"radio\" id=\"myText_0\" name=\"myText\" " +
                "value=\"value1\"></input>label1</label><label for=\"myText_1\" " +
                "class=\"radio\"><input type=\"radio\" id=\"myText_1\" " +
                "name=\"myText\" value=\"value2\"></input>label2</label>" +
                "<label for=\"myText_2\" class=\"radio\"><input type=\"radio\" " +
                "id=\"myText_2\" name=\"myText\" value=\"value3\"></input>" +
                "label3</label></div><ul class=\"errors\">" +
                "<li>Required field</li></ul></td>", text);
    }

    public void testEditValidSelection() {
        radioField.setStringValue("value1");
        String text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:</label>" +
                "</th><td><div id=\"myText\"><label for=\"myText_0\" " +
                "class=\"radio\"><input type=\"radio\" id=\"myText_0\" " +
                "name=\"myText\" value=\"value1\" checked=\"checked\">" +
                "</input>label1</label><label for=\"myText_1\" class=\"radio\">" +
                "<input type=\"radio\" id=\"myText_1\" name=\"myText\" " +
                "value=\"value2\"></input>label2</label><label for=\"myText_2\" " +
                "class=\"radio\"><input type=\"radio\" id=\"myText_2\" " +
                "name=\"myText\" value=\"value3\"></input>label3</label>" +
                "</div></td>", text);
    }

    public void testEditInvalidSelection() {
        radioField.setStringValue("value4");
        String text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:</label>" +
                "</th><td><div id=\"myText\"><label for=\"myText_0\" " +
                "class=\"radio\"><input type=\"radio\" id=\"myText_0\" " +
                "name=\"myText\" value=\"value1\"></input>label1</label>" +
                "<label for=\"myText_1\" class=\"radio\"><input type=\"radio\" " +
                "id=\"myText_1\" name=\"myText\" value=\"value2\">" +
                "</input>label2</label><label for=\"myText_2\" class=\"radio\">" +
                "<input type=\"radio\" id=\"myText_2\" name=\"myText\" " +
                "value=\"value3\"></input>label3</label></div></td>", text);
    }

    //--------------------------------------------------------------------------
    // Mode.VIEW
    //--------------------------------------------------------------------------

    public void testViewNull() {
        radioField.setMode(Mode.VIEW);
        String text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\"></div>" +
                "</td>", text);
    }

    public void testViewValidSelection() {
        radioField.setMode(Mode.VIEW);
        radioField.setStringValue("value1");
        String text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\"><a href=\"url1\">label1</a></div>" +
                "</td>", text);
    }

    public void testViewValidSelectionNoUrl() {
        radioField.setMode(Mode.VIEW);
        radioField.setStringValue("value3");
        String text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\">label3</div>" +
                "</td>", text);
    }

    public void testViewInvalidSelection() {
        radioField.setMode(Mode.VIEW);
        radioField.setStringValue("value4");
        String text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\"></div>" +
                "</td>", text);
    }

    //--------------------------------------------------------------------------
    // Mode.PREVIEW
    //--------------------------------------------------------------------------

    public void testPreviewNull() {
        radioField.setMode(Mode.PREVIEW);
        String text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\"></div>" +
                "<input type=\"hidden\" name=\"myText\"></input>" +
                "<input type=\"hidden\" name=\"myText_readonly\" value=\"\"></input>" +
                "</td>", text);
    }

    public void testPreviewValidSelection() {
        radioField.setMode(Mode.PREVIEW);
        radioField.setStringValue("value1");
        String text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\"><a href=\"url1\">label1</a></div>" +
                "<input type=\"hidden\" name=\"myText\" value=\"value1\"></input>" +
                "<input type=\"hidden\" name=\"myText_readonly\" value=\"\"></input>" +
                "</td>", text);
    }

    public void testPreviewValidSelectionNoUrl() {
        radioField.setMode(Mode.PREVIEW);
        radioField.setStringValue("value3");
        String text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\">label3</div>" +
                "<input type=\"hidden\" name=\"myText\" value=\"value3\"></input>" +
                "<input type=\"hidden\" name=\"myText_readonly\" value=\"\"></input>" +
                "</td>", text);
    }

    public void testPreviewInvalidSelection() {
        radioField.setMode(Mode.PREVIEW);
        radioField.setStringValue("value4");
        String text = elementToString(radioField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\"></div>" +
                "<input type=\"hidden\" name=\"myText\"></input>" +
                "<input type=\"hidden\" name=\"myText_readonly\" value=\"\"></input>" +
                "</td>", text);
    }


    //--------------------------------------------------------------------------
    // Mode.HIDDEN
    //--------------------------------------------------------------------------

    public void testHiddenNull() {
        radioField.setMode(Mode.HIDDEN);
        String text = elementToString(radioField);
        assertEquals("<input type=\"hidden\" name=\"myText\"></input>", text);
    }

    public void testHiddenValidSelection() {
        radioField.setMode(Mode.HIDDEN);
        radioField.setStringValue("value1");
        String text = elementToString(radioField);
        assertEquals("<input type=\"hidden\" name=\"myText\" value=\"value1\"></input>", text);
    }

    public void testHiddenValidSelectionNoUrl() {
        radioField.setMode(Mode.HIDDEN);
        radioField.setStringValue("value3");
        String text = elementToString(radioField);
        assertEquals("<input type=\"hidden\" name=\"myText\" value=\"value3\"></input>", text);
    }

    public void testHiddenInvalidSelection() {
        radioField.setMode(Mode.HIDDEN);
        radioField.setStringValue("value4");
        String text = elementToString(radioField);
        assertEquals("<input type=\"hidden\" name=\"myText\"></input>", text);
    }

    //--------------------------------------------------------------------------
    // metodi di Element
    //--------------------------------------------------------------------------

    public void testReadFromRequestWrongValue() {
        assertFalse(radioField.isRequired());

        req.setParameter("myText", "wrongValue");
        assertNull(radioField.getStringValue());
        radioField.readFromRequest(req);
        assertNull(radioField.getStringValue());
    }

    public void testReadFromRequest() {
        assertFalse(radioField.isRequired());

        req.setParameter("myText", "value1");
        assertNull(radioField.getStringValue());
        radioField.readFromRequest(req);
        assertEquals("value1", radioField.getStringValue());
    }

    public void testReadFromRequestRequired() {
        assertEquals(1, optionProvider2.getOptions(0).size());
        radioField2.setRequired(true);

        assertNull(radioField2.getStringValue());
        radioField2.readFromRequest(req);
        assertEquals("value1", radioField2.getStringValue());
    }

    public void testReadFromRequestNotRequired() {
        assertFalse(radioField.isRequired());
        assertEquals(1, optionProvider2.getOptions(0).size());

        assertNull(radioField2.getStringValue());
        radioField2.readFromRequest(req);
        assertNull(radioField2.getStringValue(), radioField2.getStringValue());
    }

    public void testReadFromObject() {
        assertNull(radioField.getStringValue());
        myText = "myValue2";
        radioField.readFromObject(this);
        assertEquals("myValue2", radioField.getStringValue());
    }

    public void testWriteToObject() {
        assertNull(myText);
        radioField.setStringValue("myValue2");
        radioField.writeToObject(this);
        assertEquals("myValue2", myText);
    }
    */
}