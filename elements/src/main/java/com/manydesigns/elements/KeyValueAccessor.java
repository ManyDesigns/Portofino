package com.manydesigns.elements;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface KeyValueAccessor {
    public static final String copyright =
            "Copyright (C) 2005-2021 ManyDesigns srl";

    Object get(String name);
    void set(String name, Object value);
    boolean has(String name);
}
