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
import com.manydesigns.elements.fields.SelectBean1;
import com.manydesigns.elements.util.FormUtil;
import com.manydesigns.elements.util.Util;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Test
public class FormTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    private Form form;
    private Form formWithPrefix;
    private Form formWithFieldNames;

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();

        FormBuilder builder =
                new FormBuilder(SelectBean1.class);

        form = builder.build();

        builder.configPrefix("prova.");
        formWithPrefix = builder.build();

        builder.configPrefix(null);
        builder.configFields("myUrl", "myValue");
        formWithFieldNames = builder.build();
    }

    public void testNullPrefix() {
        assertNull(form.getId());
        String text = Util.elementToString(form);
        assertEquals(
                "<fieldset class=\"mde-columns-1\"><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"myValue\" class=\"control-label\">My value</label><div><input id=\"myValue\" type=\"text\" name=\"myValue\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"myLabel\" class=\"control-label\">My label</label><div><input id=\"myLabel\" type=\"text\" name=\"myLabel\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"myUrl\" class=\"control-label\">My url</label><div><input id=\"myUrl\" type=\"text\" name=\"myUrl\" class=\"form-control\" /></div></div></div></div></fieldset>",
                text);
    }

    public void testWithPrefix() {
        assertNull(formWithPrefix.getId());
        String text = Util.elementToString(formWithPrefix);
        assertEquals(
                "<fieldset class=\"mde-columns-1\"><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"prova.myValue\" class=\"control-label\">My value</label><div><input id=\"prova.myValue\" type=\"text\" name=\"prova.myValue\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"prova.myLabel\" class=\"control-label\">My label</label><div><input id=\"prova.myLabel\" type=\"text\" name=\"prova.myLabel\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"prova.myUrl\" class=\"control-label\">My url</label><div><input id=\"prova.myUrl\" type=\"text\" name=\"prova.myUrl\" class=\"form-control\" /></div></div></div></div></fieldset>",
                text);
    }

    public void testWithFieldNames() {
        assertNull(formWithFieldNames.getId());
        String text = Util.elementToString(formWithFieldNames);
        assertEquals(
                "<fieldset class=\"mde-columns-1\"><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"myUrl\" class=\"control-label\">My url</label><div><input id=\"myUrl\" type=\"text\" name=\"myUrl\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"myValue\" class=\"control-label\">My value</label><div><input id=\"myValue\" type=\"text\" name=\"myValue\" class=\"form-control\" /></div></div></div></div></fieldset>",
                text);
    }
    
    public void testJson() {
        FormBuilder builder = new FormBuilder(AnnotatedBean3.class);
        Form formWithHref = builder.build();
        formWithHref.findFieldByPropertyName("field6").setHref("test");

        //Test full form
        AnnotatedBean3 obj = new AnnotatedBean3();
        obj.field6 = "1";
        formWithHref.readFromObject(obj);
        String toJson = FormUtil.writeToJson(formWithHref);
        assertEquals(
                "{\"field1\":{\"value\":null},\"field2\":{\"value\":null},\"field3\":{\"value\":null},\"field4\":{\"value\":null},\"field5\":{\"value\":null},\"field6\":{\"value\":\"1\",\"displayValue\":\"a\",\"href\":\"test\"}}",
                toJson);

        FormUtil.readFromJson(formWithHref, new JSONObject(toJson));
        assertEquals(toJson, FormUtil.writeToJson(formWithHref));

        //Test simple form
        FormUtil.readFromJson(formWithHref, new JSONObject("{\"field6\": \"1\"}"));
        assertEquals(toJson, FormUtil.writeToJson(formWithHref));
    }
}
