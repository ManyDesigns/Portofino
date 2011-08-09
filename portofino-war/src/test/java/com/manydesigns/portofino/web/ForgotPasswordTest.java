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