/*
* Copyright (C) 2005-2014 ManyDesigns srl.  All rights reserved.
* http://www.manydesigns.com/
*
* Unless you have purchased a commercial license agreement from ManyDesigns srl,
* the following license terms apply:
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3 as published by
* the Free Software Foundation.
*
* There are special exceptions to the terms and conditions of the GPL
* as it is applied to this software. View the full text of the
* exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
* software distribution.
*
* This program is distributed WITHOUT ANY WARRANTY; and without the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
* or write to:
* Free Software Foundation, Inc.,
* 59 Temple Place - Suite 330,
* Boston, MA  02111-1307  USA
*
*/

package com.manydesigns.elements.util;

import com.manydesigns.elements.FormElement;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.forms.FieldSet;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.json.JsonKeyValueAccessor;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class FormUtil {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String JSON_VALUE = "value";

    /**
     * Writes a collection of fields as properties of a JSON object.
     * @param js the JSONStringer to write to. Must have a JSON object open for writing.
     * @param fields the fields to output
     * @throws org.json.JSONException if the JSON can not be generated.
     */
    public static void fieldsToJson(JSONStringer js, Collection<Field> fields) throws JSONException {
        for (Field field : fields) {
            Object value = field.getValue();
            String displayValue = field.getDisplayValue();
            String href = field.getHref();
            js.key(field.getPropertyAccessor().getName());
            js.object()
                    .key(JSON_VALUE)
                    .value(value)
                    .key("displayValue")
                    .value(displayValue)
                    .key("href")
                    .value(href)
                    .endObject();
        }
    }

    public static List<Field> collectVisibleFields(Form form, List<Field> fields) {
        for(FieldSet fieldSet : form) {
             collectVisibleFields(fieldSet, fields);
        }
        return fields;
    }

    public static List<Field> collectVisibleFields(FieldSet fieldSet, List<Field> fields) {
        for(FormElement element : fieldSet) {
            if(element instanceof Field) {
                Field field = (Field) element;
                if(field.isEnabled()) {
                    fields.add(field);
                }
            } else if(element instanceof FieldSet) {
                collectVisibleFields((FieldSet) element, fields);
            }
        }
        return fields;
    }

    public static String formToJson(Form form) {
        JSONStringer js = new JSONStringer();
        js.object();
        List<Field> fields = new ArrayList<Field>();
        collectVisibleFields(form, fields);
        fieldsToJson(js, fields);
        js.endObject();
        return js.toString();
    }

    public static Form readFromJson(Form form, JSONObject jsonObject) {
        JsonKeyValueAccessor kv = new JsonKeyValueAccessor(jsonObject) {
            @Override
            public Object get(String name) {
                Object o = super.get(name);
                if(o instanceof JSONObject) {
                    return new JsonKeyValueAccessor((JSONObject) o).get(JSON_VALUE);
                } else {
                    return o;
                }
            }

            @Override
            public void set(String name, Object value) {
                Object o = super.get(name);
                if(o instanceof JSONObject) {
                    new JsonKeyValueAccessor((JSONObject) o).set(JSON_VALUE, value);
                } else {
                    super.set(name, value);
                }
            }
        };
        form.readFrom(kv);
        return form;
    }
}
