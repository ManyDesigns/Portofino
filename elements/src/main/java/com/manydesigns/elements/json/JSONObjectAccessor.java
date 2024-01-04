package com.manydesigns.elements.json;

import com.manydesigns.elements.KeyValueAccessor;
import org.json.JSONObject;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class JSONObjectAccessor implements KeyValueAccessor {

    private final JSONObject jsonObject;

    public JSONObjectAccessor(JSONObject jsonObject) {
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

    @Override
    public boolean has(String name) {
        return jsonObject.has(name);
    }

    @Override
    public KeyValueAccessor object(String name) {
        if (jsonObject.isNull(name)) {
            return null;
        }
        return new JSONObjectAccessor(jsonObject.getJSONObject(name));
    }

    @Override
    public KeyValueAccessor list(String name) {
        if (jsonObject.isNull(name)) {
            return null;
        }
        return new JSONArrayAccessor(jsonObject.getJSONArray(name));
    }

    @Override
    public KeyValueAccessor atIndex(int index) {
        throw new UnsupportedOperationException("Not a list");
    }

    @Override
    public int length() {
        return 0;
    }
}
