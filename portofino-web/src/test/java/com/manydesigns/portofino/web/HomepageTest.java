/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.manydesigns.portofino.web;

import com.manydesigns.portofino.CommonTestUtil;
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
public class HomepageTest extends CommonTestUtil {

    public static Test suite() {
        return new TestSuite(HomepageTest.class);
    }

    public void testLinkHomePage() throws Exception {
      System.out.println("testLinkHomePage:");

        ServletUnitClient client = servletRunner.newClient();

        String url = "http://127.0.0.1/Document.action";
        System.out.println(url);
        WebResponse resp = client.getResponse(url);
        assertEquals("Codice risposta.",
               HttpServletResponse.SC_OK, resp.getResponseCode());

    }
}