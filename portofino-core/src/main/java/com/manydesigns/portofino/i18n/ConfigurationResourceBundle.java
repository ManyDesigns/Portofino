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
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected Configuration configuration;

    public ConfigurationResourceBundle(Configuration configuration) {
        this.configuration = configuration;
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
}