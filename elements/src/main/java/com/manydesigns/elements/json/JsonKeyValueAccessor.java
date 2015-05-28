package com.manydesigns.elements.json;

import com.manydesigns.elements.KeyValueAccessor;
import org.json.JSONObject;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class JsonKeyValueAccessor implements KeyValueAccessor {

    private final JSONObject jsonObject;

    public JsonKeyValueAccessor(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public Object get(String name) {
        if(jsonObject.has(name)) {
            Object object = jsonObject.get(name);
            return object == JSONObject.NULL ? null : object;
        } else {
            return null;
        }
    }

    @Override
    public void set(String name, Object value) {
        jsonObject.put(name, value);
    }
}
