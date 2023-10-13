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

package com.manydesigns.elements.forms;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.fields.*;
import com.manydesigns.elements.fields.helpers.FieldsManager;
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
public class FormBuilderTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public FormBuilder formBuilder1;
    public FormBuilder formBuilder2;
    public FormBuilder formBuilder3;

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();

        formBuilder1 = new FormBuilder(AllDefaultFieldsBean.class);
        formBuilder2 = new FormBuilder(AnnotatedBean1.class);
        formBuilder3 = new FormBuilder(ChildBean1.class);
    }

    public void testRegistry() {
        FieldsManager fieldsManager = FieldsManager.getManager();
        assertEquals(16, fieldsManager.getHelperList().size());

        Form form = formBuilder1.build();

        assertEquals(1, form.size());

        FieldSet fieldSet = form.get(0);
        assertEquals(18, fieldSet.size());

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

        field = (Field) fieldSet.get(16);
        assertEquals("an object", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(ObjectField.class, field.getClass());

        field = (Field) fieldSet.get(17);
        assertEquals("a self reference", field.getLabel());
        assertFalse(field.isRequired());
        assertEquals(ObjectField.class, field.getClass());
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
        assertEquals(
                "<fieldset class=\"mde-columns-1\"><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value required\"><label for=\"boolean1\" class=\"control-label\">Boolean1</label><div><div class=\"checkbox\"><input id=\"boolean1\" type=\"checkbox\" name=\"boolean1\" value=\"true\" /><label for=\"boolean1\"></label><input type=\"hidden\" name=\"__checkbox_boolean1\" value=\"true\" /></div></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value required\"><label for=\"boolean2\" class=\"control-label\">Boolean2</label><div><div class=\"checkbox\"><input id=\"boolean2\" type=\"checkbox\" name=\"boolean2\" value=\"true\" /><label for=\"boolean2\"></label><input type=\"hidden\" name=\"__checkbox_boolean2\" value=\"true\" /></div></div></div></div></div></fieldset><fieldset class=\"mde-columns-1\"><legend>foo</legend><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"date\" class=\"control-label\">Date</label><div><input id=\"date\" type=\"text\" name=\"date\" size=\"15\" class=\"form-control mde-text-field-with-explicit-size\" /><span class=\"help-block\">(yyyy-MM-dd) </span><script type=\"text/javascript\">$(function() { setupDatePicker('#date', 'yyyy-MM-dd'); });</script></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"decimal\" class=\"control-label\">Decimal</label><div><input id=\"decimal\" type=\"text\" name=\"decimal\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value required\"><label for=\"anInt\" class=\"control-label\">An int</label><div><input id=\"anInt\" type=\"text\" name=\"anInt\" min=\"-2147483648\" max=\"2147483647\" class=\"form-control\" /></div></div></div></div></fieldset><fieldset class=\"mde-columns-1\"><legend>bar</legend><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"anInteger\" class=\"control-label\">An integer</label><div><input id=\"anInteger\" type=\"text\" name=\"anInteger\" min=\"-2147483648\" max=\"2147483647\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"text\" class=\"control-label\">Text</label><div><input id=\"text\" type=\"text\" name=\"text\" class=\"form-control\" /></div></div></div></div></fieldset>",
                text);
    }


}
