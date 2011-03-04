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
*/
public class SearchFormTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

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
        try{
            SelectSearchField field = (SelectSearchField) form.get(0);
            //Controllo l'html prodotto
            field.toXhtml(buffer);
            writer.flush();
            String result = writer.toString();
            assertEquals("<fieldset><legend class=\"attr_name\">Field1</legend>" +
                    "<select id=\"field1\" name=\"field1\">" +
                    "<option value=\"\" selected=\"selected\">-- Select field1 --" +
                    "</option><option value=\"1\">a</option>" +
                    "<option value=\"2\">b</option></select>",
                result);
        }catch (Throwable e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    //Lettura corretta da request
    public void testForm2(){
        try{
            SelectSearchField field = (SelectSearchField) form.get(0);
            req.setParameter("field1", "1");
            field.readFromRequest(req);
            assertEquals("1", field.getSelectionModel().getValue(0));
            field.toXhtml(buffer);
            String result = writer.toString();
            assertEquals("<fieldset><legend class=\"attr_name\">Field1</legend>" +
                    "<select id=\"field1\" name=\"field1\">" +
                    "<option value=\"\">-- Select field1 --" +
                    "</option><option value=\"1\" selected=\"selected\">a</option>" +
                    "<option value=\"2\">b</option></select>",
                result);
            StringBuilder sb = new StringBuilder();
            field.toSearchString(sb);
            result = sb.toString();
            assertEquals("field1=1", result);
        }catch (Throwable e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    //Lettura dato non esitente da request
    public void testForm3(){
        try{
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
                    "<option value=\"2\">b</option></select>",
                result);
        }catch (Throwable e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    //Lettura da request vuota
    public void testForm4(){
        try{
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
                    "<option value=\"2\">b</option></select>",
                result);

        }catch (Throwable e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    //testo il form builder aggiungendo un selection provider su field2
    public void testForm5(){
        try{
            String[] values = {"v1", "v2", "v3"};
            String[] labels = {"ll", "l2", "l3"};
            DefaultSelectionProvider provider = DefaultSelectionProvider.create("provider", values, labels);

            SearchFormBuilder builder =
                new SearchFormBuilder(AnnotatedBean3.class);
            builder.configSelectionProvider(provider, "field2");
            form = builder.build();
            SelectSearchField field = (SelectSearchField) form.get(1);

            //Controllo l'html prodotto
            field.toXhtml(buffer);
            writer.flush();
            String result = writer.toString();
            assertEquals("<fieldset><legend class=\"attr_name\">Field2</legend>" +
                    "<select id=\"field2\" name=\"field2\"><option value=\"\" " +
                    "selected=\"selected\">-- Select field2 --</option><option " +
                    "value=\"v1\">ll</option><option value=\"v2\">l2</option>" +
                    "<option value=\"v3\">l3</option></select>",
                result);

        }catch (Throwable e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    //**************************************************************************
    // test DISPLAYMODE
    //**************************************************************************
    public void testForm6(){
        try{
            SelectSearchField field = (SelectSearchField) form.get(2);
            //Controllo l'html prodotto
            field.toXhtml(buffer);
            writer.flush();
            String result = writer.toString();
            assertEquals("<input type=\"hidden\" id=\"field3\" name=\"field3\" />" +
                    "<input id=\"field3_autocomplete\" type=\"text\" " +
                    "name=\"field3_autocomplete\" /><script " +
                    "type=\"text/javascript\">" +
                    "setupAutocomplete('#field3_autocomplete', 'field3', 0);</script>",
                result);
        }catch (Throwable e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    public void testForm7(){
        try{
            SelectSearchField field = (SelectSearchField) form.get(3);
            //Controllo l'html prodotto
            field.toXhtml(buffer);
            writer.flush();
            String result = writer.toString();
            assertEquals("<fieldset id=\"field4\" class=\"radio\"><input type=\"radio\"" +
                    " id=\"field4_0\" name=\"field4\" value=\"\" checked=\"checked\" />" +
                    "&nbsp;<label for=\"field4_0\">None</label><br /><input type=\"radio\"" +
                    " id=\"field4_1\" name=\"field4\" value=\"1\" />" +
                    "&nbsp;<label for=\"field4_1\">a</label><br />" +
                    "<input type=\"radio\" id=\"field4_2\" name=\"field4\" value=\"2\" />" +
                    "&nbsp;<label for=\"field4_2\">b</label><br /></fieldset>",
                result);
        }catch (Throwable e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (writer!=null){
            writer.close();
        }
    }
}
