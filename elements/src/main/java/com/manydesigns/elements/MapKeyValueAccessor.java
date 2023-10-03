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
    public KeyValueAccessor inner(Object value) {
        return new MapKeyValueAccessor((Map<String, Object>) value);
    }
}
