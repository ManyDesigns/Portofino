package com.manydesigns.portofino.rest.messagebodywriters;

import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
public class XhtmlFragmentMessageBodyWriter implements MessageBodyWriter<XhtmlFragment> {

    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return XhtmlFragment.class.isAssignableFrom(type) && mediaType.isCompatible(MediaType.TEXT_HTML_TYPE);
    }

    @Override
    public long getSize(XhtmlFragment fragment, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(
            XhtmlFragment fragment, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream)
            throws IOException, WebApplicationException {
        OutputStreamWriter writer = new OutputStreamWriter(entityStream);
        XhtmlBuffer buffer = new XhtmlBuffer(writer);
        fragment.toXhtml(buffer);
        writer.flush();
    }
}
