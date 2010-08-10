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
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class CommonDatabaseAbstraction implements DatabaseAbstraction {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static final Logger logger =
            LogUtil.getLogger(CommonDatabaseAbstraction.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public CommonDatabaseAbstraction() {}

    //**************************************************************************
    // Implementation of DatabaseAbstraction
    //**************************************************************************

    //**************************************************************************
    // Read entire model
    //**************************************************************************

    public Database readModel(ConnectionProvider connectionProvider) {
        Database database = new Database(connectionProvider.getDatabaseName());
        Connection conn = null;
        try {
            conn = connectionProvider.acquireConnection();
            DatabaseMetaData metadata = conn.getMetaData();
            readSchemas(metadata, database);
            readTables(metadata, database);
            readColumns(connectionProvider, metadata, database);
            readPKs(metadata, database);
            readFKs(metadata, database);
        } catch (Throwable e) {
            LogUtil.severeMF(logger, "Could not read model from {0}",
                    e, connectionProvider.getDatabaseName());
            return null;
        } finally {
            connectionProvider.releaseConnection(conn);
        }

        return database;
    }


    //**************************************************************************
    // Read schemas
    //**************************************************************************

    protected void readSchemas(DatabaseMetaData metadata, Database database)
            throws SQLException {
        logger.fine("Searching for schemas...");
        ResultSet rs = null;
        try {
            rs = metadata.getSchemas();
            while(rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                Schema schema = new Schema(database.getDatabaseName(), schemaName);
                LogUtil.fineMF(logger, "Found schema: {0}",
                        schema.getQualifiedName());
                database.getSchemas().add(schema);
            }
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }


    //**************************************************************************
    // Read tables
    //**************************************************************************

    protected void readTables(DatabaseMetaData metadata, Database database)
            throws SQLException {
        for (Schema schema : database.getSchemas()) {
            readTables(metadata, schema);
        }
    }

    protected void readTables(DatabaseMetaData metadata, Schema schema)
            throws SQLException {
        String expectedDatabaseName = schema.getDatabaseName();
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
                            expectedDatabaseName,
                            schemaName,
                            tableName,
                            expectedSchemaName);
                    continue;
                }

                Table table = new Table(expectedDatabaseName,
                        expectedSchemaName, tableName);
                LogUtil.fineMF(logger, "Found table: {0}",
                        table.getQualifiedName());
                schema.getTables().add(table);
            }
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }


    //**************************************************************************
    // Read columns
    //**************************************************************************

    protected void readColumns(ConnectionProvider connectionProvider,
                               DatabaseMetaData metadata, Database database)
            throws SQLException {
        for (Table table : database.getAllTables()) {
            readColumns(connectionProvider, metadata, table);
        }
    }

    protected void readColumns(ConnectionProvider connectionProvider,
                               DatabaseMetaData metadata, Table table)
            throws SQLException {
        String expectedDatabaseName = table.getDatabaseName();
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
                            expectedDatabaseName,
                            schemaName,
                            tableName,
                            columnName,
                            expectedSchemaName,
                            expectedTableName);
                    continue;
                }

                Type type = connectionProvider.getTypeByName(columnType);
                Column column = new Column(expectedDatabaseName,
                        expectedSchemaName, expectedTableName,
                        columnName, columnType,
                        nullable, type.isAutoincrement(),
                        length, scale);
                LogUtil.fineMF(logger, "Found column: {0} of type {1}",
                        column.getQualifiedName(),
                        column.getColumnType());
                column.setJavaType(type.getDefaultJavaType());

                table.getColumns().add(column);
            }
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }



    //**************************************************************************
    // Read Primary keys
    //**************************************************************************

    protected void readPKs(DatabaseMetaData metadata, Database database)
            throws SQLException {
        for (Table table : database.getAllTables()) {
            readPKs(metadata, table);
        }
    }

    protected void readPKs(DatabaseMetaData metadata, Table table)
            throws SQLException {
        String expectedDatabaseName = table.getDatabaseName();
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
                            expectedDatabaseName,
                            schemaName,
                            tableName,
                            columnName,
                            expectedSchemaName,
                            expectedTableName);
                    continue;
                }

                if (primaryKey == null) {
                    primaryKey = new PrimaryKey(
                            expectedDatabaseName,
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

                Column column = table.findColumnByName(columnName);

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


    //**************************************************************************
    // Read foreign keys
    //**************************************************************************

    protected void readFKs(DatabaseMetaData metadata, Database database)
            throws SQLException {
        for (Table table : database.getAllTables()) {
            readFKs(metadata, database, table);
        }
    }

    protected void readFKs(DatabaseMetaData metadata, Database database,
                         Table table) throws SQLException {
        String expectedDatabaseName = table.getDatabaseName();
        String expectedSchemaName = table.getSchemaName();
        String expectedTableName = table.getTableName();
        LogUtil.fineMF(logger, "Searching for foreign keys in table {0}",
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
                            expectedDatabaseName,
                            schemaName,
                            tableName,
                            columnName,
                            expectedSchemaName,
                            expectedTableName);
                    continue;
                }

                String qualifiedReferencedTableName =
                        MessageFormat.format("{0}.{1}.{2}",
                                expectedDatabaseName,
                                referencedSchemaName,
                                referencedTableName
                        );
                Table referencedTable =
                        database.findTableByQualifiedName(
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

                Column column = findColumn(database, table, columnName);
                if (column == null) {
                    LogUtil.warningMF(logger, "Column {0} not found. " +
                            "Aborting FK search for this table.",
                            column.getQualifiedName());
                    return;
                }

                Column referencedColumn =
                        findColumn(database,
                                referencedTable, referencedColumnName);
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

    protected Column findColumn(Database database, Table table, String columnName) {
        String qualifiedColumnName =
                MessageFormat.format("{0}.{1}.{2}",
                        table.getSchemaName(),
                        table.getQualifiedName(),
                        columnName);
        return database.findColumnByQualifiedName(
                        qualifiedColumnName);
    }

    protected void installRelationship(Relationship relationship,
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

    protected String decodeUpdateDeleteRule(short rule) {
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

    protected Column[] ensureMinimumArrayLength(Column[] oldArray, int length) {
        if (oldArray.length >= length) {
            return oldArray;
        }
        Column[] newArray = new Column[length];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }

    protected Reference[] ensureMinimumArrayLength(Reference[] oldArray, int length) {
        if (oldArray.length >= length) {
            return oldArray;
        }
        Reference[] newArray = new Reference[length];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }
}
