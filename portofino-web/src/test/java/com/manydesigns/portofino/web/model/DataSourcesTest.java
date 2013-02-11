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

package com.manydesigns.portofino.web.model;

import com.manydesigns.portofino.CommonTestUtil;
import com.manydesigns.portofino.web.TestUtils;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletUnitClient;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.servlet.http.HttpServletResponse;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DataSourcesTest extends CommonTestUtil {
    public static Test suite() {
        return new TestSuite(DataSourcesTest.class);
    }

    public void testManageDataSource() throws Exception {
      System.out.println("testLinkHomePage:");

        ServletUnitClient client = servletRunner.newClient();

        TestUtils.login(client);

        String url = "http://127.0.0.1/model/ConnectionProviders.action";
        System.out.println(url);
        WebResponse resp = client.getResponse(url);
        String text = resp.getText();
        assertEquals("Codice risposta.",
                HttpServletResponse.SC_OK, resp.getResponseCode());

        //Controllo ci sia la tabella con le connessioni
        assertTrue(text.contains("<table><thead><tr><th>&nbsp;" +
                "</th><th>Database name</th><th>Description</th><th>Status</th></tr></thead><tbody><tr><td><input type=\"checkbox\" name=\"selection\" value=\"jpetstore\" /></td><td><div class=\"value\" id=\"row0_databaseName\"><a href=\"/model/ConnectionProviders.action?databaseName=jpetstore\">jpetstore</a></div></td><td><div class=\"value\" id=\"row0_description\">JDBC connection to URL: jdbc:h2:mem:jpetstore</div></td><td><div class=\"value status_green\" id=\"row0_status\">connected</div></td></tr><tr><td><input type=\"checkbox\" name=\"selection\" value=\"hibernatetest\" /></td><td><div class=\"value\" id=\"row1_databaseName\"><a href=\"/model/ConnectionProviders.action?databaseName=hibernatetest\">hibernatetest</a></div></td><td><div class=\"value\" id=\"row1_description\">JDBC connection to URL: jdbc:h2:mem:hibernatetest</div></td><td><div class=\"value status_green\" id=\"row1_status\">connected</div></td></tr><tr><td><input type=\"checkbox\" name=\"selection\" value=\"portofino\" /></td><td><div class=\"value\" id=\"row2_databaseName\"><a href=\"/model/ConnectionProviders.action?databaseName=portofino\">portofino</a></div></td><td><div class=\"value\" id=\"row2_description\">JDBC connection to URL: jdbc:h2:mem:portofino4</div></td><td><div class=\"value status_green\" id=\"row2_status\">connected</div></td></tr></tbody></table>"));
        WebForm form = resp.getForms()[0];
        assertEquals(4, form.getButtons().length);

        assertEquals("Create new", form.getButtons()[0].getValue());
        assertEquals("Delete", form.getButtons()[1].getValue());
        assertEquals("Create new", form.getButtons()[2].getValue());
        assertEquals("Delete", form.getButtons()[3].getValue());

        //**********************************************************************
        // 1. Testo creazione
        //**********************************************************************
        //Creo nuovo provider
        resp = form.submit((SubmitButton) form.getButtons()[0]);
        assertEquals("Codice risposta.",
                HttpServletResponse.SC_OK, resp.getResponseCode());
        text = resp.getText();
        assertTrue(text.contains("<div id=\"jdbc\" style=\"display:none\">\n" +
                "                <h2>Jdbc Connection Provider</h2>\n" +
                "                <fieldset class=\"nolegend\"><table class=\"details\"><tr><th><label for=\"jdbc_databaseName\" class=\"field\"><span class=\"required\">*</span>&nbsp;Database name:</label></th><td><input id=\"jdbc_databaseName\" type=\"text\" name=\"jdbc_databaseName\" class=\"text\" /></td></tr><tr><th><label for=\"jdbc_driver\" class=\"field\"><span class=\"required\">*</span>&nbsp;Driver:</label></th><td><input id=\"jdbc_driver\" type=\"text\" name=\"jdbc_driver\" class=\"text\" /></td></tr><tr><th><label for=\"jdbc_url\" class=\"field\"><span class=\"required\">*</span>&nbsp;Connection URL:</label></th><td><input id=\"jdbc_url\" type=\"text\" name=\"jdbc_url\" class=\"text\" /></td></tr><tr><th><label for=\"jdbc_username\" class=\"field\"><span class=\"required\">*</span>&nbsp;Username:</label></th><td><input id=\"jdbc_username\" type=\"text\" name=\"jdbc_username\" class=\"text\" /></td></tr><tr><th><label for=\"jdbc_password\" class=\"field\"><span class=\"required\">*</span>&nbsp;Password:</label></th><td><input type=\"password\" class=\"text\" id=\"jdbc_password\" name=\"jdbc_password\" /></td></tr><tr><th><label for=\"jdbc_includeSchemas\" class=\"field\"><span class=\"required\">*</span>&nbsp;Include schemas:</label></th><td><input id=\"jdbc_includeSchemas\" type=\"text\" name=\"jdbc_includeSchemas\" class=\"text\" /></td></tr><tr><th><label for=\"jdbc_excludeSchemas\" class=\"field\"><span class=\"required\">*</span>&nbsp;Exclude schemas:</label></th><td><input id=\"jdbc_excludeSchemas\" type=\"text\" name=\"jdbc_excludeSchemas\" class=\"text\" /></td></tr></table></fieldset>\n" +
                "            </div>\n" +
                "            <div id=\"jndi\" style=\"display:none\">\n" +
                "                <h2>Jndi Connection Provider</h2>\n" +
                "                <fieldset class=\"nolegend\"><table class=\"details\"><tr><th><label for=\"jndi_databaseName\" class=\"field\"><span class=\"required\">*</span>&nbsp;Database name:</label></th><td><input id=\"jndi_databaseName\" type=\"text\" name=\"jndi_databaseName\" class=\"text\" /></td></tr><tr><th><label for=\"jndi_jndiResource\" class=\"field\"><span class=\"required\">*</span>&nbsp;Jndi resource:</label></th><td><input id=\"jndi_jndiResource\" type=\"text\" name=\"jndi_jndiResource\" class=\"text\" /></td></tr><tr><th><label for=\"jndi_includeSchemas\" class=\"field\"><span class=\"required\">*</span>&nbsp;Include schemas:</label></th><td><input id=\"jndi_includeSchemas\" type=\"text\" name=\"jndi_includeSchemas\" class=\"text\" /></td></tr><tr><th><label for=\"jndi_excludeSchemas\" class=\"field\"><span class=\"required\">*</span>&nbsp;Exclude schemas:</label></th><td><input id=\"jndi_excludeSchemas\" type=\"text\" name=\"jndi_excludeSchemas\" class=\"text\" /></td></tr></table></fieldset>\n" +
                "            </div>\n" +
                "        </div>"));

        form = resp.getForms()[0];

        assertEquals("Save", form.getButtons()[0].getValue());
        assertEquals("cancel", form.getButtons()[1].getValue());
        assertEquals(4, form.getButtons().length);
        assertEquals(14, form.getParameterNames().length);

        form.setParameter("connectionType", "jdbc");
        form.setParameter("jdbc_driver", "org.postgresql.Driver");
        form.setParameter("jdbc_includeSchemas", "model");
        form.setParameter("jdbc_excludeSchemas", "meta");
        form.setParameter("jdbc_databaseName", "simon");
        form.setParameter("jdbc_username", "rer");
        form.setParameter("jdbc_password", "rer");
        form.setParameter("jdbc_url", "jdbc:postgresql://127.0.0.1:5432/simon");

        //Clicco su salva
        resp = form.submit((SubmitButton) form.getButtons()[0]);
        assertEquals("Codice risposta.",
                HttpServletResponse.SC_OK, resp.getResponseCode());
        text = resp.getText();
        assertTrue(text.contains("<tr><td><input type=\"checkbox\" name=\"selection\" value=\"simon\" /></td><td><div class=\"value\" id=\"row3_databaseName\"><a href=\"/model/ConnectionProviders.action?databaseName=simon\">simon</a></div></td><td><div class=\"value\" id=\"row3_description\">JDBC connection to URL: jdbc:postgresql://127.0.0.1:5432/simon</div></td><td><div class=\"value\" id=\"row3_status\"></div></td></tr>"));

        //**********************************************************************
        // 2. Testo modifica
        //**********************************************************************
        resp = resp.getLinkWith("simon").click();
        assertEquals("Codice risposta.",
                HttpServletResponse.SC_OK, resp.getResponseCode());
        text = resp.getText();
        //Passo ad edit
        form = resp.getForms()[0];
        resp = form.submit((SubmitButton) form.getButtons()[3]);
        assertEquals("Codice risposta.",
                HttpServletResponse.SC_OK, resp.getResponseCode());
        text = resp.getText();
        //Compilo i campi
        form = resp.getForms()[0];
        form.setParameter("url", "jdbc:postgresql://localhost:5432/simon");
        resp = form.submit((SubmitButton) form.getButtons()[0]);
        assertEquals("Codice risposta.",
                        HttpServletResponse.SC_OK, resp.getResponseCode());
        text = resp.getText();
        assertTrue(text.contains("<tr><td><input type=\"checkbox\" name=\"selection\" value=\"simon\" /></td><td><div class=\"value\" id=\"row3_databaseName\"><a href=\"/model/ConnectionProviders.action?databaseName=simon\">simon</a></div></td><td><div class=\"value\" id=\"row3_description\">JDBC connection to URL: jdbc:postgresql://localhost:5432/simon</div></td><td><div class=\"value\" id=\"row3_status\"></div></td></tr>"));


        //**********************************************************************
        // 3. Testo cancellazione
        //**********************************************************************
        form = resp.getForms()[0];
        form.setParameter("selection", "simon");
        resp = form.submit((SubmitButton) form.getButtons()[1]);
        assertEquals("Codice risposta.",
                HttpServletResponse.SC_OK, resp.getResponseCode());
        text = resp.getText();
        assertFalse(text.contains("<tr><td><input type=\"checkbox\" name=\"selection\" value=\"simon\" /></td><td><div class=\"value\" id=\"row3_databaseName\"><a href=\"/model/ConnectionProviders.action?databaseName=simon\">simon</a></div></td><td><div class=\"value\" id=\"row3_description\">JDBC connection to URL: jdbc:postgresql://127.0.0.1:5432/simon</div></td><td><div class=\"value\" id=\"row3_status\"></div></td></tr>"));
        assertTrue(text.contains("<div class=\"userMessages\"><ul class=\"infoMessages\"><li>Connection providers deleted</li></ul></div>"));

   }
}