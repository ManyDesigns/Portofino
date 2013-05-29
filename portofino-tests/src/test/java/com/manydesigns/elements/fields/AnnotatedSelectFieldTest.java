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
import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.options.SelectionModel;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;

import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class AnnotatedSelectFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    @Select(values={"v1", "v2"}, labels = {"l1", "l2"})
    public String myText;

    private SelectField selectField;

    SelectionProvider selectionProvider;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        myText = null;
    }

    private void setupField(Mode mode) throws Exception {
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(this.getClass());
        PropertyAccessor myPropertyAccessor =
                classAccessor.getProperty("myText");
        selectField = new SelectField(myPropertyAccessor, mode, null);
    }

    public void testOptions() throws Exception {
        setupField(Mode.EDIT);

        Map<Object,SelectionModel.Option> options = selectField.getOptions();
        assertNotNull(options);
        assertEquals(2, options.size());

        assertEquals("l1", options.get("v1").label);
        assertEquals("l2", options.get("v2").label);
    }

    public void testEditNull() throws Exception {
        setupField(Mode.EDIT);

        String text = Util.elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"mde-field-label\">My text:" +
                "</label></th><td><select id=\"myText\" name=\"myText\">" +
                "<option value=\"\" selected=\"selected\">-- Select my text --</option>" +
                "<option value=\"v1\">l1</option>" +
                "<option value=\"v2\">l2</option>" +
                "</select></td>", text);
    }

    public void testViewNull() throws Exception {
        setupField(Mode.VIEW);

        selectField.setValue("v2");
        String text = Util.elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"mde-field-label\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\">l2</div></td>", text);
    }
}
