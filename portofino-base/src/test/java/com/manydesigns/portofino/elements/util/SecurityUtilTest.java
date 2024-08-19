package com.manydesigns.portofino.elements.util;

import com.manydesigns.elements.servlet.MutableHttpServletRequest;
import com.manydesigns.elements.stripes.ElementsActionBeanContext;
import com.manydesigns.elements.util.SecurityUtil;
import net.sourceforge.stripes.action.RedirectResolution;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.Assert.*;

public class SecurityUtilTest {


    @Test
    public void testException(){
        String returnUrl="/\t/google.com";
        int i=0;
        try {
            URL url = new URL(returnUrl);

        } catch (MalformedURLException e) {
            i++;
            e.printStackTrace();
            //Ok, if it is not a full URL there's no risk of XSS attacks with returnUrl=http://www.evil.com/hack
        }
        assertEquals(i, 0);
    }

    @Test
    public void testNoException(){
        String returnUrl="/login";
        int i=0;
        try {
            URL url = new URL(returnUrl);

        } catch (MalformedURLException e) {
            i++;
            e.printStackTrace();
            //Ok, if it is not a full URL there's no risk of XSS attacks with returnUrl=http://www.evil.com/hack
        }
        assertEquals(i, 0);
    }


    @Test
    public void testValidateRelativeUrl(){
        String returnUrl="/login";
        assertTrue(SecurityUtil.isValidUrl(null, returnUrl));
    }

    @Test
    public void testValidateRightAbsoluteUrl(){
        String returnUrl="http://localhost/login";

        ElementsActionBeanContext context=new ElementsActionBeanContext();
        MutableHttpServletRequest req=new MutableHttpServletRequest();
        req.setScheme("http");
        req.setServerName("localhost");
        req.setContextPath("login");
        req.setRequestURI(returnUrl);
        context.setRequest(req);
        assertTrue(SecurityUtil.isValidUrl(context, returnUrl));
    }

    @Test
    public void testValidateWrongAbsoluteUrl(){
        String returnUrl="http://fakesite.com/login";

        ElementsActionBeanContext context=new ElementsActionBeanContext();
        MutableHttpServletRequest req=new MutableHttpServletRequest();
        req.setScheme("http");
        req.setServerName("localhost");
        req.setContextPath("login");
        req.setRequestURI(returnUrl);
        context.setRequest(req);
        assertFalse(SecurityUtil.isValidUrl(context, returnUrl));
    }

    @Test
    public void testValidateWrongScheme(){
        String returnUrl="file://get/my/file";

        ElementsActionBeanContext context=new ElementsActionBeanContext();
        MutableHttpServletRequest req=new MutableHttpServletRequest();
        req.setScheme("http");
        req.setServerName("localhost");
        req.setContextPath("login");
        req.setRequestURI(returnUrl);
        context.setRequest(req);
        assertFalse(SecurityUtil.isValidUrl(context, returnUrl));
    }

    @Test
    public void testValidateMaliciousUrl1(){
        String returnUrl="/\t/google.com";
        assertFalse(SecurityUtil.isValidUrl(null, returnUrl));
    }
}
