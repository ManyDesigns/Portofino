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
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.fields.SelectFieldTest2;
import com.manydesigns.elements.fields.search.SelectSearchField;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionModel;
import com.manydesigns.elements.util.Util;

import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class SearchFormCascadeTest  extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";
    Integer[][] valuesArray = {
            {1, 1},
            {1, 2},
            {1, 3},
            {2, 4}
    };

    String[][] labelsArray = {
            {"paperino", "qui"},
            {"paperino", "quo"},
            {"paperino", "qua"},
            {"cip", "ciop"}
    };

    DefaultSelectionProvider selectionProvider;

    SearchForm form;
    SelectSearchField selectField1;
    SelectSearchField selectField2;
    SelectionModel selectionModel;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        selectionProvider = new DefaultSelectionProvider("selectionProvider", 2);
        for(int i = 0; i < valuesArray.length; i++) {
            selectionProvider.appendRow(valuesArray[i], labelsArray[i], true);
        }

        form = new SearchFormBuilder(SelectFieldTest2.Bean.class)
                .configSelectionProvider(selectionProvider, "p1", "p2")
                .build();
        selectField1 = (SelectSearchField) form.get(0);
        selectField2 = (SelectSearchField) form.get(1);

        selectionModel = selectField1.getSelectionModel();
    }

    public void testHtml() {
        assertNull(form.getId());
        String text = Util.elementToString(form);
        assertEquals("<ul class=\"searchform\">" +
                "<li><fieldset><legend class=\"attr_name\">P1</legend>" +
                "<select id=\"p1\" name=\"p1\">" +
                "<option value=\"\" selected=\"selected\">-- Select p1 --</option>" +
                "<option value=\"1\">paperino</option>" +
                "<option value=\"2\">cip</option>" +
                "</select>" +
                "</fieldset></li>" +
                "<li><fieldset><legend class=\"attr_name\">P2</legend>" +
                "<select id=\"p2\" name=\"p2\"></select>" +
                "</fieldset></li></ul>", text);
        req.addParameter("p1", "1");
        form.readFromRequest(req);
        text = Util.elementToString(form);
        assertNotNull(selectField1.getValues());
        assertEquals(1, selectField1.getValues()[0]);
        assertNull(selectField2.getValues());
        Map<Object,SelectionModel.Option> options0 = selectField1.getOptions();
        assertNotNull(options0);
        assertEquals(2, options0.size());
        assertEquals("paperino", options0.get(1).label);


        Map<Object,SelectionModel.Option> options1 = selectField2.getOptions();
        assertNotNull(options1);
        assertEquals(3, options1.size());
        assertEquals("qui", options1.get(1).label);
        assertEquals("quo", options1.get(2).label);
        assertEquals("qua", options1.get(3).label);

        assertEquals("<ul class=\"searchform\"><li><fieldset><legend class=\"attr_name\">P1</legend><select id=\"p1\" name=\"p1\"><option value=\"\">-- Select p1 --</option><option value=\"1\" selected=\"selected\">paperino</option><option value=\"2\">cip</option></select></fieldset></li><li><fieldset><legend class=\"attr_name\">P2</legend><select id=\"p2\" name=\"p2\"><option value=\"\" selected=\"selected\">-- Select p2 --</option><option value=\"1\">qui</option><option value=\"2\">quo</option><option value=\"3\">qua</option></select></fieldset></li></ul>",text);
    }

    public static class Bean {
        @Required
        public Integer p1;

        @Required
        public Integer p2;

        public Bean(Integer p1, Integer p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }
}
