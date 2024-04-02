package com.manydesigns.portofino.pageactions.rest.messagebodywriters;

import com.manydesigns.elements.ElementsThreadLocals;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.StripesRequestWrapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import net.sourceforge.stripes.exception.StripesServletException;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Provider
@ConstrainedTo(RuntimeType.SERVER)
public class StripesMessageBodyWriter implements MessageBodyWriter<Resolution> {

    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Resolution.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Resolution resolution, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(
            Resolution resolution, Class<?> type, Type genericType, Annotation[] annotations, final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream)
            throws IOException, WebApplicationException {
        // OutputStream and Writer for HttpServletResponseWrapper.
        final ServletOutputStream responseStream = new ServletOutputStream() {
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener listener) {}

            @Override
            public void write(final int b) throws IOException {
                entityStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                entityStream.write(b, off, len);
            }

            @Override
            public void write(byte[] b) throws IOException {
                entityStream.write(b);
            }

            @Override
            public void flush() throws IOException {
                entityStream.flush();
            }

            @Override
            public void close() throws IOException {
                entityStream.close();
            }
        };
        final PrintWriter responseWriter = new PrintWriter(new OutputStreamWriter(entityStream, "UTF-8")); //TODO
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        HttpServletResponse response = ElementsThreadLocals.getHttpServletResponse();
        try {
            try {
                request = StripesRequestWrapper.findStripesWrapper(request);
            } catch (IllegalStateException e) {
                request = new StripesRequestWrapper(request);
            }
            resolution.execute(request, new HttpServletResponseWrapper(response) {

                @Override
                public ServletOutputStream getOutputStream() throws IOException {
                    return responseStream;
                }

                @Override
                public PrintWriter getWriter() throws IOException {
                    return responseWriter;
                }

                @Override
                public void addHeader(String name, String value) {
                    httpHeaders.add(name, value);
                }

                @Override
                public void setHeader(String name, String value) {
                    httpHeaders.putSingle(name, value);
                }

                @Override
                public String getContentType() {
                    return mediaType.toString();
                }
            });
            responseWriter.flush();
            responseStream.flush();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}
