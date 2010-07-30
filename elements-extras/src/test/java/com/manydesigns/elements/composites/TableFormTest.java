/*
 * Copyright (C) 2005-2009 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.composites;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;

import java.util.Arrays;
import java.util.Collection;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableFormTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2009, ManyDesigns srl";

    public Bean1 bean;
    public TableForm tableForm;
    public TableForm tableFormWithPrefix;
    public TableForm tableFormWithFieldNames;

    public Bean1[] beanArray1 = {
            new Bean1(1, "qui", true),
            new Bean1(2, "quo", true),
            new Bean1(3, "qua", false),
    };

    public Bean1[] beanArray2 = {
            new Bean1(),
            new Bean1(),
    };

    @Override
    public void setUp() throws Exception {
        super.setUp();

        TableFormBuilder builder =
                new TableFormBuilder(Bean1.class);
        builder.configNRows(4);

        tableForm = builder.build();

        builder.configPrefix("prova.");
        tableFormWithPrefix = builder.build();

        builder.configPrefix(null);
        builder.configFields("id", "nome");
        tableFormWithFieldNames = builder.build();

    }

    public void testNullPrefix() {
        assertEquals(4, tableForm.getNRows());
        assertNull(tableForm.getId());
        String text = elementToString(tableForm);
        assertEquals("<table><thead><tr><th>Id</th><th>Nome</th><th>Attivo</th>" +
                "</tr></thead><tbody>" +
                "<tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row0.id\" name=\"row0.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row0.nome\" name=\"row0.nome\"></input></td>" +
                "<td><input id=\"row0.attivo\" type=\"checkbox\" name=\"row0.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"row0.attivo_chk\" value=\"\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row1.id\" name=\"row1.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row1.nome\" name=\"row1.nome\"></input></td>" +
                "<td><input id=\"row1.attivo\" type=\"checkbox\" name=\"row1.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"row1.attivo_chk\" value=\"\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row2.id\" name=\"row2.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row2.nome\" name=\"row2.nome\"></input></td>" +
                "<td><input id=\"row2.attivo\" type=\"checkbox\" name=\"row2.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"row2.attivo_chk\" value=\"\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row3.id\" name=\"row3.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row3.nome\" name=\"row3.nome\"></input></td>" +
                "<td><input id=\"row3.attivo\" type=\"checkbox\" name=\"row3.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"row3.attivo_chk\" value=\"\"></input></td>" +
                "</tr>" +
                "</tbody></table>", text);
    }

    public void testWithPrefix() {
        assertEquals(4, tableFormWithPrefix.getNRows());
        assertNull(tableFormWithPrefix.getId());
        String text = elementToString(tableFormWithPrefix);
        assertEquals("<table><thead><tr><th>Id</th><th>Nome</th><th>Attivo</th>" +
                "</tr></thead><tbody>" +
                "<tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"prova.row0.id\" name=\"prova.row0.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"prova.row0.nome\" name=\"prova.row0.nome\"></input></td>" +
                "<td><input id=\"prova.row0.attivo\" type=\"checkbox\" name=\"prova.row0.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"prova.row0.attivo_chk\" value=\"\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"prova.row1.id\" name=\"prova.row1.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"prova.row1.nome\" name=\"prova.row1.nome\"></input></td>" +
                "<td><input id=\"prova.row1.attivo\" type=\"checkbox\" name=\"prova.row1.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"prova.row1.attivo_chk\" value=\"\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"prova.row2.id\" name=\"prova.row2.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"prova.row2.nome\" name=\"prova.row2.nome\"></input></td>" +
                "<td><input id=\"prova.row2.attivo\" type=\"checkbox\" name=\"prova.row2.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"prova.row2.attivo_chk\" value=\"\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"prova.row3.id\" name=\"prova.row3.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"prova.row3.nome\" name=\"prova.row3.nome\"></input></td>" +
                "<td><input id=\"prova.row3.attivo\" type=\"checkbox\" name=\"prova.row3.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"prova.row3.attivo_chk\" value=\"\"></input></td>" +
                "</tr>" +
                "</tbody></table>", text);
    }

    public void testWithFieldNames() {
        assertEquals(4, tableFormWithFieldNames.getNRows());
        assertNull(tableFormWithFieldNames.getId());
        String text = elementToString(tableFormWithFieldNames);
        assertEquals("<table><thead><tr><th>Id</th><th>Nome</th>" +
                "</tr></thead><tbody>" +
                "<tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row0.id\" name=\"row0.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row0.nome\" name=\"row0.nome\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row1.id\" name=\"row1.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row1.nome\" name=\"row1.nome\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row2.id\" name=\"row2.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row2.nome\" name=\"row2.nome\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row3.id\" name=\"row3.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row3.nome\" name=\"row3.nome\"></input></td>" +
                "</tr>" +
                "</tbody></table>", text);
    }

    public void testReadFromObjectArray() {
        tableForm.readFromObject(beanArray1);
        commonReadFromObject();
    }

    public void testReadFromObjectCollection() {
        Collection collection = Arrays.asList(beanArray1);
        tableForm.readFromObject(collection);
        commonReadFromObject();

    }

    private void commonReadFromObject() {
        String text = elementToString(tableForm);
        assertEquals("<table><thead><tr><th>Id</th><th>Nome</th><th>Attivo</th>" +
                "</tr></thead><tbody>" +
                "<tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row0.id\" name=\"row0.id\" value=\"1\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row0.nome\" name=\"row0.nome\" value=\"qui\"></input></td>" +
                "<td><input id=\"row0.attivo\" type=\"checkbox\" name=\"row0.attivo\" value=\"checked\" checked=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"row0.attivo_chk\" value=\"\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row1.id\" name=\"row1.id\" value=\"2\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row1.nome\" name=\"row1.nome\" value=\"quo\"></input></td>" +
                "<td><input id=\"row1.attivo\" type=\"checkbox\" name=\"row1.attivo\" value=\"checked\" checked=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"row1.attivo_chk\" value=\"\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row2.id\" name=\"row2.id\" value=\"3\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row2.nome\" name=\"row2.nome\" value=\"qua\"></input></td>" +
                "<td><input id=\"row2.attivo\" type=\"checkbox\" name=\"row2.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"row2.attivo_chk\" value=\"\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row3.id\" name=\"row3.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row3.nome\" name=\"row3.nome\"></input></td>" +
                "<td><input id=\"row3.attivo\" type=\"checkbox\" name=\"row3.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"row3.attivo_chk\" value=\"\"></input></td>" +
                "</tr>" +
                "</tbody></table>", text);
    }

    public void testReadFromRequestAndWriteToObject() {
        req.setParameter("row0.id", "10");
        req.setParameter("row0.nome", "bla");
        req.setParameter("row0.attivo_chk", "");
        req.setParameter("row1.id", "11");
        req.setParameter("row1.nome", "blabla");
        req.setParameter("row1.attivo", "checked");
        req.setParameter("row1.attivo_chk", "");
        tableForm.readFromRequest(req);
        String text = elementToString(tableForm);
        assertEquals("<table><thead><tr><th>Id</th><th>Nome</th><th>Attivo</th>" +
                "</tr></thead><tbody>" +
                "<tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row0.id\" name=\"row0.id\" value=\"10\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row0.nome\" name=\"row0.nome\" value=\"bla\"></input></td>" +
                "<td><input id=\"row0.attivo\" type=\"checkbox\" name=\"row0.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"row0.attivo_chk\" value=\"\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row1.id\" name=\"row1.id\" value=\"11\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row1.nome\" name=\"row1.nome\" value=\"blabla\"></input></td>" +
                "<td><input id=\"row1.attivo\" type=\"checkbox\" name=\"row1.attivo\" value=\"checked\" checked=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"row1.attivo_chk\" value=\"\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row2.id\" name=\"row2.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row2.nome\" name=\"row2.nome\"></input></td>" +
                "<td><input id=\"row2.attivo\" type=\"checkbox\" name=\"row2.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"row2.attivo_chk\" value=\"\"></input></td>" +
                "</tr><tr>" +
                "<td><input type=\"text\" class=\"text\" id=\"row3.id\" name=\"row3.id\"></input></td>" +
                "<td><input type=\"text\" class=\"text\" id=\"row3.nome\" name=\"row3.nome\"></input></td>" +
                "<td><input id=\"row3.attivo\" type=\"checkbox\" name=\"row3.attivo\" value=\"checked\" class=\"checkbox\"></input><input type=\"hidden\" name=\"row3.attivo_chk\" value=\"\"></input></td>" +
                "</tr>" +
                "</tbody></table>", text);

        // parte writeToObject()
        tableForm.writeToObject(beanArray2);
        assertEquals(2, beanArray2.length);

        Bean1 current = beanArray2[0];
        assertNotNull(current);
        assertEquals(10, current.id);
        assertEquals("bla", current.nome);
        assertFalse(current.attivo);

        current = beanArray2[1];
        assertNotNull(current);
        assertEquals(11, current.id);
        assertEquals("blabla", current.nome);
        assertTrue(current.attivo);
    }

    public void testLinks() {
        tableForm.readFromObject(beanArray1);
        tableForm.setMode(Mode.VIEW);
        String text = elementToString(tableForm);
        assertEquals("<table><thead><tr><th>Id</th><th>Nome</th><th>Attivo</th>" +
                "</tr></thead><tbody>" +
                "<tr>" +
                "<td><div class=\"value\" id=\"row0.id\">1</div></td>" +
                "<td><div class=\"value\" id=\"row0.nome\">qui</div></td>" +
                "<td><div class=\"value\" id=\"row0.attivo\">Yes</div></td>" +
                "</tr><tr>" +
                "<td><div class=\"value\" id=\"row1.id\">2</div></td>" +
                "<td><div class=\"value\" id=\"row1.nome\">quo</div></td>" +
                "<td><div class=\"value\" id=\"row1.attivo\">Yes</div></td>" +
                "</tr><tr>" +
                "<td><div class=\"value\" id=\"row2.id\">3</div></td>" +
                "<td><div class=\"value\" id=\"row2.nome\">qua</div></td>" +
                "<td><div class=\"value\" id=\"row2.attivo\">No</div></td>" +
                "</tr><tr>" +
                "<td><div class=\"value\" id=\"row3.id\"></div></td>" +
                "<td><div class=\"value\" id=\"row3.nome\"></div></td>" +
                "<td><div class=\"value\" id=\"row3.attivo\">No</div></td>" +
                "</tr>" +
                "</tbody></table>", text);
    }

}
