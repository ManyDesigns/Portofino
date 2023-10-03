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
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.servlet.MutableHttpServletRequest;
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
public class ObjectFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    ClassAccessor classAccessor;
    PropertyAccessor myPropertyAccessor;

    public NestedBean nested;

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        classAccessor = JavaClassAccessor.getClassAccessor(this.getClass());
        myPropertyAccessor = classAccessor.getProperty("nested");
    }


    //--------------------------------------------------------------------------
    // Required
    //--------------------------------------------------------------------------

    public void testRequiredEdit() {
        var field = new ObjectField(myPropertyAccessor, Mode.EDIT);
        assertEquals(Mode.EDIT, field.getMode());

        field.setRequired(true);
        assertTrue(field.isRequired());

        assertNull(field.getValue());
        String text = Util.elementToString(field);

        assertEquals(
                "<div class=\"form-group readwrite no-value required\"><label for=\"nested\" class=\"control-label\">Nested</label><div><fieldset class=\"mde-columns-1\"><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"nested.name\" class=\"control-label\">Name</label><div><input id=\"nested.name\" type=\"text\" name=\"nested.name\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value required\"><label for=\"nested.flag\" class=\"control-label\">Flag</label><div><div class=\"checkbox\"><input id=\"nested.flag\" type=\"checkbox\" name=\"nested.flag\" value=\"true\" /><label for=\"nested.flag\"></label><input type=\"hidden\" name=\"__checkbox_nested.flag\" value=\"true\" /></div></div></div></div></div></fieldset></div></div>",
                text);

        NestedBean n1 = new NestedBean("n1", true);
        field.setValue(n1);
        assertEquals(n1, field.getValue());
        text = Util.elementToString(field);

        assertEquals(
                "<div class=\"form-group readwrite no-value required\"><label for=\"nested\" class=\"control-label\">Nested</label><div><fieldset class=\"mde-columns-1\"><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite\"><label for=\"nested.name\" class=\"control-label\">Name</label><div><input id=\"nested.name\" type=\"text\" name=\"nested.name\" value=\"n1\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite required\"><label for=\"nested.flag\" class=\"control-label\">Flag</label><div><div class=\"checkbox\"><input id=\"nested.flag\" type=\"checkbox\" name=\"nested.flag\" value=\"true\" checked=\"checked\" /><label for=\"nested.flag\"></label><input type=\"hidden\" name=\"__checkbox_nested.flag\" value=\"true\" /></div></div></div></div></div></fieldset></div></div>",
                text);
    }

    /*
        public void testRequiredReadFromRequest() {
            booleanField = new BooleanField(myPropertyAccessor, Mode.EDIT);

            booleanField.setRequired(true);
            assertTrue(booleanField.isRequired());

            MutableHttpServletRequest req = new MutableHttpServletRequest();

            booleanField.setValue(true);
            assertTrue(booleanField.getValue());

            req.setParameter("myBoolean", "");
            booleanField.readFromRequest(req);
            assertFalse(booleanField.getValue());

            req.setParameter("myBoolean", "true");
            booleanField.readFromRequest(req);
            assertTrue(booleanField.getValue());

            req.setParameter("myBoolean", "false");
            booleanField.readFromRequest(req);
            assertFalse(booleanField.getValue());

            req.setParameter("myBoolean", (String) null);
            req.setParameter(BooleanField.CHECK_PREFIX + "myBoolean", "true");
            booleanField.readFromRequest(req);
            assertFalse(booleanField.getValue());

            booleanField.setValue(true);
            req.setParameter("myBoolean", (String) null);
            req.setParameter(BooleanField.CHECK_PREFIX + "myBoolean", (String) null);
            booleanField.readFromRequest(req);
            assertTrue(booleanField.getValue());
        }


        //--------------------------------------------------------------------------
        // Not required
        //--------------------------------------------------------------------------

        public void testNotRequiredEdit() {
            booleanField = new BooleanField(myPropertyAccessor, Mode.EDIT);
            assertEquals(Mode.EDIT, booleanField.getMode());

            booleanField.setRequired(false);
            assertFalse(booleanField.isRequired());

            assertNull(booleanField.getValue());
            String text = Util.elementToString(booleanField);

            assertEquals("<div class=\"form-group readwrite no-value\">" +
                    "<label for=\"myBoolean\" class=\"control-label\">My boolean</label>" +
                    "<div>" +
                    "<select id=\"myBoolean\" class=\"form-control\" name=\"myBoolean\">" +
                    "<option selected=\"selected\"></option>" +
                    "<option value=\"true\">Yes</option>" +
                    "<option value=\"false\">No</option>" +
                    "</select>" +
                    "</div>" +
                    "</div>", text);

            booleanField.setValue(false);
            assertFalse(booleanField.getValue());
            text = Util.elementToString(booleanField);

            assertEquals("<div class=\"form-group readwrite\">" +
                    "<label for=\"myBoolean\" class=\"control-label\">My boolean</label>" +
                    "<div>" +
                    "<select id=\"myBoolean\" class=\"form-control\" name=\"myBoolean\">" +
                    "<option></option>" +
                    "<option value=\"true\">Yes</option>" +
                    "<option value=\"false\" selected=\"selected\">No</option>" +
                    "</select>" +
                    "</div>" +
                    "</div>", text);


            booleanField.setValue(true);
            assertTrue(booleanField.getValue());
            text = Util.elementToString(booleanField);

            assertEquals("<div class=\"form-group readwrite\">" +
                    "<label for=\"myBoolean\" class=\"control-label\">My boolean</label>" +
                    "<div>" +
                    "<select id=\"myBoolean\" class=\"form-control\" name=\"myBoolean\">" +
                    "<option></option>" +
                    "<option value=\"true\" selected=\"selected\">Yes</option>" +
                    "<option value=\"false\">No</option>" +
                    "</select>" +
                    "</div>" +
                    "</div>", text);

        }

        public void testNotRequiredPreview() {
            booleanField = new BooleanField(myPropertyAccessor, Mode.PREVIEW);
            assertEquals(Mode.PREVIEW, booleanField.getMode());

            booleanField.setRequired(false);
            assertFalse(booleanField.isRequired());

            checkPreview();
        }

        public void testNotRequiredReadFromRequest() {
            booleanField = new BooleanField(myPropertyAccessor, Mode.EDIT);

            booleanField.setRequired(false);
            assertFalse(booleanField.isRequired());

            MutableHttpServletRequest req = new MutableHttpServletRequest();

            booleanField.setValue(true);
            assertTrue(booleanField.getValue());

            req.setParameter("myBoolean", "");
            booleanField.readFromRequest(req);
            assertNull(booleanField.getValue());

            req.setParameter("myBoolean", "true");
            booleanField.readFromRequest(req);
            assertTrue(booleanField.getValue());

            req.setParameter("myBoolean", "false");
            booleanField.readFromRequest(req);
            assertFalse(booleanField.getValue());

            req.setParameter("myBoolean", (String) null);
            req.setParameter(BooleanField.CHECK_PREFIX + "myBoolean", "true");
            booleanField.readFromRequest(req);
            assertNull(booleanField.getValue());

            booleanField.setValue(true);
            req.setParameter("myBoolean", (String) null);
            req.setParameter(BooleanField.CHECK_PREFIX + "myBoolean", (String) null);
            booleanField.readFromRequest(req);
            assertTrue(booleanField.getValue());
        }*/
    public static class NestedBean {
        public String name;
        public boolean flag;

        public NestedBean(String name, boolean flag) {
            this.name = name;
            this.flag = flag;
        }
    }
}
