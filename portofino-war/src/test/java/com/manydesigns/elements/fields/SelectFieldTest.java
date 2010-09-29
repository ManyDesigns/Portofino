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
public class SelectFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public String myText;

    private SelectField selectField;
    private SelectField selectField2;

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
                JavaClassAccessor.getClassAccessor(this.getClass());
        PropertyAccessor myPropertyAccessor =
                classAccessor.getProperty("myText");
        selectField = new SelectField(myPropertyAccessor);

        optionProvider = DefaultOptionProvider.create(
                "optionProvider", 1, valuesArray, labelsArray);
        selectField.setOptionProvider(optionProvider);

        // impostiamo selectField2
        selectField2 = new SelectField(myPropertyAccessor);

        optionProvider2 = DefaultOptionProvider.create(
                "optionProvider", 1, valuesArray2, labelsArray2);
        selectField2.setOptionProvider(optionProvider2);
    }

    public void testSimple() {
        assertNotNull(selectField.getComboLabel());
        assertEquals("-- Select my text --", selectField.getComboLabel());
        assertEquals(optionProvider, selectField.getOptionProvider());
        assertEquals(optionProvider2, selectField2.getOptionProvider());
        assertNull(selectField.getValue());
        assertNotNull(selectField.getErrors());
        assertEquals(0, selectField.getErrors().size());
        assertNull(selectField.getHelp());
        assertEquals("myText", selectField.getId());
        assertEquals("myText", selectField.getInputName());
        assertEquals("my text", selectField.getLabel());
        assertEquals(Mode.EDIT, selectField.getMode());
        assertFalse(selectField.isRequired());
        assertEquals(3, optionProvider.getOptions(0).size());
        assertEquals(1, optionProvider2.getOptions(0).size());
    }

    //--------------------------------------------------------------------------
    // Mode.EDIT
    //--------------------------------------------------------------------------

    public void testEditNull() {
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><select id=\"myText\" name=\"myText\">" +
                "<option value=\"\" selected=\"selected\">-- Select my text --</option>" +
                "<option value=\"value1\">label1</option>" +
                "<option value=\"value2\">label2</option>" +
                "<option value=\"value3\">label3</option>" +
                "</select></td>", text);
    }

    public void testEditNullRequired() {
        selectField.setRequired(true);
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">" +
                "<span class=\"required\">*</span>&nbsp;My text:" +
                "</label></th><td><select id=\"myText\" name=\"myText\">" +
                "<option value=\"\" selected=\"selected\">-- Select my text --</option>" +
                "<option value=\"value1\">label1</option>" +
                "<option value=\"value2\">label2</option>" +
                "<option value=\"value3\">label3</option>" +
                "</select></td>", text);

        assertFalse(selectField.validate());
        text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">" +
                "<span class=\"required\">*</span>&nbsp;My text:" +
                "</label></th><td><select id=\"myText\" name=\"myText\">" +
                "<option value=\"\" selected=\"selected\">-- Select my text --</option>" +
                "<option value=\"value1\">label1</option>" +
                "<option value=\"value2\">label2</option>" +
                "<option value=\"value3\">label3</option>" +
                "</select><ul class=\"errors\">" +
                "<li>Required field" +
                "</li></ul></td>", text);
    }

    public void testEditNullWithComboLabel() {
        selectField.setComboLabel("-- Scegli opzione --");
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><select id=\"myText\" name=\"myText\">" +
                "<option value=\"\" selected=\"selected\">-- Scegli opzione --</option>" +
                "<option value=\"value1\">label1</option>" +
                "<option value=\"value2\">label2</option>" +
                "<option value=\"value3\">label3</option>" +
                "</select></td>", text);
        assertEquals("-- Scegli opzione --", selectField.getComboLabel());
    }

    public void testEditValidSelection() {
        selectField.setValue("value1");
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><select id=\"myText\" name=\"myText\">" +
                "<option value=\"\">-- Select my text --</option>" +
                "<option value=\"value1\" selected=\"selected\">label1</option>" +
                "<option value=\"value2\">label2</option>" +
                "<option value=\"value3\">label3</option>" +
                "</select></td>", text);
    }

    public void testEditInvalidSelection() {
        selectField.setValue("value4");
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><select id=\"myText\" name=\"myText\">" +
                "<option value=\"\" selected=\"selected\">-- Select my text --</option>" +
                "<option value=\"value1\">label1</option>" +
                "<option value=\"value2\">label2</option>" +
                "<option value=\"value3\">label3</option>" +
                "</select></td>", text);
    }

    //--------------------------------------------------------------------------
    // Mode.VIEW
    //--------------------------------------------------------------------------

    public void testViewNull() {
        selectField.setMode(Mode.VIEW);
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\"></div>" +
                "</td>", text);
    }

    public void testViewValidSelection() {
        selectField.setMode(Mode.VIEW);
        selectField.setValue("value1");
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\">label1</div>" +
                "</td>", text);
    }

    public void testViewValidSelectionNoUrl() {
        selectField.setMode(Mode.VIEW);
        selectField.setValue("value3");
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\">label3</div>" +
                "</td>", text);
    }

    public void testViewInvalidSelection() {
        selectField.setMode(Mode.VIEW);
        selectField.setValue("value4");
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\"></div>" +
                "</td>", text);
    }

    //--------------------------------------------------------------------------
    // Mode.PREVIEW
    //--------------------------------------------------------------------------

    public void testPreviewNull() {
        selectField.setMode(Mode.PREVIEW);
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\"></div>" +
                "<input type=\"hidden\" name=\"myText\"></input>" +
                "</td>", text);
    }

    public void testPreviewValidSelection() {
        selectField.setMode(Mode.PREVIEW);
        selectField.setValue("value1");
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\">label1</div>" +
                "<input type=\"hidden\" name=\"myText\" value=\"value1\"></input>" +
                "</td>", text);
    }

    public void testPreviewValidSelectionNoUrl() {
        selectField.setMode(Mode.PREVIEW);
        selectField.setValue("value3");
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\">label3</div>" +
                "<input type=\"hidden\" name=\"myText\" value=\"value3\"></input>" +
                "</td>", text);
    }

    public void testPreviewInvalidSelection() {
        selectField.setMode(Mode.PREVIEW);
        selectField.setValue("value4");
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\"></div>" +
                "<input type=\"hidden\" name=\"myText\"></input>" +
                "</td>", text);
    }


    //--------------------------------------------------------------------------
    // Mode.HIDDEN
    //--------------------------------------------------------------------------

    public void testHiddenNull() {
        selectField.setMode(Mode.HIDDEN);
        String text = elementToString(selectField);
        assertEquals("<input type=\"hidden\" name=\"myText\"></input>", text);
    }

    public void testHiddenValidSelection() {
        selectField.setMode(Mode.HIDDEN);
        selectField.setValue("value1");
        String text = elementToString(selectField);
        assertEquals("<input type=\"hidden\" name=\"myText\" value=\"value1\"></input>", text);
    }

    public void testHiddenValidSelectionNoUrl() {
        selectField.setMode(Mode.HIDDEN);
        selectField.setValue("value3");
        String text = elementToString(selectField);
        assertEquals("<input type=\"hidden\" name=\"myText\" value=\"value3\"></input>", text);
    }

    public void testHiddenInvalidSelection() {
        selectField.setMode(Mode.HIDDEN);
        selectField.setValue("value4");
        String text = elementToString(selectField);
        assertEquals("<input type=\"hidden\" name=\"myText\"></input>", text);
    }

    //--------------------------------------------------------------------------
    // metodi di Element
    //--------------------------------------------------------------------------

    public void testReadFromRequestWrongValue() {
        assertFalse(selectField.isRequired());

        req.setParameter("myText", "wrongValue");
        assertNull(selectField.getValue());
        selectField.readFromRequest(req);
        assertNull(selectField.getValue());
    }

    public void testReadFromRequest() {
        assertFalse(selectField.isRequired());

        req.setParameter("myText", "value1");
        assertNull(selectField.getValue());
        selectField.readFromRequest(req);
        assertEquals("value1", selectField.getValue());
    }

    public void testReadFromRequestRequired() {
        assertEquals(1, optionProvider2.getOptions(0).size());
        selectField2.setRequired(true);

        assertNull(selectField2.getValue());
        selectField2.readFromRequest(req);
        String text = elementToString(selectField2);
        assertEquals("<th><label for=\"myText\" class=\"field\">" +
                "<span class=\"required\">*</span>&nbsp;My text:" +
                "</label></th><td><select id=\"myText\" name=\"myText\">" +
                "<option value=\"\" selected=\"selected\">-- Select my text --</option>" +
                "<option value=\"value1\">label1</option>" +
                "</select></td>", text);
    }

    public void testReadFromRequestNotRequired() {
        assertFalse(selectField.isRequired());
        assertEquals(1, optionProvider2.getOptions(0).size());

        assertNull(selectField2.getValue());
        selectField2.readFromRequest(req);
        assertNull(selectField2.getValue());
    }

    public void testReadFromObject() {
        assertNull(selectField.getValue());
        myText = "value2";
        selectField.readFromObject(this);
        assertEquals("value2", selectField.getValue());
    }

    public void testWriteToObject() {
        assertNull(myText);
        selectField.setValue("value2");
        selectField.writeToObject(this);
        assertEquals("value2", myText);
    }
}
