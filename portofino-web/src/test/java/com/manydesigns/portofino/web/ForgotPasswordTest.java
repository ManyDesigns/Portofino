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

package com.manydesigns.portofino.web;

import com.manydesigns.portofino.CommonTestUtil;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebForm;
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
public class ForgotPasswordTest extends CommonTestUtil {

    public static Test suite() {
        return new TestSuite(ForgotPasswordTest.class);
    }

    public void testLogin1() throws Exception {
      System.out.println("test forgot password:");

        ServletUnitClient client = servletRunner.newClient();

        String url = "http://127.0.0.1/Document.action";
        System.out.println(url);
        WebResponse resp = client.getResponse(url);
        String text = resp.getText();
        //Controllo il menu deve avere solo la Homepage
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());
        assertTrue(text.contains("        <div id=\"sidebar\" class=\"yui-b\">\n" +
                "            <ul><li class=\"selected\"><a href=\"/Document.action\" title=\"Homepage\">Homepage</a></li></ul>\n" +
                "        </div>\n" +
                "    </div>\n"));
        assertEquals("Homepage", resp.getElementWithID("sidebar").getText());

        //Clicco su login
        WebLink login = resp.getLinkWith("Log in");
        resp = login.click();
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());
        text = resp.getText();

        WebForm form = resp.getFormWithID("Login");
        assertEquals (text, "Login", form
                .getSubmitButtonWithID("loginButton").getValue());

        WebLink link = resp.getLinkWith("recupera password");
        resp = link.click();
        text = resp.getText();
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());
        text = resp.getText();

        assertTrue(text, text.contains("Inserisci l'email del tuo account.<br/>\n" +
                "            Ti verranno inviate le istruzioni su come modificare la tua password</p>"));
    }

}