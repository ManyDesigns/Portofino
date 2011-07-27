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
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.options.SelectionModel;
import com.manydesigns.elements.options.DefaultSelectionProvider;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class SelectFieldTest3 extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    private SelectField selectField;

    private Long[] valuesArray = {
            1000L,
            2000L,
            3000L
    };
    private String[] labelsArray = {
            "label1",
            "label2",
            "label3"
    };

    protected SelectionProvider selectionProvider;
    protected SelectionModel selectionModel;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        selectionProvider = DefaultSelectionProvider.create(
                "selectionProvider", valuesArray, labelsArray);
        selectionModel = selectionProvider.createSelectionModel();
    }

    private void setupSelectFields(Mode mode) {
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(SelectBean3.class);
        PropertyAccessor myPropertyAccessor =
                null;
        try {
            myPropertyAccessor = classAccessor.getProperty("longValue");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            fail();
        }

        // impostiamo selectField1
        selectField = new SelectField(myPropertyAccessor, mode, null);
        selectField.setSelectionModel(selectionModel);
    }

    public void testSimpleRadio() {
        setupSelectFields(Mode.EDIT);

        String text = elementToString(selectField);
        assertEquals("<th><label for=\"longValue\" class=\"field\"><span class=\"required\">*</span>&nbsp;Long value:" +
                "</label></th><td><fieldset id=\"longValue\" class=\"radio\">" +
                "<input type=\"radio\" id=\"longValue_0\" name=\"longValue\" value=\"1000\" />&nbsp;<label for=\"longValue_0\">label1</label><br />" +
                "<input type=\"radio\" id=\"longValue_1\" name=\"longValue\" value=\"2000\" />&nbsp;<label for=\"longValue_1\">label2</label><br />" +
                "<input type=\"radio\" id=\"longValue_2\" name=\"longValue\" value=\"3000\" />&nbsp;<label for=\"longValue_2\">label3</label><br />" +
                "</fieldset></td>", text);

        assertFalse(selectField.validate());
        req.setParameter("longValue", "1000");
        selectField.readFromRequest(req);
        assertEquals(1000L, selectField.getValue());
        assertTrue(selectField.validate());
        assertEquals(1000L, selectField.getValue());
    }
}
