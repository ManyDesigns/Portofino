/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.elements.servlet;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class AttributeMap implements Map<String, Object> {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected Method attributeNamesGetter;
    protected Method attributeGetter;
    protected Method attributeSetter;

    protected Object wrappedObject;

    //--------------------------------------------------------------------------
    // Logger
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LoggerFactory.getLogger(AttributeMap.class);

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public static AttributeMap createAttributeMap(ServletRequest request) {
        return new AttributeMap(ServletRequest.class, request);
    }

    public static AttributeMap createAttributeMap(HttpSession session) {
        return new AttributeMap(HttpSession.class, session);
    }

    public static AttributeMap createAttributeMap(ServletContext servletContext) {
        return new AttributeMap(ServletContext.class, servletContext);
    }

    protected AttributeMap(Class clazz, Object wrappedObject) {
        this.wrappedObject = wrappedObject;
        try {
            attributeNamesGetter = clazz.getMethod("getAttributeNames");
            attributeGetter = clazz.getMethod("getAttribute", String.class);
            attributeSetter = clazz.getMethod("setAttribute", String.class, Object.class);
        } catch (NoSuchMethodException e) {
            throw new Error("Required method not found", e);
        }
    }

    //--------------------------------------------------------------------------
    // Reflection
    //--------------------------------------------------------------------------

    protected Enumeration getAttributeNames() {
        if (wrappedObject == null) {
            logger.debug("Null wrapped object");
            return null;
        }
        try {
            return (Enumeration) attributeNamesGetter.invoke(wrappedObject);
        } catch (Throwable e) {
            logger.warn("Invocation error", e);
            return Collections.enumeration(Collections.EMPTY_LIST);
        }
    }

    protected Object getAttribute(String name) {
        if (wrappedObject == null) {
            logger.debug("Null wrapped object");
            return null;
        }
        try {
            return attributeGetter.invoke(wrappedObject, name);
        } catch (Throwable e) {
            logger.warn("Invocation error", e);
            return null;
        }
    }

    protected void setAttribute(String name, Object value) {
        if (wrappedObject == null) {
            logger.debug("Null wrapped object");
            return;
        }
        try {
            attributeSetter.invoke(wrappedObject, name, value);
        } catch (Throwable e) {
            logger.warn("Invocation error", e);
        }
    }

    //--------------------------------------------------------------------------
    // Map implementation
    //--------------------------------------------------------------------------

    public int size() {
        int counter = 0;
        Enumeration e = getAttributeNames();
        while (e.hasMoreElements()) {
            e.nextElement();
            counter++;
        }
        return counter;
    }

    public boolean isEmpty() {
        return !getAttributeNames().hasMoreElements();
    }

    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    public boolean containsValue(Object value) {
        Enumeration e = getAttributeNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            Object keyValue = getAttribute(key);
            if (keyValue == value) {
                return true;
            }
        }
        return false;
    }

    public Object get(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        return getAttribute((String) key);
    }

    public Object put(String key, @Nullable Object o) {
        Object oldValue = get(key);
        setAttribute(key, o);
        return oldValue;
    }

    public Object remove(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        return put((String) key, null);
    }

    public void putAll(Map<? extends String, ? extends Object> map) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Set<String> keySet() {
        Set<String> result = new HashSet<String>();

        Enumeration e = getAttributeNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            result.add(key);
        }

        return result;
    }

    public Collection<Object> values() {
        Collection<Object> result = new ArrayList<Object>();

        Enumeration e = getAttributeNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            Object value = getAttribute(key);
            result.add(value);
        }

        return result;
    }

    public Set<Map.Entry<String,Object>> entrySet() {
        Set<Map.Entry<String,Object>> result =
                new HashSet<Map.Entry<String,Object>>();

        Enumeration e = getAttributeNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            Object value = getAttribute(key);
            Entry entry = new Entry(key, value);
            result.add(entry);
        }

        return result;
    }

    static class Entry implements Map.Entry<String, Object> {
        final String key;
        Object value;

        Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object o) {
            Object oldValue = value;
            value = o;
            return oldValue;
        }
    }
}
