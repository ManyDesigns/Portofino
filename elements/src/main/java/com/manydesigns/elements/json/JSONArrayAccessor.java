package com.manydesigns.elements.json;

import com.manydesigns.elements.KeyValueListAccessor;
import org.json.JSONArray;

public class JSONArrayAccessor extends KeyValueListAccessor {

    private final JSONArray jsonArray;

    public JSONArrayAccessor(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
        if (!jsonArray.isEmpty()) {
            currentObjectAccessor = new JSONObjectAccessor(jsonArray.getJSONObject(0));
        }
    }

    protected JSONArrayAccessor(JSONArray jsonArray, JSONObjectAccessor currentAccessor) {
        this.jsonArray = jsonArray;
        this.currentObjectAccessor = currentAccessor;
    }

    @Override
    public KeyValueListAccessor atIndex(int index) {
        return new JSONArrayAccessor(jsonArray, new JSONObjectAccessor(jsonArray.getJSONObject(index)));
    }

    @Override
    public int length() {
        return jsonArray.length();
    }
}
