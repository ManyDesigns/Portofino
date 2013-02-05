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

package com.manydesigns.portofino.database;

import com.manydesigns.elements.fields.search.BaseCriteria;
import com.manydesigns.elements.fields.search.TextMatchMode;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.model.database.Table;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class TableCriteria extends BaseCriteria {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Table table;


    //**************************************************************************
    // Constructor
    //**************************************************************************

    public TableCriteria(Table table) {
        super();
        this.table = table;
    }


    //**************************************************************************
    // Getter/setters
    //**************************************************************************

    public Table getTable() {
        return table;
    }

    //**************************************************************************
    // Overrides to simplify type casting
    //**************************************************************************

    @Override
    public TableCriteria eq(PropertyAccessor accessor, Object value) {
        return (TableCriteria)super.eq(accessor, value);
    }

    @Override
    public TableCriteria in(PropertyAccessor accessor, Object[] values) {
        return (TableCriteria)super.in(accessor, values);
    }

    @Override
    public TableCriteria ne(PropertyAccessor accessor, Object value) {
        return (TableCriteria)super.ne(accessor, value);
    }

    @Override
    public TableCriteria between(PropertyAccessor accessor, Object min, Object max) {
        return (TableCriteria)super.between(accessor, min, max);
    }

    @Override
    public TableCriteria gt(PropertyAccessor accessor, Object value) {
        return (TableCriteria)super.gt(accessor, value);
    }

    @Override
    public TableCriteria ge(PropertyAccessor accessor, Object value) {
        return (TableCriteria)super.ge(accessor, value);
    }

    @Override
    public TableCriteria lt(PropertyAccessor accessor, Object value) {
        return (TableCriteria)super.lt(accessor, value);
    }

    @Override
    public TableCriteria le(PropertyAccessor accessor, Object value) {
        return (TableCriteria)super.le(accessor, value);
    }

    @Override
    public TableCriteria like(PropertyAccessor accessor, String value, TextMatchMode textMatchMode) {
        return (TableCriteria)super.like(accessor, value, textMatchMode);
    }

    @Override
    public TableCriteria ilike(PropertyAccessor accessor, String value, TextMatchMode textMatchMode) {
        return (TableCriteria)super.ilike(accessor, value, textMatchMode);
    }

    @Override
    public TableCriteria isNull(PropertyAccessor accessor) {
        return (TableCriteria)super.isNull(accessor);
    }

    @Override
    public TableCriteria isNotNull(PropertyAccessor accessor) {
        return (TableCriteria)super.isNotNull(accessor);
    }

    @Override
    public TableCriteria orderBy(PropertyAccessor accessor, String direction) {
        return (TableCriteria)super.orderBy(accessor, direction);
    }
}
