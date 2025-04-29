package com.manydesigns.elements.util;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.RedirectResolution;
import org.apache.commons.validator.routines.UrlValidator;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class SecurityUtil {

    public static boolean isValidUrl(ActionBeanContext context, String strUrl) {
        String[] schemes = new String[]{"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes);

        if (urlValidator.isValid(strUrl)) {
            return false;
        }

        try {
            URI uri= new URI(strUrl);
            if( uri.isAbsolute() ){
                URL url = new URL(strUrl);
                return  isSameHost(context,url);
            }
            return true;
        } catch (MalformedURLException e) {
            //Ok, if it is not a full URL there's no risk of XSS attacks with returnUrl=http://www.evil.com/hack
            return false;
        }catch (Exception general){
            return false;
        }

    }

    public static Boolean isSameHost(ActionBeanContext context, URL url){
        return context.getRequest().getServerName().equals(url.getHost());
    }

}
