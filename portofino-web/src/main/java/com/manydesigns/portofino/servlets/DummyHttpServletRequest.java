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

package com.manydesigns.portofino.servlets;

import org.apache.commons.lang.ArrayUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DummyHttpServletRequest implements HttpServletRequest {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    private String method;
    private final Map<String, String[]> parameterMap;
    private String contextPath;

    public DummyHttpServletRequest() {
        parameterMap = new HashMap<String, String[]>();
    }

    public String getAuthType() {
        throw new UnsupportedOperationException();
    }

    public Cookie[] getCookies() {
        throw new UnsupportedOperationException();
    }

    public long getDateHeader(String s) {
        throw new UnsupportedOperationException();
    }

    public String getHeader(String s) {
        throw new UnsupportedOperationException();
    }

    public Enumeration getHeaders(String s) {
        throw new UnsupportedOperationException();
    }

    public Enumeration getHeaderNames() {
        throw new UnsupportedOperationException();
    }

    public int getIntHeader(String s) {
        throw new UnsupportedOperationException();
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public String getPathInfo() {
        throw new UnsupportedOperationException();
    }

    public String getPathTranslated() {
        throw new UnsupportedOperationException();
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getQueryString() {
        throw new UnsupportedOperationException();
    }

    public String getRemoteUser() {
        throw new UnsupportedOperationException();
    }

    public boolean isUserInRole(String s) {
        throw new UnsupportedOperationException();
    }

    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException();
    }

    public String getRequestedSessionId() {
        throw new UnsupportedOperationException();
    }

    public String getRequestURI() {
        throw new UnsupportedOperationException();
    }

    public StringBuffer getRequestURL() {
        throw new UnsupportedOperationException();
    }

    public String getServletPath() {
        throw new UnsupportedOperationException();
    }

    public HttpSession getSession(boolean b) {
        throw new UnsupportedOperationException();
    }

    public HttpSession getSession() {
        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException();
    }

    public Object getAttribute(String s) {
        throw new UnsupportedOperationException();
    }

    public Enumeration getAttributeNames() {
        throw new UnsupportedOperationException();
    }

    public String getCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException();
    }

    public int getContentLength() {
        throw new UnsupportedOperationException();
    }

    public String getContentType() {
        throw new UnsupportedOperationException();
    }

    public ServletInputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getParameter(String key) {
        String[] values = getParameterValues(key);
        if (values == null || values.length < 1) {
            return null;
        }
        return values[0];
    }

    public Enumeration getParameterNames() {
        throw new UnsupportedOperationException();
    }

    public String[] getParameterValues(String key) {
        return parameterMap.get(key);
    }

    public Map getParameterMap() {
        return parameterMap;
    }

    public String getProtocol() {
        throw new UnsupportedOperationException();
    }

    public String getScheme() {
        throw new UnsupportedOperationException();
    }

    public String getServerName() {
        throw new UnsupportedOperationException();
    }

    public int getServerPort() {
        throw new UnsupportedOperationException();
    }

    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getRemoteAddr() {
        throw new UnsupportedOperationException();
    }

    public String getRemoteHost() {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(String s, Object o) {
        throw new UnsupportedOperationException();
    }

    public void removeAttribute(String s) {
        throw new UnsupportedOperationException();
    }

    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    public Enumeration getLocales() {
        throw new UnsupportedOperationException();
    }

    public boolean isSecure() {
        throw new UnsupportedOperationException();
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public String getRealPath(String s) {
        throw new UnsupportedOperationException();
    }

    public int getRemotePort() {
        throw new UnsupportedOperationException();
    }

    public String getLocalName() {
        throw new UnsupportedOperationException();
    }

    public String getLocalAddr() {
        throw new UnsupportedOperationException();
    }

    public int getLocalPort() {
        throw new UnsupportedOperationException();
    }

    public void addParameter(String key, String value) {
        if(parameterMap.get(key)!=null){
            String[] values = parameterMap.remove(key);
            values = (String[]) ArrayUtils.add(values, value);
            parameterMap.put(key, values);
        } else {
            String[] values = {value};
            parameterMap.put(key, values);
        }
    }

    public void setParameter(String key, String value) {
        String[] values = {value};
        parameterMap.put(key, values);
    }

    public void setContextPath(String context)
    {
        this.contextPath=context;
    }
}
