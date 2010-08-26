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

package com.manydesigns.portofino.model.datamodel;

import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Column {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields: physical (jdbc)
    //**************************************************************************

    protected String databaseName;
    protected String schemaName;
    protected String tableName;
    protected String columnName;
    protected String columnType;
    protected String classProperty;
    protected boolean nullable;
    protected boolean autoincrement;
    protected int length;
    protected int scale;

    //**************************************************************************
    // Fields: logical (Java)
    //**************************************************************************

    protected Class javaType;

    //**************************************************************************
    // Constructors
    //**************************************************************************
    public Column(String databaseName, String schemaName,
                  String tableName, String columnName,
                  String columnType, boolean nullable,
                  boolean autoincrement, int length,
                  int scale) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnType = columnType;
        this.nullable = nullable;
        this.autoincrement = autoincrement;
        this.length = length;
        this.scale = scale;
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public Class getJavaType() {
        return javaType;
    }

    public void setJavaType(Class javaType) {
        this.javaType = javaType;
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    public void setAutoincrement(boolean autoincrement) {
        this.autoincrement = autoincrement;
    }

    public String getQualifiedName() {
        return MessageFormat.format("{0}.{1}.{2}.{3}",
                databaseName, schemaName, tableName, columnName);
    }

    public String getClassProperty() {
        return classProperty;
    }

    public void setClassProperty(String classProperty) {
        this.classProperty = classProperty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Column column = (Column) o;

        if (autoincrement != column.autoincrement) return false;
        if (length != column.length) return false;
        if (nullable != column.nullable) return false;
        if (scale != column.scale) return false;
        if (classProperty != null ? !classProperty.equals(column.classProperty) : column.classProperty != null)
            return false;
        if (columnName != null ? !columnName.equals(column.columnName) : column.columnName != null)
            return false;
        if (columnType != null ? !columnType.equals(column.columnType) : column.columnType != null)
            return false;
        if (databaseName != null ? !databaseName.equals(column.databaseName) : column.databaseName != null)
            return false;
        if (javaType != null ? !javaType.equals(column.javaType) : column.javaType != null)
            return false;
        if (schemaName != null ? !schemaName.equals(column.schemaName) : column.schemaName != null)
            return false;
        if (tableName != null ? !tableName.equals(column.tableName) : column.tableName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = databaseName != null ? databaseName.hashCode() : 0;
        result = 31 * result + (schemaName != null ? schemaName.hashCode() : 0);
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
        result = 31 * result + (columnName != null ? columnName.hashCode() : 0);
        result = 31 * result + (columnType != null ? columnType.hashCode() : 0);
        result = 31 * result + (classProperty != null ? classProperty.hashCode() : 0);
        result = 31 * result + (nullable ? 1 : 0);
        result = 31 * result + (autoincrement ? 1 : 0);
        result = 31 * result + length;
        result = 31 * result + scale;
        result = 31 * result + (javaType != null ? javaType.hashCode() : 0);
        return result;
    }
}
