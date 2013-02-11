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
