/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.portofino.util.DummyHttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class BooleanFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    ClassAccessor classAccessor;
    PropertyAccessor myPropertyAccessor;

    public Boolean myBoolean;
    private BooleanField booleanField;


    @Override
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

        assertNull(booleanField.getBooleanValue());
        String text = elementToString(booleanField);
        assertEquals("<th><label for=\"myBoolean\" class=\"field\">" +
                "My boolean:</label></th><td>" +
                "<input id=\"myBoolean\" type=\"checkbox\" name=\"myBoolean\" value=\"true\" class=\"checkbox\" />" +
                "<input type=\"hidden\" name=\"__checkbox_myBoolean\" value=\"true\" />" +
                "</td>", text);

        booleanField.setBooleanValue(false);
        assertFalse(booleanField.getBooleanValue());
        text = elementToString(booleanField);
        assertEquals("<th><label for=\"myBoolean\" class=\"field\">" +
                "My boolean:</label></th><td>" +
                "<input id=\"myBoolean\" type=\"checkbox\" name=\"myBoolean\" value=\"true\" class=\"checkbox\" />" +
                "<input type=\"hidden\" name=\"__checkbox_myBoolean\" value=\"true\" />" +
                "</td>", text);

        booleanField.setBooleanValue(true);
        assertTrue(booleanField.getBooleanValue());
        text = elementToString(booleanField);
        assertEquals("<th><label for=\"myBoolean\" class=\"field\">" +
                "My boolean:</label></th><td>" +
                "<input id=\"myBoolean\" type=\"checkbox\" name=\"myBoolean\" value=\"true\" checked=\"checked\" class=\"checkbox\" />" +
                "<input type=\"hidden\" name=\"__checkbox_myBoolean\" value=\"true\" />" +
                "</td>", text);
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

        DummyHttpServletRequest req = new DummyHttpServletRequest();

        booleanField.setBooleanValue(true);
        assertTrue(booleanField.getBooleanValue());

        req.setParameter("myBoolean", "");
        booleanField.readFromRequest(req);
        assertFalse(booleanField.getBooleanValue());

        req.setParameter("myBoolean", "true");
        booleanField.readFromRequest(req);
        assertTrue(booleanField.getBooleanValue());

        req.setParameter("myBoolean", "false");
        booleanField.readFromRequest(req);
        assertFalse(booleanField.getBooleanValue());

        req.setParameter("myBoolean", null);
        req.setParameter(BooleanField.CHECK_PREFIX + "myBoolean", "true");
        booleanField.readFromRequest(req);
        assertFalse(booleanField.getBooleanValue());

        booleanField.setBooleanValue(true);
        req.setParameter("myBoolean", null);
        req.setParameter(BooleanField.CHECK_PREFIX + "myBoolean", null);
        booleanField.readFromRequest(req);
        assertTrue(booleanField.getBooleanValue());
    }


    //--------------------------------------------------------------------------
    // Not required
    //--------------------------------------------------------------------------

    public void testNotRequiredEdit() {
        booleanField = new BooleanField(myPropertyAccessor, Mode.EDIT);
        assertEquals(Mode.EDIT, booleanField.getMode());

        booleanField.setRequired(false);
        assertFalse(booleanField.isRequired());

        assertNull(booleanField.getBooleanValue());
        String text = elementToString(booleanField);
        assertEquals("<th><label for=\"myBoolean\" class=\"field\">" +
                "My boolean:</label></th><td>" +
                "<select id=\"myBoolean\" name=\"myBoolean\">" +
                "<option selected=\"selected\"></option>" +
                "<option value=\"true\">Yes</option>" +
                "<option value=\"false\">No</option>" +
                "</select></td>", text);

        booleanField.setBooleanValue(false);
        assertFalse(booleanField.getBooleanValue());
        text = elementToString(booleanField);
        assertEquals("<th><label for=\"myBoolean\" class=\"field\">" +
                "My boolean:</label></th><td>" +
                "<select id=\"myBoolean\" name=\"myBoolean\">" +
                "<option></option>" +
                "<option value=\"true\">Yes</option>" +
                "<option value=\"false\" selected=\"selected\">No</option>" +
                "</select></td>", text);

        booleanField.setBooleanValue(true);
        assertTrue(booleanField.getBooleanValue());
        text = elementToString(booleanField);
        assertEquals("<th><label for=\"myBoolean\" class=\"field\">" +
                "My boolean:</label></th><td>" +
                "<select id=\"myBoolean\" name=\"myBoolean\">" +
                "<option></option>" +
                "<option value=\"true\" selected=\"selected\">Yes</option>" +
                "<option value=\"false\">No</option>" +
                "</select></td>", text);
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

        DummyHttpServletRequest req = new DummyHttpServletRequest();

        booleanField.setBooleanValue(true);
        assertTrue(booleanField.getBooleanValue());

        req.setParameter("myBoolean", "");
        booleanField.readFromRequest(req);
        assertNull(booleanField.getBooleanValue());

        req.setParameter("myBoolean", "true");
        booleanField.readFromRequest(req);
        assertTrue(booleanField.getBooleanValue());

        req.setParameter("myBoolean", "false");
        booleanField.readFromRequest(req);
        assertFalse(booleanField.getBooleanValue());

        req.setParameter("myBoolean", null);
        req.setParameter(BooleanField.CHECK_PREFIX + "myBoolean", "true");
        booleanField.readFromRequest(req);
        assertNull(booleanField.getBooleanValue());

        booleanField.setBooleanValue(true);
        req.setParameter("myBoolean", null);
        req.setParameter(BooleanField.CHECK_PREFIX + "myBoolean", null);
        booleanField.readFromRequest(req);
        assertTrue(booleanField.getBooleanValue());
    }

    //--------------------------------------------------------------------------
    // Common
    //--------------------------------------------------------------------------

    protected void checkPreview() {
        assertNull(booleanField.getBooleanValue());
        String text = elementToString(booleanField);
        assertEquals("<th><label for=\"myBoolean\" class=\"field\">" +
                "My boolean:</label></th><td>" +
                "<div class=\"value\" id=\"myBoolean\"></div>" +
                "<input type=\"hidden\" name=\"myBoolean\" />" +
                "<input type=\"hidden\" name=\"__checkbox_myBoolean\" value=\"true\" />" +
                "</td>", text);

        booleanField.setBooleanValue(false);
        assertFalse(booleanField.getBooleanValue());
        text = elementToString(booleanField);
        assertEquals("<th><label for=\"myBoolean\" class=\"field\">" +
                "My boolean:</label></th><td>" +
                "<div class=\"value\" id=\"myBoolean\">No</div>" +
                "<input type=\"hidden\" name=\"myBoolean\" value=\"false\" />" +
                "<input type=\"hidden\" name=\"__checkbox_myBoolean\" value=\"true\" />" +
                "</td>", text);

        booleanField.setBooleanValue(true);
        assertTrue(booleanField.getBooleanValue());
        text = elementToString(booleanField);
        assertEquals("<th><label for=\"myBoolean\" class=\"field\">" +
                "My boolean:</label></th><td>" +
                "<div class=\"value\" id=\"myBoolean\">Yes</div>" +
                "<input type=\"hidden\" name=\"myBoolean\" value=\"true\" />" +
                "<input type=\"hidden\" name=\"__checkbox_myBoolean\" value=\"true\" />" +
                "</td>", text);
    }


}
