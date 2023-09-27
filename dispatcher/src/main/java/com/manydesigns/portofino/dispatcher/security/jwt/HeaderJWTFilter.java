package com.manydesigns.portofino.dispatcher.security.jwt;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Created by alessio on 7/6/16.
 */
public class HeaderJWTFilter extends JWTFilter {

    @Override
    protected String getToken(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Object mappedValue) {
        return httpRequest.getHeader(getHeaderName());
    }
    
    public String getHeaderName() {
        return StringUtils.defaultString(getInitParam("headerName"), "jwt");
    }

}
