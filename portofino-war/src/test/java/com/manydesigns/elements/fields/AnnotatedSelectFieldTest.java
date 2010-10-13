/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;

import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class AnnotatedSelectFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    @Select(values={"v1", "v2"}, labels = {"l1", "l2"})
    public String myText;

    private SelectField selectField;

    SelectionProvider selectionProvider;

    @Override
    protected void setUp() throws Exception {
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

        Map<Object,String> options = selectField.getOptions();
        assertNotNull(options);
        assertEquals(2, options.size());

        assertEquals("l1", options.get("v1"));
        assertEquals("l2", options.get("v2"));
    }

    public void testEditNull() throws Exception {
        setupField(Mode.EDIT);

        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td><select id=\"myText\" name=\"myText\">" +
                "<option value=\"\" selected=\"selected\">-- Select my text --</option>" +
                "<option value=\"v1\">l1</option>" +
                "<option value=\"v2\">l2</option>" +
                "</select></td>", text);
    }

    public void testViewNull() throws Exception {
        setupField(Mode.VIEW);

        selectField.setValue("v2");
        String text = elementToString(selectField);
        assertEquals("<th><label for=\"myText\" class=\"field\">My text:" +
                "</label></th><td>" +
                "<div class=\"value\" id=\"myText\">l2</div></td>", text);
    }
}
