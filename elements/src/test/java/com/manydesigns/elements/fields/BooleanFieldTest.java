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
public class BooleanFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    ClassAccessor classAccessor;
    PropertyAccessor myPropertyAccessor;

    public Boolean myBoolean;
    private BooleanField booleanField;


    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();

        myBoolean = null;

        classAccessor = JavaClassAccessor.getClassAccessor(this.getClass());
        myPropertyAccessor = classAccessor.getProperty("myBoolean");
    }


    //--------------------------------------------------------------------------
    // Required
    //--------------------------------------------------------------------------

    public void testRequiredEdit() {
        booleanField = new BooleanField(myPropertyAccessor, Mode.EDIT);
        assertEquals(Mode.EDIT, booleanField.getMode());

        booleanField.setRequired(true);
        assertTrue(booleanField.isRequired());

        assertNull(booleanField.getValue());
        String text = Util.elementToString(booleanField);

        assertEquals("<div class=\"form-group readwrite no-value required\"><label for=\"myBoolean\" class=\"control-label\">My boolean</label><div><div class=\"checkbox\"><input id=\"myBoolean\" type=\"checkbox\" name=\"myBoolean\" value=\"true\" /><label for=\"myBoolean\"></label><input type=\"hidden\" name=\"__checkbox_myBoolean\" value=\"true\" /></div></div></div>", text);

        booleanField.setValue(false);
        assertFalse(booleanField.getValue());
        text = Util.elementToString(booleanField);

        assertEquals("<div class=\"form-group readwrite required\"><label for=\"myBoolean\" class=\"control-label\">My boolean</label><div><div class=\"checkbox\"><input id=\"myBoolean\" type=\"checkbox\" name=\"myBoolean\" value=\"true\" /><label for=\"myBoolean\"></label><input type=\"hidden\" name=\"__checkbox_myBoolean\" value=\"true\" /></div></div></div>", text);

        booleanField.setValue(true);
        assertTrue(booleanField.getValue());
        text = Util.elementToString(booleanField);

        assertEquals("<div class=\"form-group readwrite required\"><label for=\"myBoolean\" class=\"control-label\">My boolean</label><div><div class=\"checkbox\"><input id=\"myBoolean\" type=\"checkbox\" name=\"myBoolean\" value=\"true\" checked=\"checked\" /><label for=\"myBoolean\"></label><input type=\"hidden\" name=\"__checkbox_myBoolean\" value=\"true\" /></div></div></div>", text);

    }


    public void testRequiredPreview() {
        booleanField = new BooleanField(myPropertyAccessor, Mode.PREVIEW);
        assertEquals(Mode.PREVIEW, booleanField.getMode());

        booleanField.setRequired(true);
        assertTrue(booleanField.isRequired());

        checkPreview();
    }

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
    }

    //--------------------------------------------------------------------------
    // Common
    //--------------------------------------------------------------------------

    protected void checkPreview() {
        assertNull(booleanField.getValue());
        String text = Util.elementToString(booleanField);
        assertEquals("<div class=\"form-group readwrite no-value" + (booleanField.isRequired() ? " required" : "") + "\">" +
                "<label for=\"myBoolean\" class=\"control-label\">My boolean</label>" +
                "<div>" +
                "<p class=\"form-control-static\" id=\"myBoolean\"></p>" +
                "<input type=\"hidden\" name=\"myBoolean\" />" +
                "<input type=\"hidden\" name=\"__checkbox_myBoolean\" value=\"true\" />" +
                "</div>" +
                "</div>", text);

        booleanField.setValue(false);
        assertFalse(booleanField.getValue());
        text = Util.elementToString(booleanField);
        assertEquals("<div class=\"form-group readwrite" + (booleanField.isRequired() ? " required" : "") + "\">" +
                "<label for=\"myBoolean\" class=\"control-label\">My boolean</label>" +
                "<div>" +
                "<p class=\"form-control-static\" id=\"myBoolean\">No</p>" +
                "<input type=\"hidden\" name=\"myBoolean\" value=\"false\" />" +
                "<input type=\"hidden\" name=\"__checkbox_myBoolean\" value=\"true\" />" +
                "</div>" +
                "</div>", text);

        booleanField.setValue(true);
        assertTrue(booleanField.getValue());
        text = Util.elementToString(booleanField);
        assertEquals("<div class=\"form-group readwrite" + (booleanField.isRequired() ? " required" : "") + "\">" +
                "<label for=\"myBoolean\" class=\"control-label\">My boolean</label>" +
                "<div>" +
                "<p class=\"form-control-static\" id=\"myBoolean\">Yes</p>" +
                "<input type=\"hidden\" name=\"myBoolean\" value=\"true\" />" +
                "<input type=\"hidden\" name=\"__checkbox_myBoolean\" value=\"true\" />" +
                "</div>" +
                "</div>", text);
    }


}
