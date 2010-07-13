/*
 * Copyright (C) 2005-2009 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements;

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
*/
public class DummyHttpServletRequest implements HttpServletRequest {
    public static final String copyright =
            "Copyright (c) 2005-2009, ManyDesigns srl";

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

    public void setParameter(String key, String value) {
        String[] values = {value};
        parameterMap.put(key, values);
    }

    public void setContextPath(String context)
    {
        this.contextPath=context;
    }


}
