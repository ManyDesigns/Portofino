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

package com.manydesigns.portofino.database;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.portofino.model.Column;
import com.manydesigns.portofino.model.Database;
import com.manydesigns.portofino.model.Schema;
import com.manydesigns.portofino.model.Table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class JdbcMetadataReader {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String databaseName;
    protected DatabaseMetaData metadata;
    protected Database database;
    protected List<Schema> schemas;
    protected List<Table> tables;

    public final Logger logger = LogUtil.getLogger(JdbcMetadataReader.class);

    //**************************************************************************
    // Constructor
    //**************************************************************************

    public JdbcMetadataReader() {
    }


    //**************************************************************************
    // Read entire model
    //**************************************************************************

    public Database readModelFromConnection(Connection conn, String databaseName)
            throws SQLException {
        this.databaseName = databaseName;
        database = new Database(databaseName, null);
        schemas = database.getSchemas();
        tables = new ArrayList<Table>();
        try {
            metadata = conn.getMetaData();
            readSchemas();
            readTables();
            readColumns();
        } finally {
            DbUtil.closeConnection(conn);
        }

        return database;
    }


    //**************************************************************************
    // Read schemas
    //**************************************************************************

    private void readSchemas() throws SQLException {
        logger.fine("Searching for schemas...");
        ResultSet rs = null;
        try {
            rs = metadata.getSchemas();
            while(rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                Schema schema = new Schema(databaseName, schemaName);
                LogUtil.fineMF(logger, "Found schema: {0}",
                        schema.getQualifiedName());
                schemas.add(schema);
            }
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }


    //**************************************************************************
    // Read tables
    //**************************************************************************

    private void readTables() throws SQLException {
        for (Schema schema : schemas) {
            readTables(schema);
        }
    }

    private void readTables(Schema schema) throws SQLException {
        String expectedSchemaName = schema.getSchemaName();
        LogUtil.fineMF(logger, "Searching for tables in schema {0}",
                schema.getQualifiedName());
        ResultSet rs = null;
        try {
            String[] args = {"TABLE"};
            rs = metadata.getTables(null, expectedSchemaName, null, args);
            while(rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");

                // sanity check
                if (!expectedSchemaName.equals(schemaName)) {
                    LogUtil.fineMF(logger,
                            "Skipping table {0}.{1}.{2} because schema " +
                                    "does not match expected: {0}.{3}",
                            databaseName,
                            schemaName,
                            tableName,
                            expectedSchemaName);
                    continue;
                }

                Table table =
                        new Table(databaseName, expectedSchemaName, tableName);
                LogUtil.fineMF(logger, "Found table: {0}",
                        table.getQualifiedName());
                schema.getTables().add(table);
                tables.add(table);
            }
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }


    //**************************************************************************
    // Read columns
    //**************************************************************************

    private void readColumns()  throws SQLException {
        for (Table table: tables) {
            readColumns(table);
        }
    }

    private void readColumns(Table table) throws SQLException {
        String expectedSchemaName = table.getSchemaName();
        String expectedTableName = table.getTableName();
        LogUtil.fineMF(logger, "Searching for columns in table {0}",
                table.getQualifiedName());
        ResultSet rs = null;
        try {
            rs = metadata.getColumns(null, expectedSchemaName,
                    expectedTableName, null);
            while(rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                String columnType = rs.getString("TYPE_NAME");
                boolean nullable = (rs.getInt("NULLABLE") ==
                        DatabaseMetaData.columnNullable);
                int length = rs.getInt("COLUMN_SIZE");
                int scale = rs.getInt("DECIMAL_DIGITS");

                // sanity check
                if (!expectedSchemaName.equals(schemaName) ||
                        !expectedTableName.equals(tableName)) {
                    LogUtil.fineMF(logger,
                            "Skipping column {0}.{1}.{2}.{3} because table " +
                                    "does not match expected: {0}.{4}.{5}",
                            databaseName,
                            schemaName,
                            tableName,
                            columnName,
                            expectedSchemaName,
                            expectedTableName);
                    continue;
                }

                Column column =
                        new Column(databaseName, expectedSchemaName,
                                expectedTableName, columnName,
                                columnType, nullable, length, scale);
                LogUtil.fineMF(logger, "Found column: {0} of type {1}",
                        column.getQualifiedName(),
                        column.getColumnType());
                table.getColumns().add(column);
            }
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }

}
