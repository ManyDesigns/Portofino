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

package com.manydesigns.portofino.database.platforms;

import com.manydesigns.portofino.model.connections.ConnectionProvider;
import com.manydesigns.portofino.database.DbUtil;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.model.datamodel.*;
import org.apache.commons.dbutils.DbUtils;
import org.hibernate.dialect.MySQLDialect;

import java.sql.*;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class MySql5DatabasePlatform extends AbstractDatabasePlatform {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static String DESCRIPTION = "MySQL 5.x";
    public final static String STANDARD_DRIVER_CLASS_NAME =
            "com.mysql.jdbc.Driver";

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public MySql5DatabasePlatform() {
        super(new MySQLDialect());
    }

    //**************************************************************************
    // Implementation of DatabaseAbstraction
    //**************************************************************************

    public String getDescription() {
        return DESCRIPTION;
    }

    public String getStandardDriverClassName() {
        return STANDARD_DRIVER_CLASS_NAME;
    }

    public boolean isApplicable(ConnectionProvider connectionProvider) {
        return "MySQL".equals(connectionProvider.getDatabaseProductName());
    }

    //**************************************************************************
    // Read schemas
    //**************************************************************************

    @Override
    protected void readSchemas(Connection conn,
                               ConnectionProvider connectionProvider,
                               DatabaseMetaData metadata, Database database)
            throws SQLException {
        logger.debug("Searching for schemas...");
        ResultSet rs = null;
        Pattern includePattern = connectionProvider.getIncludeSchemasPattern();
        Pattern excludePattern = connectionProvider.getExcludeSchemasPattern();
        try {
            rs = metadata.getCatalogs();
            while(rs.next()) {
                String schemaName = rs.getString(TABLE_CAT);
                if (includePattern != null) {
                    Matcher includeMatcher = includePattern.matcher(schemaName);
                    if (!includeMatcher.matches()) {
                        logger.info("Schema '{}' does not match include " +
                                "pattern '{}'. Skipping this schema.",
                                schemaName,
                                connectionProvider.getIncludeSchemas());
                        continue;
                    }
                }
                if (excludePattern != null) {
                    Matcher excludeMatcher = excludePattern.matcher(schemaName);
                    if (excludeMatcher.matches()) {
                        logger.info("Schema '{}' matches exclude pattern " +
                                "'{}'. Skipping this schema.",
                                schemaName,
                                connectionProvider.getExcludeSchemas());
                        continue;
                    }
                }
                Schema schema = new Schema(database, schemaName);
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

    @Override
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
            rs = metadata.getTables(expectedSchemaName, null, null, tableTypes);
            while(rs.next()) {
                String schemaName = rs.getString(TABLE_CAT);
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

                Table table = new Table(schema, tableName);
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

    @Override
    protected void readColumns(Connection conn,
                               ConnectionProvider connectionProvider,
                               DatabaseMetaData metadata, Table table)
            throws SQLException {
        String expectedSchemaName = table.getSchemaName();
        String expectedTableName = table.getTableName();
        logger.debug("Searching for columns in table {}",
                table.getQualifiedName());
        String sql = MessageFormat.format("SELECT * FROM `{0}`.`{1}`",
                expectedSchemaName, expectedTableName);
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(sql);
            rs = st.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String columnName = rsmd.getColumnName(i);
                String columnType = rsmd.getColumnTypeName(i);
                boolean nullable = (rsmd.isNullable(i)
                        == ResultSetMetaData.columnNullable);
                int length = rsmd.getPrecision(i);
                int scale = rsmd.getScale(i);
                boolean autoincrement = rsmd.isAutoIncrement(i);
                boolean searchable = rsmd.isSearchable(i);

                Column column = new Column(table,
                        columnName, columnType,
                        nullable, autoincrement,
                        length, scale, searchable);
                logger.debug("Found column: {} of type {}",
                        column.getQualifiedName(),
                        column.getColumnType());
                Type type = connectionProvider.getTypeByName(columnType);
                column.setJavaType(type.getDefaultJavaType().getName());

                table.getColumns().add(column);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(st);
        }
    }



    //**************************************************************************
    // Read Primary keys
    //**************************************************************************

    @Override
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

            rs = metadata.getPrimaryKeys(expectedSchemaName, null,
                    expectedTableName);
            while(rs.next()) {
                String schemaName = rs.getString(TABLE_CAT);
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
                    primaryKey = new PrimaryKey(table, pkName);
                    logger.debug("Found primary key: {}", pkName);
                } else if (!primaryKey.getPrimaryKeyName().equals(pkName)) {
                    //sanity check
                    logger.debug("Found new PK name {} different " +
                            "from previous name {}",
                            pkName, primaryKey.getPrimaryKeyName());
                    return;
                }

                Column column = table.findColumnByName(columnName);
                PrimaryKeyColumn primaryKeyColumn =
                        new PrimaryKeyColumn(primaryKey, columnName);

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

            // copy non-null elements of array
            for (PrimaryKeyColumn current : pkColumnArray) {
                if (current == null) {
                    continue;
                }
                primaryKey.add(current);
            }
            // sanity check
            if (primaryKey.size() == 0) {
                logger.warn("Primary key {} is empty. Discarding.",
                        primaryKey.getPrimaryKeyName());
                return;
            }
            table.setPrimaryKey(primaryKey);
            logger.debug("Installed PK {} with number of columns: {}",
                    primaryKey.getPrimaryKeyName(),
                    primaryKey.size());
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
    }


    //**************************************************************************
    // Read foreign keys
    //**************************************************************************

    @Override
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
            rs = metadata.getImportedKeys(expectedSchemaName, null,
                    expectedTableName);
            while(rs.next()) {
                String schemaName = rs.getString(FKTABLE_CAT);
                String tableName = rs.getString(FKTABLE_NAME);
                String columnName = rs.getString(FKCOLUMN_NAME);

                String referencedDatabaseName = expectedDatabaseName;
                String referencedSchemaName = rs.getString(PKTABLE_CAT);
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

                    // accrocchio per problemi case-sensitive di mysql
                    Database database = table.getSchema().getDatabase();
                    for (Table currentTable : database.getAllTables()) {
                        if (currentTable.getSchemaName()
                                .equalsIgnoreCase(referencedSchemaName)
                                && currentTable.getTableName()
                                .equalsIgnoreCase(referencedTableName)) {
                            referencedSchemaName = currentTable.getSchemaName();
                            referencedTableName = currentTable.getTableName();
                            break;
                        }
                    }

                    relationship = new ForeignKey(
                            table, fkName,
                            referencedDatabaseName,
                            referencedSchemaName,
                            referencedTableName,
                            decodeUpdateDeleteRule(updateRule),
                            decodeUpdateDeleteRule(deleteRule));

                    // reset the refernceArray
                    referenceArray = new Reference[0];
                    logger.debug("Found foreign key: {}", fkName);
                }

                Reference reference = new Reference(
                        relationship,
                        columnName,
                        referencedColumnName);

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
                        new Object[] {
                                qualifiedFromColumnName,
                                qualifiedToColumnName,
                                keySeq});

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


}
