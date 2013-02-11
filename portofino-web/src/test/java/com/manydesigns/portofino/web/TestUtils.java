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

import com.manydesigns.portofino.web.model.DataSourcesTest;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletUnitClient;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TestUtils {


    public static void login(ServletUnitClient client) throws IOException, SAXException {
        String url = "http://127.0.0.1/user/Login.action";
        System.out.println(url);
        WebResponse resp = client.getResponse(url);
        String text = resp.getText();
        DataSourcesTest.assertEquals("Codice risposta.",
                HttpServletResponse.SC_OK, resp.getResponseCode());
        text = resp.getText();

        WebForm form = resp.getFormWithID("Login");
        DataSourcesTest.assertEquals(text, "Login", form
                .getSubmitButtonWithID("loginButton").getValue());

        form.setParameter("userName", "admin");
        form.setParameter("pwd", "admin");

        resp = form.submit();
    }
}