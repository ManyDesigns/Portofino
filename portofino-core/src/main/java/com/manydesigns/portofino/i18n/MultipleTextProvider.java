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

import com.manydesigns.elements.i18n.TextProvider;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MultipleTextProvider implements TextProvider {

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected final List<ResourceBundle> resourceBundles;

    public MultipleTextProvider(ResourceBundle... resourceBundles) {
        this.resourceBundles = new ArrayList<ResourceBundle>(Arrays.asList(resourceBundles));
    }
    //--------------------------------------------------------------------------
    // TextProvider implementation
    //--------------------------------------------------------------------------

    public String getText(String key, Object... args) {
        String localizedString = getLocalizedString(key);
        return MessageFormat.format(localizedString, args);
    }

    public List<ResourceBundle> getResourceBundles() {
        return resourceBundles;
    }

    //--------------------------------------------------------------------------
    // Utility methods
    //--------------------------------------------------------------------------

    public String getLocalizedString(String key) {
        for (ResourceBundle current : resourceBundles) {
            try {
                return current.getString(key);
            } catch (Throwable t) {
                /* IGNORE */
            }
        }
        return key;
    }
}