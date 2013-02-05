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