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

package com.manydesigns.portofino.i18n;

import org.apache.commons.configuration.Configuration;

import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ConfigurationResourceBundle extends ResourceBundle {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected Configuration configuration;
    protected Locale locale;

    public ConfigurationResourceBundle(Configuration configuration, Locale locale) {
        this.configuration = configuration;
        this.locale = locale;
    }

    public Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return configuration.getProperty(key);
    }

    public Enumeration<String> getKeys() {
        Set<String> myKeys = handleKeySet();
        if(parent != null) {
            Enumeration<String> parentKeysEnum = parent.getKeys();
            while (parentKeysEnum.hasMoreElements()) {
                myKeys.add(parentKeysEnum.nextElement());
            }
        }
        return Collections.enumeration(myKeys);
    }

    protected Set<String> handleKeySet() {
        Iterator<String> iterator = configuration.getKeys();
        Set<String> keys = new HashSet<String>();
        while (iterator.hasNext()) {
            keys.add(iterator.next());
        }
        return keys;
    }

    @Override
    public void setParent(ResourceBundle parent) {
        super.setParent(parent);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }
}