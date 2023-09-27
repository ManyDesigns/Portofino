package com.manydesigns.portofino.dispatcher.security.jwt;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Created by alessio on 7/6/16.
 */
public class CookieJWTFilter extends JWTFilter {

    @Override
    protected String getToken(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Object mappedValue) {
        Cookie[] cookies = httpRequest.getCookies();
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if (getCookieName().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    public String getCookieName() {
        return StringUtils.defaultString(getInitParam("cookieName"), "jwt");
    }

}
