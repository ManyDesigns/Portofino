/*
* Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MutableHttpServletResponse implements HttpServletResponse {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";


    public final static Logger logger =
            LoggerFactory.getLogger(MutableHttpServletResponse.class);

    final ServletOutputStream outputStream;
    PrintWriter writer;
    String characterEncoding;
    int contentLength;
    int status;
    String statusMessage;
    String contentType;
    Locale locale;

    public MutableHttpServletResponse(ServletOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeURL(String s) {
        return s;
    }

    @Override
    public String encodeRedirectURL(String s) {
        return s;
    }

    @Override
    public String encodeUrl(String s) {
        return encodeURL(s);
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return encodeRedirectURL(s);
    }

    @Override
    public void sendError(int i, String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int i) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendRedirect(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDateHeader(String s, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addDateHeader(String s, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addHeader(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIntHeader(String s, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addIntHeader(String s, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatus(int i) {
        logger.debug("Setting status to: {}", i);
        status = i;
    }

    @Override
    public void setStatus(int i, String s) {
        logger.debug("Setting status and message to: {} - {}", i, s);
        status = i;
        statusMessage = s;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaders(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaderNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            OutputStreamWriter osw;
            if (characterEncoding == null) {
                logger.debug("Creating writer with default encoding");
                osw = new OutputStreamWriter(outputStream);
            } else {
                logger.debug("Creating writer with encoding: {}", characterEncoding);
                osw = new OutputStreamWriter(outputStream, characterEncoding);
            }
            writer = new PrintWriter(osw);
        }
        return writer;
    }

    @Override
    public void setCharacterEncoding(String s) {
        logger.debug("Setting encoding to: {}", s);
        characterEncoding = s;
    }

    @Override
    public void setContentLength(int i) {
        logger.debug("Setting content length to: {}", i);
        contentLength = i;
    }

    @Override
    public void setContentType(String s) {
        logger.debug("Setting content type to: {}", s);
        contentType = s;
    }

    @Override
    public void setBufferSize(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBufferSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer == null) {
            logger.debug("Flushing output stream");
            outputStream.flush();
        } else {
            logger.debug("Flushing writer");
            writer.flush();
        }
    }

    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCommitted() {
        return true;
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocale(Locale locale) {
        logger.debug("Setting locale to: {}", locale);
        this.locale = locale;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }
}
