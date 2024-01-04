package com.manydesigns.elements;

/**
 * Abstraction over key-value data for Elements forms (e.g. JSON objects, HTTP requests, etc.)
 *
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface KeyValueAccessor {
    String copyright = "Copyright (C) 2005-2024 ManyDesigns srl";

    Object get(String name);
    void set(String name, Object value);
    boolean has(String name);

    KeyValueAccessor object(String name);

    KeyValueAccessor list(String name);

    KeyValueAccessor atIndex(int index);

    int length();
}
