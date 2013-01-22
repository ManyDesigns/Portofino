/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.elements.forms;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.fields.search.SelectSearchField;
import com.manydesigns.elements.xml.XhtmlBuffer;

import java.io.StringWriter;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class SearchFormTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    private SearchForm form;
    private StringWriter writer = null;
    XhtmlBuffer buffer;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        writer = new StringWriter();
        SearchFormBuilder builder =
                new SearchFormBuilder(AnnotatedBean3.class);
        form = builder.build();
        buffer = new XhtmlBuffer(writer);
    }

    public void testForm1(){

        SelectSearchField field = (SelectSearchField) form.get(0);
        //Controllo l'html prodotto
        field.toXhtml(buffer);
        writer.flush();
        String result = writer.toString();
        assertEquals("<fieldset><legend class=\"attr_name\">Field1</legend>" +
                "<select id=\"field1\" name=\"field1\">" +
                "<option value=\"\" selected=\"selected\">-- Select field1 --" +
                "</option><option value=\"1\">a</option>" +
                "<option value=\"2\">b</option></select></fieldset>",
            result);

    }

    //Lettura corretta da request
    public void testForm2(){
        SelectSearchField field = (SelectSearchField) form.get(0);
        req.setParameter("field1", "1");
        field.readFromRequest(req);
        Object[] value = (Object[]) field.getSelectionModel().getValue(0);
        assertEquals("1", (String) value[0]);
        field.toXhtml(buffer);
        String result = writer.toString();
        assertEquals("<fieldset><legend class=\"attr_name\">Field1</legend>" +
                "<select id=\"field1\" name=\"field1\">" +
                "<option value=\"\">-- Select field1 --" +
                "</option><option value=\"1\" selected=\"selected\">a</option>" +
                "<option value=\"2\">b</option></select></fieldset>",
            result);
        StringBuilder sb = new StringBuilder();
        field.toSearchString(sb);
        result = sb.toString();
        assertEquals("field1=1", result);
    }

    //Lettura multipla da request con checkbox
    public void testForm2a(){

        SelectSearchField field = (SelectSearchField) form.get(4);
        String[] field5Values = {"1", "2"};
        req.setParameter("field5", field5Values);
        field.readFromRequest(req);
        Object[] value = (Object[]) field.getSelectionModel().getValue(0);
        assertEquals("1", (String) value[0]);
        field.toXhtml(buffer);
        String result = writer.toString();
        assertEquals("<fieldset><legend class=\"attr_name\">Field5</legend>" +
                "<input id=\"field5_0\" type=\"checkbox\" name=\"field5\" value=\"1\" checked=\"checked\" />" +
                "&nbsp;<label for=\"field5_0\">a</label><br />" +
                "<input id=\"field5_1\" type=\"checkbox\" name=\"field5\" value=\"2\" checked=\"checked\" />" +
                "&nbsp;<label for=\"field5_1\">b</label><br /></fieldset>",
            result);
        StringBuilder sb = new StringBuilder();
        field.toSearchString(sb);
        result = sb.toString();
        assertEquals("field5=1,field5=2", result);
    }

    //Lettura multipla da request con multipleselect
    public void testForm2b(){

        SelectSearchField field = (SelectSearchField) form.get(5);
        String[] field6Values = {"1", "2"};
        req.setParameter("field6", field6Values);
        field.readFromRequest(req);
        Object[] value = (Object[]) field.getSelectionModel().getValue(0);
        assertEquals("1", (String) value[0]);
        field.toXhtml(buffer);
        String result = writer.toString();
        assertEquals("<fieldset><legend class=\"attr_name\">Field6</legend>" +
                "<select id=\"field6\" name=\"field6\" multiple=\"multiple\" " +
                "size=\"5\"><option value=\"1\" selected=\"selected\">a" +
                "</option><option value=\"2\" selected=\"selected\">" +
                "b</option></select></fieldset>",
            result);
        StringBuilder sb = new StringBuilder();
        field.toSearchString(sb);
        result = sb.toString();
        assertEquals("field6=1,field6=2", result);
    }

    //Lettura dato non esitente da request
    public void testForm3(){

        SelectSearchField field = (SelectSearchField) form.get(0);
        req.setParameter("field1", "3");
        field.readFromRequest(req);
        field.toXhtml(buffer);
        String result = writer.toString();

        //Il dato non è nel modello
        assertNull(field.getSelectionModel().getValue(0));

        //Devo avere il campo vuoto selezionato
        assertEquals("<fieldset><legend class=\"attr_name\">Field1</legend>" +
                "<select id=\"field1\" name=\"field1\">" +
                "<option value=\"\" selected=\"selected\">-- Select field1 --" +
                "</option><option value=\"1\">a</option>" +
                "<option value=\"2\">b</option></select></fieldset>",
            result);

    }

    //Lettura da request vuota
    public void testForm4(){

        SelectSearchField field = (SelectSearchField) form.get(0);

        field.readFromRequest(req);
        field.toXhtml(buffer);
        String result = writer.toString();

        //Il dato non è nel modello
        assertNull(field.getSelectionModel().getValue(0));

        //Devo avere il campo vuoto selezionato
        assertEquals("<fieldset><legend class=\"attr_name\">Field1</legend>" +
                "<select id=\"field1\" name=\"field1\">" +
                "<option value=\"\" selected=\"selected\">-- Select field1 --" +
                "</option><option value=\"1\">a</option>" +
                "<option value=\"2\">b</option></select></fieldset>",
            result);


    }

    //testo il form builder aggiungendo un selection provider su field2
    public void testForm5(){
        DefaultSelectionProvider provider = new DefaultSelectionProvider("provider");
        provider.appendRow("v1", "ll", true);
        provider.appendRow("v2", "l2", true);
        provider.appendRow("v3", "l3", true);

        SearchFormBuilder builder =
            new SearchFormBuilder(AnnotatedBean3.class);
        builder.configSelectionProvider(provider, "field2");
        form = builder.build();
        SelectSearchField field = (SelectSearchField) form.get(1);

        //Controllo l'html prodotto
        field.toXhtml(buffer);
        writer.flush();
        String result = writer.toString();
        assertEquals("<fieldset><legend class=\"attr_name\">Field2</legend><select id=\"field2\" name=\"field2\"><option value=\"\" selected=\"selected\">-- Select field2 --</option><option value=\"v1\">ll</option><option value=\"v2\">l2</option><option value=\"v3\">l3</option></select></fieldset>",
            result);
    }

    //**************************************************************************
    // test DISPLAYMODE
    //**************************************************************************
    public void testForm6(){
        SelectSearchField field = (SelectSearchField) form.get(2);
        //Controllo l'html prodotto
        field.toXhtml(buffer);
        writer.flush();
        String result = writer.toString();
        assertEquals("<input type=\"hidden\" id=\"field3\" name=\"field3\" /><fieldset><legend class=\"attr_name\">Field3</legend><input id=\"field3_autocomplete\" type=\"text\" name=\"field3_autocomplete\" /><script type=\"text/javascript\">setupAutocomplete('#field3_autocomplete', 'field3', 0, '#field3');</script></fieldset>",
            result);
    }
    public void testForm7(){
        SelectSearchField field = (SelectSearchField) form.get(3);
        //Controllo l'html prodotto
        field.toXhtml(buffer);
        writer.flush();
        String result = writer.toString();
        assertEquals("<fieldset id=\"field4\" class=\"radio\"><legend class=\"attr_name\">Field4</legend><input type=\"radio\" id=\"field4_0\" name=\"field4\" value=\"\" checked=\"checked\" />&nbsp;<label for=\"field4_0\">None</label><br /><input type=\"radio\" id=\"field4_1\" name=\"field4\" value=\"1\" />&nbsp;<label for=\"field4_1\">a</label><br /><input type=\"radio\" id=\"field4_2\" name=\"field4\" value=\"2\" />&nbsp;<label for=\"field4_2\">b</label><br /></fieldset>",
            result);
    }
    public void testForm8(){

        SelectSearchField field = (SelectSearchField) form.get(4);
        //Controllo l'html prodotto
        field.toXhtml(buffer);
        writer.flush();
        String result = writer.toString();
        assertEquals("<fieldset><legend class=\"attr_name\">Field5</legend>" +
                "<input id=\"field5_0\" type=\"checkbox\" name=\"field5\" value=\"1\" />" +
                "&nbsp;<label for=\"field5_0\">a</label><br />" +
                "<input id=\"field5_1\" type=\"checkbox\" name=\"field5\" value=\"2\" />" +
                "&nbsp;<label for=\"field5_1\">b</label><br /></fieldset>",
            result);

    }
    public void testForm9(){

        SelectSearchField field = (SelectSearchField) form.get(5);
        //Controllo l'html prodotto
        field.toXhtml(buffer);
        writer.flush();
        String result = writer.toString();
        assertEquals("<fieldset>" +
                "<legend class=\"attr_name\">Field6</legend>" +
                "<select id=\"field6\" name=\"field6\" multiple=\"multiple\" size=\"5\">" +
                "<option value=\"1\">a</option><option value=\"2\">b</option>" +
                "</select></fieldset>",
            result);
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (writer!=null){
            writer.close();
        }
    }

    //testo il form builder aggiungendo un selection provider a cascata su field2
    public void testForm10(){
        DefaultSelectionProvider provider = new DefaultSelectionProvider("provider");
        provider.appendRow("v1", "ll", true);
        provider.appendRow("v2", "l2", true);
        provider.appendRow("v3", "l3", true);

        SearchFormBuilder builder =
            new SearchFormBuilder(AnnotatedBean3.class);
        builder.configSelectionProvider(provider, "field2");
        form = builder.build();
        SelectSearchField field = (SelectSearchField) form.get(1);

        //Controllo l'html prodotto
        field.toXhtml(buffer);
        writer.flush();
        String result = writer.toString();
        assertEquals("<fieldset><legend class=\"attr_name\">Field2</legend><select id=\"field2\" name=\"field2\"><option value=\"\" selected=\"selected\">-- Select field2 --</option><option value=\"v1\">ll</option><option value=\"v2\">l2</option><option value=\"v3\">l3</option></select></fieldset>",
            result);
    }
}
