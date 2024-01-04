package com.manydesigns.elements;

import com.manydesigns.elements.json.JSONObjectAccessor;

public abstract class KeyValueListAccessor implements KeyValueAccessor {
    protected KeyValueAccessor currentObjectAccessor;

    @Override
    public Object get(String name) {
        return currentObjectAccessor != null ? currentObjectAccessor.get(name) : null;
    }

    @Override
    public void set(String name, Object value) {
        if (currentObjectAccessor != null) {
            currentObjectAccessor.set(name, value);
        } else {
            throw new IllegalStateException("No element in the list");
        }
    }

    @Override
    public boolean has(String name) {
        return currentObjectAccessor != null && currentObjectAccessor.has(name);
    }

    @Override
    public KeyValueAccessor object(String name) {
        return currentObjectAccessor != null ? currentObjectAccessor.object(name) : null;
    }

    @Override
    public KeyValueAccessor list(String name) {
        return currentObjectAccessor != null ? currentObjectAccessor.list(name) : null;
    }
}
