/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.forms;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.fields.*;
import com.manydesigns.elements.fields.helpers.FieldsManager;
import com.manydesigns.elements.util.Util;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class FormBuilderTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public FormBuilder formBuilder1;
    public FormBuilder formBuilder2;
    public FormBuilder formBuilder3;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        formBuilder1 = new FormBuilder(AllDefaultFieldsBean.class);
        formBuilder2 = new FormBuilder(AnnotatedBean1.class);
        formBuilder3 = new FormBuilder(ChildBean1.class);
    }

    public void testRegistry() {
        FieldsManager fieldsManager = FieldsManager.getManager();
        assertEquals(12, fieldsManager.getHelperList().size());

        Form form = formBuilder1.build();

        assertEquals(1, form.size());

        FieldSet fieldSet = form.get(0);
        assertEquals(16, fieldSet.size());

        Field field = (Field) fieldSet.get(0);
        assertEquals("A private int", field.getLabel());
        assertTrue(field.isRequired());
        assertEquals(NumericField.class, field.getClass());

        field = (Field) fieldSet.get(1);
        assertEquals("a boolean1", field.getLabel());
        assertTrue(field.isRequired());
        assertEquals(BooleanField.class, field.getClass());

        field = (Field) fieldSet.get(2);
        assertEquals("a boolean2", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(BooleanField.class, field.getClass());

        field = (Field) fieldSet.get(3);
        assertEquals("date", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(DateField.class, field.getClass());

        field = (Field) fieldSet.get(4);
        assertEquals("decimal", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(NumericField.class, field.getClass());

        field = (Field) fieldSet.get(5);
        assertEquals("an int", field.getLabel());
        assertTrue(field.isRequired());
        assertEquals(NumericField.class, field.getClass());

        field = (Field) fieldSet.get(6);
        assertEquals("an integer", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(NumericField.class, field.getClass());

        field = (Field) fieldSet.get(7);
        assertEquals("cap", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(CAPField.class, field.getClass());

        field = (Field) fieldSet.get(8);
        assertEquals("codice fiscale", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(CodiceFiscaleField.class, field.getClass());

        field = (Field) fieldSet.get(9);
        assertEquals("email", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(EmailField.class, field.getClass());

        field = (Field) fieldSet.get(10);
        assertEquals("partita iva", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(PartitaIvaField.class, field.getClass());

        field = (Field) fieldSet.get(11);
        assertEquals("password", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(PasswordField.class, field.getClass());

        field = (Field) fieldSet.get(12);
        assertEquals("phone", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(PhoneField.class, field.getClass());

        field = (Field) fieldSet.get(13);
        assertEquals("select", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(SelectField.class, field.getClass());

        field = (Field) fieldSet.get(14);
        assertEquals("text", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(TextField.class, field.getClass());

        field = (Field) fieldSet.get(15);
        assertEquals("a blob", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(FileBlobField.class, field.getClass());
    }

    public void testConfigFields() throws NoSuchFieldException {
        formBuilder1.configFields("anInteger", "decimal", "cap");
        Form form = formBuilder1.build();

        assertEquals(1, form.size());

        FieldSet fieldSet = form.get(0);
        assertEquals(3, fieldSet.size());
        assertNull(fieldSet.getName());

        Field field = (Field) fieldSet.get(0);
        assertEquals("an integer", field.getLabel());
        assertEquals(NumericField.class, field.getClass());

        field = (Field) fieldSet.get(1);
        assertEquals("decimal", field.getLabel());
        assertEquals(NumericField.class, field.getClass());

        field = (Field) fieldSet.get(2);
        assertEquals("cap", field.getLabel());
        assertEquals(CAPField.class, field.getClass());
    }

    private String fieldArrays[][] = {
            {"anInteger", "decimal", "cap"},
            {"phone"}
    };

    public void testConfigFieldsWithGroups() throws NoSuchFieldException {
        formBuilder1.configFields(fieldArrays);
        formBuilder1.configFieldSetNames("foo", "bar");
        Form form = formBuilder1.build();

        assertEquals(2, form.size());

        FieldSet fieldSet = form.get(0);
        assertNotNull(fieldSet.getName());
        assertEquals("foo", fieldSet.getName());
        assertEquals(3, fieldSet.size());

        Field field = (Field) fieldSet.get(0);
        assertEquals("an integer", field.getLabel());
        assertEquals(NumericField.class, field.getClass());

        field = (Field) fieldSet.get(1);
        assertEquals("decimal", field.getLabel());
        assertEquals(NumericField.class, field.getClass());

        field = (Field) fieldSet.get(2);
        assertEquals("cap", field.getLabel());
        assertEquals(CAPField.class, field.getClass());


        fieldSet = form.get(1);
        assertNotNull(fieldSet.getName());
        assertEquals("bar", fieldSet.getName());
        assertEquals(1, fieldSet.size());

        field = (Field) fieldSet.get(0);
        assertEquals("phone", field.getLabel());
        assertEquals(PhoneField.class, field.getClass());
    }

    public void testConfigFieldsWithReflectiveGroups()
            throws NoSuchFieldException {
        Form form = formBuilder2.build();

        assertEquals(3, form.size());

        FieldSet fieldSet = form.get(0);
        assertNull(fieldSet.getName());
        assertEquals(2, fieldSet.size());

        // Field set 0
        Field field = (Field) fieldSet.get(0);
        assertEquals("boolean1", field.getLabel());
        assertEquals(BooleanField.class, field.getClass());

        field = (Field) fieldSet.get(1);
        assertEquals("boolean2", field.getLabel());
        assertEquals(BooleanField.class, field.getClass());


        // Field set 1
        fieldSet = form.get(1);
        assertNotNull(fieldSet.getName());
        assertEquals("foo", fieldSet.getName());
        assertEquals(3, fieldSet.size());

        field = (Field) fieldSet.get(0);
        assertEquals("date", field.getLabel());
        assertEquals(DateField.class, field.getClass());

        field = (Field) fieldSet.get(1);
        assertEquals("decimal", field.getLabel());
        assertEquals(NumericField.class, field.getClass());

        field = (Field) fieldSet.get(2);
        assertEquals("an int", field.getLabel());
        assertEquals(NumericField.class, field.getClass());


        // Field set 2
        fieldSet = form.get(2);
        assertNotNull(fieldSet.getName());
        assertEquals("bar", fieldSet.getName());
        assertEquals(2, fieldSet.size());

        field = (Field) fieldSet.get(0);
        assertEquals("an integer", field.getLabel());
        assertEquals(NumericField.class, field.getClass());

        field = (Field) fieldSet.get(1);
        assertEquals("text", field.getLabel());
        assertEquals(TextField.class, field.getClass());
    }

    public void testInheritance() throws NoSuchFieldException {
        Form form = formBuilder3.build();

        assertEquals(1, form.size());

        FieldSet fieldSet = form.get(0);
        assertEquals(3, fieldSet.size());
        assertNull(fieldSet.getName());

        Field field = (Field) fieldSet.get(0);
        assertEquals("child text", field.getLabel());
        assertEquals(TextField.class, field.getClass());

        field = (Field) fieldSet.get(1);
        assertEquals("another parent text", field.getLabel());
        assertEquals(TextField.class, field.getClass());

        field = (Field) fieldSet.get(2);
        assertEquals("parent text", field.getLabel());
        assertEquals(TextField.class, field.getClass());
    }

    public void testToString() {
        Form form = formBuilder2.build();

        String text = Util.elementToString(form);
        assertEquals("<fieldset class=\"nolegend\"><table class=\"details\">" +
                "<tr><th>" +
                "<label for=\"boolean1\" class=\"field\">Boolean1:</label></th>" +
                "<td><input id=\"boolean1\" type=\"checkbox\" name=\"boolean1\" " +
                "value=\"true\" class=\"checkbox\" />" +
                "<input type=\"hidden\" name=\"__checkbox_boolean1\" value=\"true\" />" +
                "</td></tr><tr><th>" +
                "<label for=\"boolean2\" class=\"field\">Boolean2:</label>" +
                "</th><td><input id=\"boolean2\" type=\"checkbox\" " +
                "name=\"boolean2\" value=\"true\" class=\"checkbox\" />" +
                "<input type=\"hidden\" name=\"__checkbox_boolean2\" value=\"true\" />" +
                "</td></tr></table></fieldset>" +
                "<fieldset><legend>foo</legend>" +
                "<table class=\"details\"><tr><th>" +
                "<label for=\"date\" class=\"field\">Date:</label></th>" +
                "<td><input type=\"text\" class=\"text\" id=\"date\" " +
                "name=\"date\" maxlength=\"15\" size=\"15\" /> (yyyy-MM-dd) " +
                "<script type=\"text/javascript\">setupDatePicker('#date', 'yy-mm-dd');</script>" +
                "</td></tr><tr><th>" +
                "<label for=\"decimal\" class=\"field\">Decimal:</label></th>" +
                "<td><input id=\"decimal\" type=\"text\" name=\"decimal\" " +
                "class=\"text\" /></td></tr>" +
                "<tr><th><label for=\"anInt\" class=\"field\">" +
                "<span class=\"required\">*</span>&nbsp;An int:</label></th>" +
                "<td><input id=\"anInt\" type=\"text\" name=\"anInt\" " +
                "class=\"text\" /></td></tr></table></fieldset>" +
                "<fieldset><legend>bar</legend>" +
                "<table class=\"details\"><tr><th><label for=\"anInteger\" " +
                "class=\"field\">An integer:</label></th><td>" +
                "<input id=\"anInteger\" type=\"text\" name=\"anInteger\" " +
                "class=\"text\" /></td></tr><tr><th>" +
                "<label for=\"text\" class=\"field\">Text:</label></th><td>" +
                "<input id=\"text\" type=\"text\" name=\"text\" class=\"text\" />" +
                "</td></tr></table></fieldset>", text);
    }


}
