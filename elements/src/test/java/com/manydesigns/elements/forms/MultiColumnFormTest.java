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
import com.manydesigns.elements.util.Util;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Test
public class MultiColumnFormTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public FormBuilder formBuilder1;

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();

        formBuilder1 = new FormBuilder(AnnotatedBean2.class);
    }

    public void testDefaultOneColumn() {
        Form form = formBuilder1.build();

        String text = Util.elementToString(form);
        assertEquals(
                "<fieldset class=\"mde-columns-1\"><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s1\" class=\"control-label\">S1</label><div><input id=\"s1\" type=\"text\" name=\"s1\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s2\" class=\"control-label\">S2</label><div><input id=\"s2\" type=\"text\" name=\"s2\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s3\" class=\"control-label\">S3</label><div><input id=\"s3\" type=\"text\" name=\"s3\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s4\" class=\"control-label\">S4</label><div><input id=\"s4\" type=\"text\" name=\"s4\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s5\" class=\"control-label\">S5</label><div><input id=\"s5\" type=\"text\" name=\"s5\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s6\" class=\"control-label\">S6</label><div><input id=\"s6\" type=\"text\" name=\"s6\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s7\" class=\"control-label\">S7</label><div><input id=\"s7\" type=\"text\" name=\"s7\" class=\"form-control\" /></div></div></div></div></fieldset>",
                text);
    }

    public void testTwoColumns() {
        Form form = formBuilder1.configNColumns(2).build();

        String text = Util.elementToString(form);
        assertEquals(
                "<fieldset class=\"mde-columns-2\"><div class=\"row\"><div class=\"col-md-6 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s1\" class=\"control-label\">S1</label><div><input id=\"s1\" type=\"text\" name=\"s1\" class=\"form-control\" /></div></div></div><div class=\"col-md-6 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s2\" class=\"control-label\">S2</label><div><input id=\"s2\" type=\"text\" name=\"s2\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-6 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s3\" class=\"control-label\">S3</label><div><input id=\"s3\" type=\"text\" name=\"s3\" class=\"form-control\" /></div></div></div><div class=\"col-md-6\"></div></div><div class=\"row\"><div class=\"col-md-12 mde-colspan-2\"><div class=\"form-group readwrite no-value\"><label for=\"s4\" class=\"control-label\">S4</label><div><input id=\"s4\" type=\"text\" name=\"s4\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-6 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s5\" class=\"control-label\">S5</label><div><input id=\"s5\" type=\"text\" name=\"s5\" class=\"form-control\" /></div></div></div><div class=\"col-md-6 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s6\" class=\"control-label\">S6</label><div><input id=\"s6\" type=\"text\" name=\"s6\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-6 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s7\" class=\"control-label\">S7</label><div><input id=\"s7\" type=\"text\" name=\"s7\" class=\"form-control\" /></div></div></div><div class=\"col-md-6\"></div></div></fieldset>",
                text);
    }

    public void testThreeColumns() {
        Form form = formBuilder1.configNColumns(3).build();

        String text = Util.elementToString(form);
        assertEquals(
                "<fieldset class=\"mde-columns-3\"><div class=\"row\"><div class=\"col-md-4 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s1\" class=\"control-label\">S1</label><div><input id=\"s1\" type=\"text\" name=\"s1\" class=\"form-control\" /></div></div></div><div class=\"col-md-4 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s2\" class=\"control-label\">S2</label><div><input id=\"s2\" type=\"text\" name=\"s2\" class=\"form-control\" /></div></div></div><div class=\"col-md-4 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s3\" class=\"control-label\">S3</label><div><input id=\"s3\" type=\"text\" name=\"s3\" class=\"form-control\" /></div></div></div></div><div class=\"row\"><div class=\"col-md-8 mde-colspan-2\"><div class=\"form-group readwrite no-value\"><label for=\"s4\" class=\"control-label\">S4</label><div><input id=\"s4\" type=\"text\" name=\"s4\" class=\"form-control\" /></div></div></div><div class=\"col-md-4\"></div></div><div class=\"row\"><div class=\"col-md-4 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s5\" class=\"control-label\">S5</label><div><input id=\"s5\" type=\"text\" name=\"s5\" class=\"form-control\" /></div></div></div><div class=\"col-md-4 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s6\" class=\"control-label\">S6</label><div><input id=\"s6\" type=\"text\" name=\"s6\" class=\"form-control\" /></div></div></div><div class=\"col-md-4 mde-colspan-1\"><div class=\"form-group readwrite no-value\"><label for=\"s7\" class=\"control-label\">S7</label><div><input id=\"s7\" type=\"text\" name=\"s7\" class=\"form-control\" /></div></div></div></div></fieldset>",
                text);
    }
}
