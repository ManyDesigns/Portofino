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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
