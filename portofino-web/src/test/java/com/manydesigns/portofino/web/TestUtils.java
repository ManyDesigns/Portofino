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