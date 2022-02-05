/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.i18n;

/**
 * Provides internationalized strings for a given {@link java.util.Locale Locale}.
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface TextProvider {
    String copyright = "Copyright (C) 2005-2020 ManyDesigns srl";

    /**
     * Get the localized string according to the key, and replace any placeholders with the arguments.
     * @param key the key in the resource bundle.
     * @param args arguments to insert where the string contains placeholders.
     * @return the localized string corresponding to the key, with placeholders replaced, or the key itself if the
     * resource bundle does not contain the key.
     */
    String getText(String key, Object... args);

    /**
     * Same as {@link #getText(String, Object...) getText}, but returns <code>null</code> if the key has no associated
     * string in the given locale.
     * @param key the key in the resource bundle.
     * @param args arguments to insert where the string contains placeholders.
     * @return the localized string corresponding to the key, with placeholders replaced, or null if the
     * resource bundle does not contain the key.
     */
    String getTextOrNull(String key, Object... args);
}
