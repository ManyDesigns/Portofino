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

package com.manydesigns.portofino.persistence.hibernate;

import com.manydesigns.portofino.database.model.Column;
import com.manydesigns.portofino.database.model.DatabaseLogic;
import org.hibernate.usertype.DynamicParameterizedType;

import java.lang.annotation.Annotation;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Emanuele  Poggi      - emanuele.poggi@manydesigns.com
 * @author Alessio   Stalla     - alessio.stalla@manydesigns.com
 */
public class ColumnParameterType implements DynamicParameterizedType.ParameterType {

    protected final com.manydesigns.portofino.database.model.Column column;
    protected final Class<?> returnedClass;

    public ColumnParameterType(Column column) {
        this(column, column.getActualJavaType());
    }
    
    public ColumnParameterType(Column column, Class<?> returnedClass) {
        this.column = column;
        this.returnedClass = returnedClass;
    }

    @Override
    public Class<?> getReturnedClass() {
        return returnedClass;
    }

    @Override
    public Annotation[] getAnnotationsMethod() {
        return new Annotation[0];
    }

    @Override
    public String getCatalog() {
        return column.getTable().getSchema().getCatalog();
    }

    @Override
    public String getSchema() {
        return column.getSchemaName();
    }

    @Override
    public String getTable() {
        return column.getTableName();
    }

    @Override
    public boolean isPrimaryKey() {
        return DatabaseLogic.isInPk(column);
    }

    @Override
    public String[] getColumns() {
        return new String[]{column.getColumnName()};
    }

    @Override
    public Long[] getColumnLengths() {
        return new Long[]{column.getLength() != null ? column.getLength().longValue() : null};
    }
}
