package com.manydesigns.elements;

import java.util.Map;

public class MapKeyValueAccessor implements KeyValueAccessor {

    protected final Map<String, Object> map;

    public MapKeyValueAccessor(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public Object get(String name) {
        return map.get(name);
    }

    @Override
    public void set(String name, Object value) {
        map.put(name, value);
    }

    @Override
    public boolean has(String name) {
        return map.containsKey(name);
    }

    @Override
    public KeyValueAccessor object(String name) {
        Map<String, Object> inner = (Map<String, Object>) get(name);
        return inner != null ? new MapKeyValueAccessor(inner) : null;
    }

    @Override
    public KeyValueAccessor list(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KeyValueAccessor atIndex(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int length() {
        return 0;
    }
}
