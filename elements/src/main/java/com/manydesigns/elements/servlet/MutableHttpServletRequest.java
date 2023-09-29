/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.servlet;

import com.manydesigns.elements.blobs.FileBean;
import com.manydesigns.elements.blobs.FileUploadLimitExceededException;
import com.manydesigns.elements.blobs.MultipartWrapper;
import com.manydesigns.elements.blobs.StreamingCommonsMultipartWrapper;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.lang.ArrayUtils;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.*;

/**
 * Mock HTTP Servlet Request for testing.
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class MutableHttpServletRequest implements HttpServletRequest, MultipartWrapper {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    public final Map<String, Object> attributeMap;
    public final Map<String, String[]> parameterMap;
    public final Map<String, String[]> headerMap;
    public final Map<String, FileItem> fileItemMap;
    public final List<Locale> locales;

    private String method;
    private String contextPath = "";
    private String servletPath;
    private String requestURI;
    private String queryString;
    private String scheme;
    private String serverName;
    private int serverPort;

    private String contentType;
    private String characterEncoding = Charset.defaultCharset().name();

    private MutableServletContext servletContext;
    private MutableHttpSession session;

    //**************************************************************************
    // Constructor
    //**************************************************************************

    public MutableHttpServletRequest() {
        this(new MutableServletContext());
    }

    public MutableHttpServletRequest(MutableServletContext servletContext) {
        this.servletContext = servletContext;
        attributeMap = new HashMap<>();
        headerMap = new HashMap<>();
        parameterMap = new HashMap<>();
        fileItemMap = new HashMap<>();
        locales = new ArrayList<>();
        locales.add(Locale.getDefault());
    }

    //**************************************************************************
    // Methods
    //**************************************************************************

    public void addParameter(String name, String value) {
        String[] oldValues = parameterMap.get(name);
        String[] newValues = (String[]) ArrayUtils.add(oldValues, value);
        parameterMap.put(name, newValues);
    }

    public void setParameter(String key, String... values) {
        parameterMap.put(key, values);
    }

    public boolean addFileItem(String name, FileItem item) {
        FileItem inMap = fileItemMap.putIfAbsent(name, item);
        return inMap == item;
    }

    public FileItem getFileItem(String name) {
        return fileItemMap.get(name);
    }

    public void setFileItem(String key, FileItem value) {
        fileItemMap.put(key, value);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setContextPath(String context) {
        this.contextPath=context;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public void makeMultipart() {
        setMethod("POST");
        setContentType("multipart/form-data");
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    //**************************************************************************
    // HttpServletRequest implementation
    //**************************************************************************

    public String getParameter(String name) {
        String[] values = parameterMap.get(name);
        if (values == null || values.length == 0) {
            return null;
        } else {
            return values[0];
        }
    }

    @Override
    public void build(HttpServletRequest request, File tempDir, long maxPostSize) {}

    public Enumeration getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    @Override
    public Enumeration<String> getFileParameterNames() {
        return Collections.enumeration(fileItemMap.keySet());
    }

    @Override
    public FileBean getFileParameterValue(String name) {
        final FileItem item = this.fileItemMap.get(name);

        if (item == null
                || ((item.getName() == null || item.getName().length() == 0) && item.getSize() == 0)) {
            return null;
        }
        else {
            String filename = item.getName();
            return new FileBean(null, item.getContentType(), filename, getCharacterEncoding()) {
                @Override
                public long getSize() {
                    return item.getSize();
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return item.getInputStream();
                }

                @Override
                public void delete() throws IOException {
                    item.delete();
                }
            };
        }
    }

    @Override
    public HttpServletRequestWrapper wrapRequest(HttpServletRequest request) {
        return null;
    }

    public Map getParameterMap() {
        return parameterMap;
    }

    public String getMethod() {
        return method;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getServletPath() {
        return servletPath;
    }

    public Object getAttribute(String s) {
        return attributeMap.get(s);
    }

    public void setAttribute(String s, Object o) {
        attributeMap.put(s, o);
    }

    public void removeAttribute(String s) {
        attributeMap.remove(s);
    }

    //**************************************************************************
    // Unimplemented HttpServletRequest methods
    //**************************************************************************

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
        String[] values = headerMap.get(s);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    public Enumeration<String> getHeaders(String s) {
        String[] values = headerMap.get(s);
        if (values == null) {
            return null;
        }
        return Collections.enumeration(Arrays.asList(values));
    }

    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headerMap.keySet());
    }

    public int getIntHeader(String s) {
        throw new UnsupportedOperationException();
    }

    public String getPathInfo() {
        return null; //TODO
    }

    public String getPathTranslated() {
        throw new UnsupportedOperationException();
    }

    public String getQueryString() {
        return queryString;
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
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public StringBuffer getRequestURL() {
        throw new UnsupportedOperationException();
    }

    public HttpSession getSession(boolean create) {
        if(create) {
            synchronized (this) {
                if(session == null) {
                    session = new MutableHttpSession(servletContext);
                }
            }
        }
        return session;
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    public String changeSessionId() {
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

    public Enumeration getAttributeNames() {
        return Collections.enumeration(attributeMap.keySet());
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(String s) {
        characterEncoding = s;
    }

    public int getContentLength() {
        return -1; //Not known
    }

    public long getContentLengthLong() {
        return getContentLength();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    public String getProtocol() {
        throw new UnsupportedOperationException();
    }

    public String getScheme() {
        return scheme;
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
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

    public Locale getLocale() {
        return locales.get(0);
    }

    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(locales);
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

    //Servlet API 3.0 methods
    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s1) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutableServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest request, ServletResponse response) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    //Servlet API 3.1 methods
    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass) throws IOException, ServletException {
        return null;
    }

    //Servlet API 6.0 methods

    @Override
    public String getRequestId() {
        return null;
    }

    @Override
    public String getProtocolRequestId() {
        return null;
    }

    @Override
    public ServletConnection getServletConnection() {
        return null;
    }
}
