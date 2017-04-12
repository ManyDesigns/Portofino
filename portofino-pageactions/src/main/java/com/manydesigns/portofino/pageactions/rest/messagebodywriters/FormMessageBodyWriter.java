package com.manydesigns.portofino.pageactions.rest.messagebodywriters;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.util.FormUtil;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
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
public class FormMessageBodyWriter implements MessageBodyWriter<Form> {

    public static final String copyright =
            "Copyright (C) 2005-2017 ManyDesigns srl";

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Form.class.isAssignableFrom(type) && mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public long getSize(Form form, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(
            Form form, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream)
            throws IOException, WebApplicationException {
        String json = FormUtil.writeToJson(form);
        entityStream.write(json.getBytes());
    }
}
