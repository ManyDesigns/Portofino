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
import com.manydesigns.portofino.model.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
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
    protected DataModel dataModel;
    protected Database database;
    protected List<Schema> schemas;
    protected List<Table> tables;

    public static final Logger logger =
            LogUtil.getLogger(JdbcMetadataReader.class);

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
        dataModel = new DataModel();
        database = new Database(databaseName, null);
        dataModel.getDatabases().add(database);
        schemas = database.getSchemas();
        tables = new ArrayList<Table>();
        try {
            metadata = conn.getMetaData();
            readSchemas();
            readTables();
            readColumns();
            readPKs();
            readFKs();
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



    //**************************************************************************
    // Read Primary keys
    //**************************************************************************

    private void readPKs() throws SQLException {
        for (Table table : tables) {
            readPKs(table);
        }
    }

    private void readPKs(Table table) throws SQLException {
        String expectedSchemaName = table.getSchemaName();
        String expectedTableName = table.getTableName();
        LogUtil.fineMF(logger, "Searching for primary key in table {0}",
                table.getQualifiedName());
        ResultSet rs = null;
        try {
            PrimaryKey primaryKey = null;
            Column[] pkColumnArray = new Column[0];

            rs = metadata.getPrimaryKeys(null, expectedSchemaName,
                    expectedTableName);
            while(rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                short keySeq = rs.getShort("KEY_SEQ");
                String pkName = rs.getString("PK_NAME");

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

                if (primaryKey == null) {
                    primaryKey = new PrimaryKey(
                            databaseName,
                            expectedSchemaName,
                            expectedTableName,
                            pkName);
                    LogUtil.fineMF(logger, "Found primary key: {0}", pkName);
                } else if (!primaryKey.getPkName().equals(pkName)) {
                    //sanity check
                    LogUtil.warningMF(logger,
                            "Found new PK name {0} different " +
                            "from previous name {1}",
                            pkName, primaryKey.getPkName());
                    return;
                }

                Column column = findColumn(table, columnName);

                // sanity check
                if (column == null) {
                    LogUtil.warningMF(logger, "PK column {0} not found in " +
                            "columns of table {1}. " +
                            "Aborting PK search for this table.",
                            columnName, table.getQualifiedName());
                    return;
                }

                LogUtil.fineMF(logger,
                        "Found PK column {0} with key sequence {1}",
                        column.getQualifiedName(),
                        keySeq);

                pkColumnArray =
                        ensureMinimumArrayLength(pkColumnArray, keySeq + 1);
                pkColumnArray[keySeq] = column;
            }

            if (primaryKey == null) {
                LogUtil.fineMF(logger,
                        "No PK found for: {0}",
                        table.getQualifiedName());
                return;
            }

            // copy non-null elements of array
            for (Column current : pkColumnArray) {
                if (current == null) {
                    continue;
                }
                primaryKey.getColumns().add(current);
            }
            // sanity check
            if (primaryKey.getColumns().size() == 0) {
                LogUtil.warningMF(logger,
                        "Primary key {0} is empty. Discarding.",
                        primaryKey.getPkName());
                return;
            }
            table.setPrimaryKey(primaryKey);
            LogUtil.fineMF(logger,
                    "Installed PK {0} with number of columns: {1}",
                    primaryKey.getPkName(),
                    primaryKey.getColumns().size());
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }

    private Column findColumn(Table table, String columnName) {
        String qualifiedColumnName =
                MessageFormat.format("{0}.{1}",
                        table.getQualifiedName(), columnName);
        return dataModel.findColumnByQualifiedName(
                        qualifiedColumnName);
    }


    //**************************************************************************
    // Read foreign keys
    //**************************************************************************

    private void readFKs() throws SQLException {
        for (Table table : tables) {
            readFKs(table);
        }
    }

    private void readFKs(Table table) throws SQLException {
        String expectedSchemaName = table.getSchemaName();
        String expectedTableName = table.getTableName();
        LogUtil.fineMF(logger, "Searching for forwign keys in table {0}",
                table.getQualifiedName());
        ResultSet rs = null;
        Relationship relationship = null;
        Reference[] referenceArray = new Reference[0];
        try {
            rs = metadata.getImportedKeys(null, expectedSchemaName,
                    expectedTableName);
            while(rs.next()) {
                String schemaName = rs.getString("FKTABLE_SCHEM");
                String tableName = rs.getString("FKTABLE_NAME");
                String columnName = rs.getString("FKCOLUMN_NAME");

                String referencedSchemaName = rs.getString("PKTABLE_SCHEM");
                String referencedTableName = rs.getString("PKTABLE_NAME");
                String referencedColumnName = rs.getString("PKCOLUMN_NAME");

                short keySeq = rs.getShort("KEY_SEQ");
                short updateRule = rs.getShort("UPDATE_RULE");
                short deleteRule = rs.getShort("DELETE_RULE");
                short deferrability = rs.getShort("DEFERRABILITY");
                String fkName = rs.getString("FK_NAME");

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

                String qualifiedReferencedTableName =
                        MessageFormat.format("{0}.{1}.{2}",
                                databaseName,
                                referencedSchemaName,
                                referencedTableName
                        );
                Table referencedTable =
                        dataModel.findTableByQualifiedName(
                                qualifiedReferencedTableName);

                if (relationship == null ||
                        !relationship.getRelationshipName().equals(fkName)) {
                    if (relationship != null) {
                        installRelationship(relationship, referenceArray);
                    }

                    relationship = new Relationship(
                            fkName,
                            decodeUpdateDeleteRule(updateRule),
                            decodeUpdateDeleteRule(deleteRule));
                    relationship.setFromTable(table);
                    relationship.setToTable(referencedTable);

                    // reset the refernceArray
                    referenceArray = new Reference[0];
                    LogUtil.fineMF(logger, "Found foreign key: {0}", fkName);
                }

                Column column = findColumn(table, columnName);
                if (column == null) {
                    LogUtil.warningMF(logger, "Column {0} not found. " +
                            "Aborting FK search for this table.",
                            column.getQualifiedName());
                    return;
                }

                Column referencedColumn =
                        findColumn(referencedTable, referencedColumnName);
                if (referencedColumn == null) {
                    LogUtil.warningMF(logger, "Column {0} not found. " +
                            "Aborting FK search for this table.",
                            column.getQualifiedName());
                    return;
                }

                Reference reference = new Reference(column, referencedColumn);

                LogUtil.fineMF(logger,
                        "Found FK reference {0} -> {1} with key sequence {2}",
                        column.getQualifiedName(),
                        referencedColumn.getQualifiedName(),
                        keySeq);

                referenceArray =
                        ensureMinimumArrayLength(referenceArray, keySeq + 1);
                referenceArray[keySeq] = reference;
            }
            if (relationship != null) {
                installRelationship(relationship, referenceArray);
            }
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }

    private void installRelationship(Relationship relationship,
                                     Reference[] referenceArray) {
        // copy non-null elements of array
        for (Reference current : referenceArray) {
            if (current == null) {
                continue;
            }
            relationship.getReferences().add(current);
        }
        // sanity check
        if (relationship.getReferences().size() == 0) {
            LogUtil.warningMF(logger,
                    "Foreign key {0} is empty. Discarding.",
                    relationship.getRelationshipName());
            return;
        }
        relationship.getFromTable()
                    .getManyToOneRelationships().add(relationship);
        relationship.getToTable()
                    .getOneToManyRelationships().add(relationship);
        LogUtil.fineMF(logger,
                    "Installed FK {0} with number of columns: {1}",
                relationship.getRelationshipName(),
                relationship.getReferences().size());
    }

    private String decodeUpdateDeleteRule(short rule) {
        switch (rule) {
            case DatabaseMetaData.importedKeyNoAction:
            case DatabaseMetaData.importedKeyRestrict:
                return Relationship.RULE_NO_ACTION;
            case DatabaseMetaData.importedKeyCascade:
                return Relationship.RULE_CASCADE;
            case DatabaseMetaData.importedKeySetNull:
                return Relationship.RULE_SET_NULL;
            case DatabaseMetaData.importedKeySetDefault:
                return Relationship.RULE_SET_DEFAULT;
            default:
                throw new IllegalArgumentException("Rule: " + rule);
        }
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    private Column[] ensureMinimumArrayLength(Column[] oldArray, int length) {
        if (oldArray.length >= length) {
            return oldArray;
        }
        Column[] newArray = new Column[length];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }

    private Reference[] ensureMinimumArrayLength(Reference[] oldArray, int length) {
        if (oldArray.length >= length) {
            return oldArray;
        }
        Reference[] newArray = new Reference[length];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }
}
