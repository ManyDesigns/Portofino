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

package com.manydesigns.portofino.base.model;

import junit.framework.TestCase;

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class DBParserTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    DBParser parser;
    DataModel dataModel;

    public void setUp() {
        parser = new DBParser();
    }

    public void testParseJpetStorePostgresql() {
        dataModel = parser.parse(
                "databases/jpetstore/postgresql/jpetstore-postgres.xml");

        assertNotNull(dataModel);

        List<Database> databases = dataModel.getDatabases();
        assertEquals(1, databases.size());

        Database database = databases.get(0);
        assertEquals("jpetstore", database.getName());

        Connection connection = database.getConnection();
        assertNotNull(connection);

        List<Schema> schemas = database.getSchemas();
        assertEquals(1, schemas.size());

        Schema schema = schemas.get(0);
        assertEquals("default", schema.getSchemaName());

        List<Table> tables = schema.getTables();
        assertEquals(2, tables.size());

        // tabella 0
        Table table0 = tables.get(0);
        checkTable(table0, "default", "category");

        List<Column> columns0 = table0.getColumns();
        assertEquals(3, columns0.size());

        checkColumn(columns0.get(0),
                "default", "category", "catid", "VARCHAR", false, 10, 0);
        checkColumn(columns0.get(1),
                "default", "category", "name", "VARCHAR", true, 80, 0);
        checkColumn(columns0.get(2),
                "default", "category", "descn", "VARCHAR", true, 255, 0);

        PrimaryKey primaryKey0 = table0.getPrimaryKey();
        assertEquals("pk_category", primaryKey0.getName());
        List<Column> pkColumns0 = primaryKey0.getColumns();
        assertEquals(1, pkColumns0.size());
        assertEquals(columns0.get(0), pkColumns0.get(0));

        // tabella 1
        Table table1 = tables.get(1);
        checkTable(table1, "default", "product");

        List<Column> columns1 = table1.getColumns();
        assertEquals(4, columns1.size());

        checkColumn(columns1.get(0),
                "default", "product", "productid", "VARCHAR", false, 10, 0);
        checkColumn(columns1.get(1),
                "default", "product", "category", "VARCHAR", false, 10, 0);
        checkColumn(columns1.get(2),
                "default", "product", "name", "VARCHAR", true, 80, 0);
        checkColumn(columns1.get(3),
                "default", "product", "descn", "VARCHAR", true, 255, 0);
    }

    private void checkColumn(Column column, String schemaName, String tableName,
                             String columnName, String columnType,
                             boolean nullable, int precision, int scale) {
        assertEquals(schemaName, column.getSchemaName());
        assertEquals(tableName, column.getTableName());
        assertEquals(columnName, column.getColumnName());
        assertEquals(columnType, column.getColumnType());
        assertEquals(nullable, column.isNullable());
        assertEquals(precision, column.getLength());
        assertEquals(precision, column.getPrecision());
        assertEquals(scale, column.getScale());
    }

    private void checkTable(Table table, String schemaName, String tableName) {
        assertEquals(schemaName, table.getSchemaName());
        assertEquals(tableName, table.getTableName());
    }
}
