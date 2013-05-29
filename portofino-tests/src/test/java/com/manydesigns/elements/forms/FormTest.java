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
import com.manydesigns.elements.util.Util;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class FormTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    private Form form;
    private Form formWithPrefix;
    private Form formWithFieldNames;

    @Override
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
        assertEquals("<fieldset class=\"nolegend\">" +
                "<table class=\"details\"><tr><th>" +
                "<label for=\"myValue\" class=\"field\">My value:</label>" +
                "</th><td><input id=\"myValue\" type=\"text\" name=\"myValue\" class=\"text\" />" +
                "</td></tr><tr><th>" +
                "<label for=\"myLabel\" class=\"field\">My label:</label>" +
                "</th><td><input id=\"myLabel\" type=\"text\" name=\"myLabel\" class=\"text\" />" +
                "</td></tr><tr><th>" +
                "<label for=\"myUrl\" class=\"field\">My url:</label></th><td>" +
                "<input id=\"myUrl\" type=\"text\" name=\"myUrl\" class=\"text\" />" +
                "</td></tr></table></fieldset>", text);
    }

    public void testWithPrefix() {
        assertNull(formWithPrefix.getId());
        String text = Util.elementToString(formWithPrefix);
        assertEquals("<fieldset class=\"nolegend\">" +
                "<table class=\"details\"><tr><th>" +
                "<label for=\"prova.myValue\" class=\"field\">My value:</label>" +
                "</th><td><input id=\"prova.myValue\" type=\"text\" name=\"prova.myValue\" class=\"text\" />" +
                "</td></tr><tr><th>" +
                "<label for=\"prova.myLabel\" class=\"field\">My label:</label>" +
                "</th><td><input id=\"prova.myLabel\" type=\"text\" name=\"prova.myLabel\" class=\"text\" />" +
                "</td></tr><tr><th>" +
                "<label for=\"prova.myUrl\" class=\"field\">My url:</label></th><td>" +
                "<input id=\"prova.myUrl\" type=\"text\" name=\"prova.myUrl\" class=\"text\" />" +
                "</td></tr></table></fieldset>", text);
    }

    public void testWithFieldNames() {
        assertNull(formWithFieldNames.getId());
        String text = Util.elementToString(formWithFieldNames);
        assertEquals("<fieldset class=\"nolegend\">" +
                "<table class=\"details\">" +
                "<tr><th>" +
                "<label for=\"myUrl\" class=\"field\">My url:</label></th><td>" +
                "<input id=\"myUrl\" type=\"text\" name=\"myUrl\" class=\"text\" />" +
                "</td></tr>" +
                "<tr><th>" +
                "<label for=\"myValue\" class=\"field\">My value:</label>" +
                "</th><td><input id=\"myValue\" type=\"text\" name=\"myValue\" class=\"text\" />" +
                "</td></tr>" +
                "</table></fieldset>", text);
    }
}
