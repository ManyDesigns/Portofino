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

package com.manydesigns.elements.options;

import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public interface SelectionModel {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    SelectionProvider getSelectionProvider();
    String getName();
    Object getValue(int index);
    void setValue(int index, Object value);

    String getLabelSearch(int index);
    void setLabelSearch(int index, String labelSearch);

    Map<Object, Option> getOptions(int index);

    String getOption(int index, Object value, boolean includeInactive);

    public static class Option {
        public final Object value;
        public final String label;
        public final boolean active;

        public Option(Object value, String label, boolean active) {
            this.value = value;
            this.label = label;
            this.active = active;
        }
    }
}
