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

package com.manydesigns.elements.forms;

import com.manydesigns.elements.composites.AbstractCompositeElement;
import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.fields.search.SearchField;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.jetbrains.annotations.NotNull;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class SearchForm extends AbstractCompositeElement<SearchField> {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", "searchform");

        for (SearchField current : this) {
            current.toXhtml(xb);
        }
        xb.closeElement("div");
    }

    public String toSearchString() {
        StringBuilder sb = new StringBuilder();
        for (SearchField current : this) {
            current.toSearchString(sb);
        }
        return sb.toString();
    }

    public void configureCriteria(Criteria criteria) {
        for (SearchField current : this) {
            current.configureCriteria(criteria);
        }
    }

    public SearchField findSearchFieldByPropertyName(String propertyName) {
        for(SearchField current : this) {
            PropertyAccessor accessor = current.getPropertyAccessor();
            if (accessor.getName().equals(propertyName)) {
                return current;
            }
        }
        return null;
    }



}
