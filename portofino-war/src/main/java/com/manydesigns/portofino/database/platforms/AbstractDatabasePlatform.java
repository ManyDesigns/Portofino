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

package com.manydesigns.portofino.database.platforms;

import com.manydesigns.portofino.connections.ConnectionProvider;
import com.manydesigns.portofino.database.DbUtil;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.model.datamodel.*;
import org.apache.commons.dbutils.DbUtils;
import org.hibernate.dialect.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class AbstractDatabasePlatform implements DatabasePlatform {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final static String[] tableTypes = {"TABLE"};
    public static final String TABLE_CAT = "TABLE_CAT";
    public static final String TABLE_SCHEM = "TABLE_SCHEM";
    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String COLUMN_NAME = "COLUMN_NAME";
    public static final String KEY_SEQ = "KEY_SEQ";
    public static final String PK_NAME = "PK_NAME";
    public static final String TYPE_NAME = "TYPE_NAME";
    public static final String NULLABLE = "NULLABLE";
    public static final String COLUMN_SIZE = "COLUMN_SIZE";
    public static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";
    public static final String FKTABLE_SCHEM = "FKTABLE_SCHEM";
    public static final String FKTABLE_NAME = "FKTABLE_NAME";
    public static final String FKCOLUMN_NAME = "FKCOLUMN_NAME";
    public static final String PKTABLE_SCHEM = "PKTABLE_SCHEM";
    public static final String PKTABLE_NAME = "PKTABLE_NAME";
    public static final String PKCOLUMN_NAME = "PKCOLUMN_NAME";
    public static final String UPDATE_RULE = "UPDATE_RULE";
    public static final String DELETE_RULE = "DELETE_RULE";
    public static final String DEFERRABILITY = "DEFERRABILITY";
    public static final String FK_NAME = "FK_NAME";
    public static final String FKTABLE_CAT = "FKTABLE_CAT";
    public static final String PKTABLE_CAT = "PKTABLE_CAT";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String status;
    protected Dialect hibernateDialect;
    public static final Logger logger =
            LoggerFactory.getLogger(AbstractDatabasePlatform.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public AbstractDatabasePlatform(Dialect hibernateDialect) {
        this.hibernateDialect = hibernateDialect;
        status = STATUS_CREATED;
    }

    //**************************************************************************
    // Implementation of DatabasePlatform
    //**************************************************************************

    public void test() {
        boolean success = DbUtils.loadDriver(getStandardDriverClassName());
        if (success) {
            status = STATUS_OK;
        } else {
            status = STATUS_DRIVER_NOT_FOUND;
        }
    }

    public String getStatus() {
        return status;
    }

    public Dialect getHibernateDialect() {
        return hibernateDialect;
    }

    //**************************************************************************
    // Read entire model
    //**************************************************************************

    public Database readModel(ConnectionProvider connectionProvider) {
        Database database = new Database();
        database.setDatabaseName(connectionProvider.getDatabaseName());
        Connection conn = null;
        try {
            conn = connectionProvider.acquireConnection();
            DatabaseMetaData metadata = conn.getMetaData();
            readSchemas(conn, connectionProvider, metadata, database);
            readTables(conn, connectionProvider, metadata, database);
            readColumns(conn, connectionProvider, metadata, database);
            readPKs(conn, connectionProvider, metadata, database);
            readFKs(conn, connectionProvider, metadata, database);
        } catch (Throwable e) {
            logger.error("Could not read model from " +
                    connectionProvider.getDatabaseName(), e);
            return null;
        } finally {
            connectionProvider.releaseConnection(conn);
        }

        return database;
    }

    public void shutdown(ConnectionProvider connectionProvider) {
        logger.info("Shutting down connection provider: {}",
                connectionProvider.getDatabaseName());
    }


    //**************************************************************************
    // Read schemas
    //**************************************************************************

    protected void readSchemas(Connection conn,
                               ConnectionProvider connectionProvider,
                               DatabaseMetaData metadata, Database database)
            throws SQLException {
        logger.debug("Searching for schemas...");
        ResultSet rs = null;
        Pattern includePattern = connectionProvider.getIncludeSchemasPattern();
        Pattern excludePattern = connectionProvider.getExcludeSchemasPattern();
        try {
            rs = metadata.getSchemas();
            while(rs.next()) {
                String schemaName = rs.getString(TABLE_SCHEM);
                if (includePattern != null) {
                    Matcher includeMatcher = includePattern.matcher(schemaName);
                    if (!includeMatcher.matches()) {
                        logger.info("Schema '{}' does not match include pattern '{}'. Skipping this schema.",
                                schemaName,
                                connectionProvider.getIncludeSchemas());
                        continue;
                    }
                }
                if (excludePattern != null) {
                    Matcher excludeMatcher = excludePattern.matcher(schemaName);
                    if (excludeMatcher.matches()) {
                        logger.info("Schema '{}' matches exclude pattern '{}'. Skipping this schema.",
                                schemaName,
                                connectionProvider.getExcludeSchemas());
                        continue;
                    }
                }
                Schema schema = new Schema();
                schema.setDatabase(database);
                schema.setSchemaName(schemaName);
                logger.info("Found schema: {}", schema.getQualifiedName());
                database.getSchemas().add(schema);
            }
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }


    //**************************************************************************
    // Read tables
    //**************************************************************************

    protected void readTables(Connection conn,
                              ConnectionProvider connectionProvider,
                              DatabaseMetaData metadata, Database database)
            throws SQLException {
        for (Schema schema : database.getSchemas()) {
            readTables(conn, connectionProvider, metadata, schema);
        }
    }

    protected void readTables(Connection conn,
                              ConnectionProvider connectionProvider,
                              DatabaseMetaData metadata, Schema schema)
            throws SQLException {
        String expectedDatabaseName = schema.getDatabaseName();
        String expectedSchemaName = schema.getSchemaName();
        logger.debug("Searching for tables in schema {}",
                schema.getQualifiedName());
        ResultSet rs = null;
        try {
            rs = metadata.getTables(null, expectedSchemaName, null, tableTypes);
            while(rs.next()) {
                String schemaName = rs.getString(TABLE_SCHEM);
                String tableName = rs.getString(TABLE_NAME);

                // sanity check
                if (!expectedSchemaName.equals(schemaName)) {
                    String msg = MessageFormat.format(
                            "Skipping table {0}.{1}.{2} because schema " +
                                    "does not match expected: {0}.{3}",
                            expectedDatabaseName,
                            schemaName,
                            tableName,
                            expectedSchemaName);
                    logger.debug(msg);
                    continue;
                }

                Table table = new Table();
                table.setSchema(schema);
                table.setTableName(tableName);
                logger.debug("Found table: {}", table.getQualifiedName());
                schema.getTables().add(table);
            }
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }


    //**************************************************************************
    // Read columns
    //**************************************************************************

    protected void readColumns(Connection conn,
                               ConnectionProvider connectionProvider,
                               DatabaseMetaData metadata, Database database)
            throws SQLException {
        for (Table table : database.getAllTables()) {
            readColumns(conn, connectionProvider, metadata, table);
        }
    }

    protected void readColumns(Connection conn,
                               ConnectionProvider connectionProvider,
                               DatabaseMetaData metadata, Table table)
            throws SQLException {
        String expectedDatabaseName = table.getDatabaseName();
        String expectedSchemaName = table.getSchemaName();
        String expectedTableName = table.getTableName();
        logger.debug("Searching for columns in table {}",
                table.getQualifiedName());
        ResultSet rs = null;
        try {
            rs = metadata.getColumns(null, expectedSchemaName,
                    expectedTableName, null);
            while(rs.next()) {
                String schemaName = rs.getString(TABLE_SCHEM);
                String tableName = rs.getString(TABLE_NAME);
                String columnName = rs.getString(COLUMN_NAME);
                String columnType = rs.getString(TYPE_NAME);
                boolean nullable = (rs.getInt(NULLABLE) ==
                        DatabaseMetaData.columnNullable);
                int length = rs.getInt(COLUMN_SIZE);
                int scale = rs.getInt(DECIMAL_DIGITS);

                // sanity check
                if (!expectedSchemaName.equals(schemaName) ||
                        !expectedTableName.equals(tableName)) {
                    String msg = MessageFormat.format(
                            "Skipping column {0}.{1}.{2}.{3} because table " +
                                    "does not match expected: {0}.{4}.{5}",
                            expectedDatabaseName,
                            schemaName,
                            tableName,
                            columnName,
                            expectedSchemaName,
                            expectedTableName);
                    logger.debug(msg);
                    continue;
                }

                Type type = connectionProvider.getTypeByName(columnType);
                Column column = new Column();
                column.setTable(table);
                column.setColumnName(columnName);
                column.setColumnType(columnType);
                column.setNullable(nullable);
                column.setAutoincrement(type.isAutoincrement());
                column.setLength(length);
                column.setScale(scale);
                column.setSearchable(type.isSearchable());
                logger.debug("Found column: {} of type {}",
                        column.getQualifiedName(),
                        column.getColumnType());
                column.setJavaType(type.getDefaultJavaType().getName());

                table.getColumns().add(column);
            }
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }



    //**************************************************************************
    // Read Primary keys
    //**************************************************************************

    protected void readPKs(Connection conn,
                           ConnectionProvider connectionProvider,
                           DatabaseMetaData metadata, Database database)
            throws SQLException {
        for (Table table : database.getAllTables()) {
            readPKs(conn, connectionProvider, metadata, table);
        }
    }

    protected void readPKs(Connection conn,
                           ConnectionProvider connectionProvider,
                           DatabaseMetaData metadata, Table table)
            throws SQLException {
        String expectedDatabaseName = table.getDatabaseName();
        String expectedSchemaName = table.getSchemaName();
        String expectedTableName = table.getTableName();
        logger.debug("Searching for primary key in table {}",
                table.getQualifiedName());
        ResultSet rs = null;
        try {
            PrimaryKey primaryKey = null;
            PrimaryKeyColumn[] pkColumnArray = new PrimaryKeyColumn[0];

            rs = metadata.getPrimaryKeys(null, expectedSchemaName,
                    expectedTableName);
            while(rs.next()) {
                String schemaName = rs.getString(TABLE_SCHEM);
                String tableName = rs.getString(TABLE_NAME);
                String columnName = rs.getString(COLUMN_NAME);
                short keySeq = rs.getShort(KEY_SEQ);
                String pkName = rs.getString(PK_NAME);

                // sanity check
                if (!expectedSchemaName.equals(schemaName) ||
                        !expectedTableName.equals(tableName)) {
                    String msg = MessageFormat.format(
                            "Skipping column {0}.{1}.{2}.{3} because table " +
                                    "does not match expected: {0}.{4}.{5}",
                            expectedDatabaseName,
                            schemaName,
                            tableName,
                            columnName,
                            expectedSchemaName,
                            expectedTableName);
                    logger.debug(msg);
                    continue;
                }

                if (primaryKey == null) {
                    primaryKey = new PrimaryKey();
                    primaryKey.setTable(table);
                    primaryKey.setPrimaryKeyName(pkName);
                    logger.debug("Found primary key: {}", pkName);
                } else if (!primaryKey.getPrimaryKeyName().equals(pkName)) {
                    //sanity check
                    logger.warn("Found new PK name {} different " +
                            "from previous name {}",
                            pkName, primaryKey.getPrimaryKeyName());
                    return;
                }

                Column column = table.findColumnByName(columnName);
                PrimaryKeyColumn primaryKeyColumn =
                        new PrimaryKeyColumn();
                primaryKeyColumn.setPrimaryKey(primaryKey);
                primaryKeyColumn.setColumnName(columnName);

                // sanity check
                if (column == null) {
                    logger.warn("PK column {} not found in " +
                            "columns of table {}. " +
                            "Aborting PK search for this table.",
                            columnName, table.getQualifiedName());
                    return;
                }

                logger.debug("Found PK column {} with key sequence {}",
                        column.getQualifiedName(), keySeq);

                pkColumnArray =
                        ensureMinimumArrayLength(pkColumnArray, keySeq + 1);
                pkColumnArray[keySeq] = primaryKeyColumn;
            }

            if (primaryKey == null) {
                logger.debug("No PK found for: {}", table.getQualifiedName());
                return;
            }

            List<PrimaryKeyColumn> primaryKeyColumns =
                    primaryKey.getPrimaryKeyColumns();
            // copy non-null elements of array
            for (PrimaryKeyColumn current : pkColumnArray) {
                if (current == null) {
                    continue;
                }
                primaryKeyColumns.add(current);
            }
            // sanity check
            if (primaryKeyColumns.size() == 0) {
                logger.warn("Primary key {} is empty. Discarding.",
                        primaryKey.getPrimaryKeyName());
                return;
            }
            table.setPrimaryKey(primaryKey);
            logger.debug("Installed PK {} with number of columns: {}",
                    primaryKey.getPrimaryKeyName(),
                    primaryKeyColumns.size());
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }


    //**************************************************************************
    // Read foreign keys
    //**************************************************************************

    protected void readFKs(Connection conn,
                           ConnectionProvider connectionProvider,
                           DatabaseMetaData metadata, Database database)
            throws SQLException {
        for (Table table : database.getAllTables()) {
            readFKs(conn, connectionProvider, metadata, table);
        }
    }

    protected void readFKs(Connection conn,
                           ConnectionProvider connectionProvider,
                           DatabaseMetaData metadata,
                           Table table) throws SQLException {
        String expectedDatabaseName = table.getDatabaseName();
        String expectedSchemaName = table.getSchemaName();
        String expectedTableName = table.getTableName();
        logger.debug("Searching for foreign keys in table {}",
                table.getQualifiedName());
        ResultSet rs = null;
        ForeignKey relationship = null;
        Reference[] referenceArray = new Reference[0];
        try {
            rs = metadata.getImportedKeys(null, expectedSchemaName,
                    expectedTableName);
            while(rs.next()) {
                String schemaName = rs.getString(FKTABLE_SCHEM);
                String tableName = rs.getString(FKTABLE_NAME);
                String columnName = rs.getString(FKCOLUMN_NAME);

                String referencedDatabaseName = expectedDatabaseName;
                String referencedSchemaName = rs.getString(PKTABLE_SCHEM);
                String referencedTableName = rs.getString(PKTABLE_NAME);
                String referencedColumnName = rs.getString(PKCOLUMN_NAME);

                short keySeq = rs.getShort(KEY_SEQ);
                short updateRule = rs.getShort(UPDATE_RULE);
                short deleteRule = rs.getShort(DELETE_RULE);
                short deferrability = rs.getShort(DEFERRABILITY);
                String fkName = rs.getString(FK_NAME);

                // sanity check
                if (!expectedSchemaName.equals(schemaName) ||
                        !expectedTableName.equals(tableName)) {
                    String msg = MessageFormat.format(
                            "Skipping column {0}.{1}.{2}.{3} because table " +
                                    "does not match expected: {4}.{5}.{6}",
                            referencedDatabaseName,
                            schemaName,
                            tableName,
                            columnName,
                            expectedDatabaseName,
                            expectedSchemaName,
                            expectedTableName);
                    logger.debug(msg);
                    continue;
                }

                if (relationship == null ||
                        !relationship.getForeignKeyName().equals(fkName)) {
                    if (relationship != null) {
                        installRelationship(table, relationship, referenceArray);
                    }

                    relationship = new ForeignKey();
                    relationship.setFromTable(table);
                    relationship.setForeignKeyName(fkName);
                    relationship.setToDatabase(referencedDatabaseName);
                    relationship.setToSchema(referencedSchemaName);
                    relationship.setToTableName(referencedTableName);
                    relationship.setOnUpdate(decodeUpdateDeleteRule(updateRule));
                    relationship.setOnDelete(decodeUpdateDeleteRule(deleteRule));

                    // reset the refernceArray
                    referenceArray = new Reference[0];
                    logger.debug("Found foreign key: {}", fkName);
                }

                Reference reference = new Reference();
                reference.setOwner(relationship);
                reference.setFromColumn(columnName);
                reference.setToColumn(referencedColumnName);

                String qualifiedFromColumnName = MessageFormat.format(
                        "{0}.{1}.{2}.{3}",
                        expectedDatabaseName,
                        expectedSchemaName,
                        expectedTableName,
                        columnName);

                String qualifiedToColumnName = MessageFormat.format(
                        "{0}.{1}.{2}.{3}",
                        referencedDatabaseName,
                        referencedSchemaName,
                        referencedTableName,
                        referencedColumnName);

                logger.debug("Found FK reference {} -> {} with key sequence {}",
                        new Object[] {qualifiedFromColumnName,
                                qualifiedToColumnName, keySeq});

                referenceArray =
                        ensureMinimumArrayLength(referenceArray, keySeq + 1);
                referenceArray[keySeq] = reference;
            }
            if (relationship != null) {
                installRelationship(table, relationship, referenceArray);
            }
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }

    protected void installRelationship(Table table, ForeignKey relationship,
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
            logger.warn("Foreign key {} is empty. Discarding.",
                    relationship.getForeignKeyName());
            return;
        }
        table.getForeignKeys().add(relationship);
        logger.debug("Installed FK {} with number of columns: {}",
                relationship.getForeignKeyName(),
                relationship.getReferences().size());
    }

    protected String decodeUpdateDeleteRule(short rule) {
        switch (rule) {
            case DatabaseMetaData.importedKeyNoAction:
            case DatabaseMetaData.importedKeyRestrict:
                return ForeignKey.RULE_NO_ACTION;
            case DatabaseMetaData.importedKeyCascade:
                return ForeignKey.RULE_CASCADE;
            case DatabaseMetaData.importedKeySetNull:
                return ForeignKey.RULE_SET_NULL;
            case DatabaseMetaData.importedKeySetDefault:
                return ForeignKey.RULE_SET_DEFAULT;
            default:
                throw new IllegalArgumentException("Rule: " + rule);
        }
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    protected PrimaryKeyColumn[] ensureMinimumArrayLength(PrimaryKeyColumn[] oldArray, int length) {
        if (oldArray.length >= length) {
            return oldArray;
        }
        PrimaryKeyColumn[] newArray = new PrimaryKeyColumn[length];
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
