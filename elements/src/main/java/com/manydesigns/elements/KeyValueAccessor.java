package com.manydesigns.elements;

import com.manydesigns.elements.xml.XhtmlFragment;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface KeyValueAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    Object get(String name);
    void set(String name, Object value);

}
