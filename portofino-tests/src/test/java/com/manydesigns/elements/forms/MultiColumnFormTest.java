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

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class MultiColumnFormTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public FormBuilder formBuilder1;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        formBuilder1 = new FormBuilder(AnnotatedBean2.class);
    }

    public void testDefaultOneColumn() {
        Form form = formBuilder1.build();

        String text = Util.elementToString(form);
        assertEquals("<fieldset class=\"nolegend\">" +
                "<table class=\"details\">" +
                "<tr>" +
                "<th><label for=\"s1\" class=\"field\">S1:</label></th>" +
                "<td><input id=\"s1\" type=\"text\" name=\"s1\" class=\"text\" /></td>" +
                "</tr><tr>" +
                "<th><label for=\"s2\" class=\"field\">S2:</label></th>" +
                "<td><input id=\"s2\" type=\"text\" name=\"s2\" class=\"text\" /></td>" +
                "</tr><tr>" +
                "<th><label for=\"s3\" class=\"field\">S3:</label></th>" +
                "<td><input id=\"s3\" type=\"text\" name=\"s3\" class=\"text\" /></td>" +
                "</tr><tr>" +
                "<th><label for=\"s4\" class=\"field\">S4:</label></th>" +
                "<td colspan=\"3\"><input id=\"s4\" type=\"text\" name=\"s4\" class=\"text\" /></td>" +
                "</tr><tr>" +
                "<th><label for=\"s5\" class=\"field\">S5:</label></th>" +
                "<td><input id=\"s5\" type=\"text\" name=\"s5\" class=\"text\" /></td>" +
                "</tr><tr>" +
                "<th><label for=\"s6\" class=\"field\">S6:</label></th>" +
                "<td><input id=\"s6\" type=\"text\" name=\"s6\" class=\"text\" /></td>" +
                "</tr><tr>" +
                "<th><label for=\"s7\" class=\"field\">S7:</label></th>" +
                "<td><input id=\"s7\" type=\"text\" name=\"s7\" class=\"text\" /></td>" +
                "</tr>" +
                "</table>" +
                "</fieldset>", text);
    }

    public void testTwoColumns() {
        Form form = formBuilder1.configNColumns(2).build();

        String text = Util.elementToString(form);
        assertEquals("<fieldset class=\"nolegend\">" +
                "<table class=\"details\">" +
                "<tr>" +
                "<th><label for=\"s1\" class=\"field\">S1:</label></th>" +
                "<td><input id=\"s1\" type=\"text\" name=\"s1\" class=\"text\" /></td>" +
                "<th><label for=\"s2\" class=\"field\">S2:</label></th>" +
                "<td><input id=\"s2\" type=\"text\" name=\"s2\" class=\"text\" /></td>" +
                "</tr><tr>" +
                "<th><label for=\"s3\" class=\"field\">S3:</label></th>" +
                "<td><input id=\"s3\" type=\"text\" name=\"s3\" class=\"text\" /></td>" +
                "<td colspan=\"2\"></td>" +
                "</tr><tr>" +
                "<th><label for=\"s4\" class=\"field\">S4:</label></th>" +
                "<td colspan=\"3\"><input id=\"s4\" type=\"text\" name=\"s4\" class=\"text\" /></td>" +
                "</tr><tr>" +
                "<th><label for=\"s5\" class=\"field\">S5:</label></th>" +
                "<td><input id=\"s5\" type=\"text\" name=\"s5\" class=\"text\" /></td>" +
                "<th><label for=\"s6\" class=\"field\">S6:</label></th>" +
                "<td><input id=\"s6\" type=\"text\" name=\"s6\" class=\"text\" /></td>" +
                "</tr><tr>" +
                "<th><label for=\"s7\" class=\"field\">S7:</label></th>" +
                "<td><input id=\"s7\" type=\"text\" name=\"s7\" class=\"text\" /></td>" +
                "<td colspan=\"2\"></td>" +
                "</tr>" +
                "</table>" +
                "</fieldset>", text);
    }

    public void testThreeColumns() {
        Form form = formBuilder1.configNColumns(3).build();

        String text = Util.elementToString(form);
        assertEquals("<fieldset class=\"nolegend\">" +
                "<table class=\"details\">" +
                "<tr>" +
                "<th><label for=\"s1\" class=\"field\">S1:</label></th>" +
                "<td><input id=\"s1\" type=\"text\" name=\"s1\" class=\"text\" /></td>" +
                "<th><label for=\"s2\" class=\"field\">S2:</label></th>" +
                "<td><input id=\"s2\" type=\"text\" name=\"s2\" class=\"text\" /></td>" +
                "<th><label for=\"s3\" class=\"field\">S3:</label></th>" +
                "<td><input id=\"s3\" type=\"text\" name=\"s3\" class=\"text\" /></td>" +
                "</tr><tr>" +
                "<th><label for=\"s4\" class=\"field\">S4:</label></th>" +
                "<td colspan=\"3\"><input id=\"s4\" type=\"text\" name=\"s4\" class=\"text\" /></td>" +
                "<td colspan=\"2\"></td>" +
                "</tr><tr>" +
                "<th><label for=\"s5\" class=\"field\">S5:</label></th>" +
                "<td><input id=\"s5\" type=\"text\" name=\"s5\" class=\"text\" /></td>" +
                "<th><label for=\"s6\" class=\"field\">S6:</label></th>" +
                "<td><input id=\"s6\" type=\"text\" name=\"s6\" class=\"text\" /></td>" +
                "<th><label for=\"s7\" class=\"field\">S7:</label></th>" +
                "<td><input id=\"s7\" type=\"text\" name=\"s7\" class=\"text\" /></td>" +
                "</tr>" +
                "</table>" +
                "</fieldset>", text);
    }
}
