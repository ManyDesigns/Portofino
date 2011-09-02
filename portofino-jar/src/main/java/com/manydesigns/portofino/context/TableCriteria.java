/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.context;

import com.manydesigns.elements.fields.search.BaseCriteria;
import com.manydesigns.elements.fields.search.TextMatchMode;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.model.datamodel.Table;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class TableCriteria extends BaseCriteria {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

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
}
