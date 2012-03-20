/*
* Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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