/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.elements.fields.search;

import com.manydesigns.elements.reflection.PropertyAccessor;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public interface Criteria {
    Criteria eq(PropertyAccessor accessor, Object value);

    Criteria in(PropertyAccessor accessor, Object[] value);

    Criteria ne(PropertyAccessor accessor, Object value);

    Criteria between(PropertyAccessor accessor, Object min, Object max);

    Criteria gt(PropertyAccessor accessor, Object value);

    Criteria ge(PropertyAccessor accessor, Object value);

    Criteria lt(PropertyAccessor accessor, Object value);

    Criteria le(PropertyAccessor accessor, Object value);

    Criteria like(PropertyAccessor accessor, String value,
                     TextMatchMode textMatchMode);

    Criteria ilike(PropertyAccessor accessor, String value,
                                      TextMatchMode textMatchMode);

    Criteria isNull(PropertyAccessor accessor);

    Criteria isNotNull(PropertyAccessor accessor);

    Criteria orderBy(PropertyAccessor accessor, String direction);

    OrderBy getOrderBy();

    static class OrderBy {

        protected final PropertyAccessor propertyAccessor;
        protected final String direction;

        public static final String ASC = "asc";
        public static final String DESC = "desc";

        public OrderBy(PropertyAccessor propertyAccessor, String direction) {
            this.propertyAccessor = propertyAccessor;
            this.direction = direction;
        }

        public PropertyAccessor getPropertyAccessor() {
            return propertyAccessor;
        }

        public String getDirection() {
            return direction;
        }

        public boolean isAsc() {
            return ASC.equals(direction);
        }

        public boolean isDesc() {
            return DESC.equals(direction);
        }
    }
}
