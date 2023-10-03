package com.manydesigns.portofino.rest;

import com.manydesigns.elements.KeyValueAccessor;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Objects;

public class FormParametersAccessor implements KeyValueAccessor {

    protected final MultivaluedMap<String, String> formParameters;

    public FormParametersAccessor(MultivaluedMap<String, String> formParameters) {
        this.formParameters = formParameters;
    }

    @Override
    public Object get(String name) {
        return formParameters.getFirst(name);
    }

    @Override
    public void set(String name, Object value) {
        formParameters.putSingle(name, Objects.toString(value, null));
    }

    @Override
    public boolean has(String name) {
        return formParameters.containsKey(name);
    }

    @Override
    public KeyValueAccessor inner(Object value) {
        throw new UnsupportedOperationException();
    }
}
