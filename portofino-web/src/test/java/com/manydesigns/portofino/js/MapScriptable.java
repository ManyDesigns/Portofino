/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.js;

import org.mozilla.javascript.Scriptable;

import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MapScriptable implements Scriptable {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    final Map map;
    Scriptable prototype;
    Scriptable parentScope;

    public MapScriptable(Map map) {
        this.map = map;
    }

    public String getClassName() {
        return "JavaMap";
    }

    public Object get(String name, Scriptable start) {
        return map.get(name);
    }

    public Object get(int index, Scriptable start) {
        return map.get(index);
    }

    public boolean has(String name, Scriptable start) {
        return map.containsKey(name);
    }

    public boolean has(int index, Scriptable start) {
        return map.containsKey(index);
    }

    public void put(String name, Scriptable start, Object value) {
        map.put(name, value);
    }

    public void put(int index, Scriptable start, Object value) {
        map.put(index, value);
    }

    public void delete(String name) {
        map.remove(name);
    }

    public void delete(int index) {
        map.remove(index);
    }

    public Scriptable getPrototype() {
        return prototype;
    }

    public void setPrototype(Scriptable prototype) {
        this.prototype = prototype;
    }

    public Scriptable getParentScope() {
        return parentScope;
    }

    public void setParentScope(Scriptable parent) {
        this.parentScope = parent;
    }

    public Object[] getIds() {
        return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getDefaultValue(Class<?> hint) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasInstance(Scriptable instance) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
