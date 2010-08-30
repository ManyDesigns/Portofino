/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.reflection;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.reflection.helpers.ClassAccessorManager;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.datamodel.Column;
import com.manydesigns.portofino.model.datamodel.Table;

import java.util.HashMap;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableAccessor implements ClassAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Table table;
    protected final ColumnAccessor[] columnAccessors;
    protected final ColumnAccessor[] keyColumnAccessors;
    protected ClassAccessor javaClassAccessor = null;


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public TableAccessor(Table table) {
        String className = table.getClassName();
        if (className != null) {
            Class clazz = ReflectionUtil.loadClass(className);
            if (clazz != null) {
                javaClassAccessor =
                        ClassAccessorManager.getManager()
                                .tryToInstantiateFromClass(clazz);
            }
        }

        this.table = table;
        List<Column> columns = table.getColumns();
        List<Column> pkColumns = table.getPrimaryKey().getColumns();
        columnAccessors = new ColumnAccessor[columns.size()];
        keyColumnAccessors = new ColumnAccessor[pkColumns.size()];
        int i = 0;
        for (Column current : columns) {
            boolean inPk = pkColumns.contains(current);
            PropertyAccessor nestedPropertyAccessor = null;
            if (javaClassAccessor != null) {
                String propertyName = current.getColumnName();
                if (current.getPropertyName() != null) {
                    propertyName = current.getPropertyName();
                }
                try {
                    nestedPropertyAccessor =
                            javaClassAccessor.getProperty(propertyName);
                } catch (NoSuchFieldException e) {
                    // TODO:
                }
            }
            ColumnAccessor columnAccessor =
                    new ColumnAccessor(current, inPk, nestedPropertyAccessor);
            columnAccessors[i] = columnAccessor;
            i++;
        }

        // key column accessors
        i = 0;
        for (Column current : pkColumns) {
            int index = columns.indexOf(current);
            ColumnAccessor columnAccessor = columnAccessors[index];
            keyColumnAccessors[i] = columnAccessor;
            i++;
        }
    }


    //**************************************************************************
    // ClassAccessor implementation
    //**************************************************************************

    public String getName() {
        return table.getQualifiedName();
    }

    public PropertyAccessor getProperty(String fieldName)
            throws NoSuchFieldException {
        for (ColumnAccessor current : columnAccessors) {
            if (current.getName().equals(fieldName)) {
                return current;
            }
        }

        throw new NoSuchFieldException(fieldName);
    }

    
    public PropertyAccessor[] getProperties() {
        return columnAccessors.clone();
    }


    public PropertyAccessor[] getKeyProperties() {
        return keyColumnAccessors.clone();
    }

    public Object newInstance() {
        if (javaClassAccessor == null) {
            HashMap<String, Object> obj =  new HashMap<String, Object>();
            obj.put("$type$", table.getQualifiedName());
            return obj;
        } else {
            return javaClassAccessor.newInstance();
        }
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public Table getTable() {
        return table;
    }
}
