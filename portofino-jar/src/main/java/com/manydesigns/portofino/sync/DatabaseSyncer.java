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

package com.manydesigns.portofino.sync;

import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.connections.ConnectionProvider;
import com.manydesigns.portofino.database.DbUtil;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.logic.DataModelLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.annotations.Annotated;
import com.manydesigns.portofino.model.annotations.Annotation;
import com.manydesigns.portofino.model.datamodel.*;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.structure.ForeignKeyConstraintType;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DatabaseSyncer {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static final String TABLE_SCHEM = "TABLE_SCHEM";
    public static final String INFORMATION_SCHEMA = "INFORMATION_SCHEMA";

    public static final Logger logger =
            LoggerFactory.getLogger(DatabaseSyncer.class);

    protected final ConnectionProvider connectionProvider;

    public DatabaseSyncer(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public Database syncDatabase(Model sourceModel) throws Exception {
        String databaseName = connectionProvider.getDatabaseName();
        Database targetDatabase = new Database();
        targetDatabase.setDatabaseName(databaseName);

        Connection conn = null;
        try {
            logger.debug("Acquiring connection");
            conn = connectionProvider.acquireConnection();

            logger.debug("Reading database metadata");
            DatabaseMetaData metadata = conn.getMetaData();

            logger.debug("Creating Liquibase connection");
            DatabaseConnection liquibaseConnection =
                    new JdbcConnection(conn);

            logger.debug("Retrieving source database");
            Database sourceDatabase =
                    DataModelLogic.findDatabaseByName(sourceModel, databaseName);
            if (sourceDatabase == null) {
                logger.debug("Source database not found. Creating an empty one.");
                sourceDatabase = new Database();
            }

            logger.debug("Reading schema names from metadata");
            List<String> schemaNames =
                    readSchemaNames(conn, connectionProvider, metadata);
            DatabaseSnapshotGeneratorFactory dsgf =
                    DatabaseSnapshotGeneratorFactory.getInstance();
            for (String schemaName : schemaNames) {
                logger.info("Processing schema: {}", schemaName);
                Schema sourceSchema =
                        DataModelLogic.findSchemaByName(
                                sourceDatabase, schemaName);
                if (sourceSchema == null) {
                    logger.debug("Source schema not found. Creating an empty one.");
                    sourceSchema = new Schema();
                    sourceSchema.setSchemaName(schemaName);
                }
                logger.debug("Creating Liquibase database");
                DatabasePlatform databasePlatform =
                        connectionProvider.getDatabasePlatform();

                liquibase.database.Database liquibaseDatabase =
                        databasePlatform.createLiquibaseDatabase();
                liquibaseDatabase.setConnection(liquibaseConnection);
                logger.debug("Creating Liquibase database snapshot");
                DatabaseSnapshot snapshot =
                        dsgf.createSnapshot(liquibaseDatabase, schemaName, null);

                logger.debug("Synchronizing schema");
                Schema targetSchema = new Schema();
                targetSchema.setDatabase(targetDatabase);
                targetDatabase.getSchemas().add(targetSchema);
                syncSchema(snapshot, sourceSchema, targetSchema);
            }
        } finally {
            connectionProvider.releaseConnection(conn);
        }
        return targetDatabase;
    }

    public Schema syncSchema(DatabaseSnapshot databaseSnapshot, Schema sourceSchema, Schema targetSchema) {
        logger.debug("Synchronizing schema: {}", sourceSchema.getSchemaName());
        targetSchema.setSchemaName(sourceSchema.getSchemaName());

        syncTables(databaseSnapshot, sourceSchema, targetSchema);

        syncPrimaryKeys(databaseSnapshot, sourceSchema, targetSchema);

        syncForeignKeys(databaseSnapshot, sourceSchema, targetSchema);

        return targetSchema;
    }

    protected void syncForeignKeys(DatabaseSnapshot databaseSnapshot, Schema sourceSchema, Schema targetSchema) {
        for(liquibase.database.structure.ForeignKey liquibaseFK : databaseSnapshot.getForeignKeys()) {
            String fkTableName = liquibaseFK.getForeignKeyTable().getName();
            Table sourceTable = DataModelLogic.findTableByName(sourceSchema, fkTableName);
            String fkName = liquibaseFK.getName();

            Table targetFromTable = DataModelLogic.findTableByName(targetSchema, fkTableName);
            if (targetFromTable == null) {
                logger.error("Table '{}' not found in schema '{}'. Skipping foreign key: {}",
                        new Object[] {
                                fkTableName,
                                targetSchema.getSchemaName(),
                                fkName
                        });
                continue;
            }

            ForeignKey targetFK = new ForeignKey(targetFromTable);
            targetFK.setName(fkName);

            liquibase.database.structure.Table liquibasePkTable =
                    liquibaseFK.getPrimaryKeyTable();

            targetFK.setToDatabase(targetSchema.getDatabaseName());

            String pkSchemaName = liquibasePkTable.getSchema();
            String pkTableName = liquibasePkTable.getName();
            targetFK.setToSchema(pkSchemaName);
            targetFK.setToTableName(pkTableName);
            if (pkSchemaName == null || pkTableName == null) {
                logger.error("Null schema or table name: foreign key " +
                        "(schema: {}, table: {}, fk: {}) " +
                        "references primary key (schema: {}, table{}). Skipping foreign key.",
                        new Object[] {
                            targetFromTable.getSchemaName(),
                            targetFromTable.getTableName(),
                            fkName,
                            pkSchemaName,
                            pkTableName
                        }
                );
                continue;
            }

            Database targetDatabase = targetSchema.getDatabase();
            Schema pkSchema = DataModelLogic.findSchemaByName(
                    targetDatabase, pkSchemaName);
            if (pkSchema == null) {
                logger.error("Cannot find referenced schema: {}. Skipping foreign key.", pkSchemaName);
                continue;
            }
            Table pkTable =
                    DataModelLogic.findTableByName(pkSchema, pkTableName);
            if (pkTable == null) {
                logger.error("Cannot find referenced table (schema: {}, table: {}). Skipping foreign key.",
                        pkSchemaName, pkTableName);
                continue;
            }

            ForeignKeyConstraintType updateRule =
                    liquibaseFK.getUpdateRule();
            if (updateRule == null) {
                updateRule = ForeignKeyConstraintType.importedKeyRestrict;
                logger.warn("Foreign key '{}' with null update rule. Using: {}",
                        fkName, updateRule.name());
            }
            targetFK.setOnUpdate(updateRule.name());

            ForeignKeyConstraintType deleteRule =
                    liquibaseFK.getDeleteRule();
            if (deleteRule == null) {
                deleteRule = ForeignKeyConstraintType.importedKeyRestrict;
                logger.warn("Foreign key '{}' with null delete rule. Using: {}",
                        fkName, deleteRule.name());
            }
            targetFK.setOnDelete(deleteRule.name());

            String[] fromColumnNames = liquibaseFK.getForeignKeyColumns().split(",");
            String[] toColumnNames = liquibaseFK.getPrimaryKeyColumns().split(",");
            if(fromColumnNames.length != toColumnNames.length) {
                logger.error("Invalid foreign key {} - columns don't match", fkName);
                continue;
            }

            boolean referencesHaveErrors = false;
            for(int i = 0; i < fromColumnNames.length; i++) {
                String fromColumnName = fromColumnNames[i].trim();
                String toColumnName = toColumnNames[i].trim();

                Column fromColumn =
                        DataModelLogic.findColumnByName(
                                targetFromTable, fromColumnName);
                if (fromColumn == null) {
                    logger.error("Cannot find from column (schema: {}, table: {}, column: {}).",
                            new Object[] {
                                    targetFromTable.getSchemaName(),
                                    targetFromTable.getTableName(),
                                    fromColumnName
                            });
                    referencesHaveErrors = true;
                }

                Column toColumn =
                        DataModelLogic.findColumnByName(pkTable, toColumnName);
                if (toColumn == null) {
                    logger.error("Cannot find to column (schema: {}, table: {}, column: {}).",
                            new Object[] {
                                    pkTable.getSchemaName(),
                                    pkTable.getTableName(),
                                    toColumn
                            });
                    referencesHaveErrors = true;
                }

                Reference reference = new Reference();
                reference.setOwner(targetFK);
                reference.setFromColumn(fromColumnName);
                reference.setToColumn(toColumnName);
                targetFK.getReferences().add(reference);
            }

            if (referencesHaveErrors) {
                logger.error("Skipping foreign key (schema: {}, table: {}, fk: {}) because of errors.",
                        new Object[] {
                                pkTable.getSchemaName(),
                                pkTable.getTableName(),
                                fkName
                        });
                continue;
            }

            //TODO ricercare per struttura? Es. rename
            ForeignKey sourceFK;
            if (sourceTable == null) {
                sourceFK = null;
            } else {
                sourceFK = sourceTable.findForeignKeyByName(fkName);
            }

            if(sourceFK != null) {
                targetFK.setManyPropertyName(sourceFK.getManyPropertyName());
                targetFK.setOnePropertyName(sourceFK.getOnePropertyName());
            }

            logger.debug("FK creation successfull. Adding FK to table.");
            targetFromTable.getForeignKeys().add(targetFK);
        }
    }

    protected void syncPrimaryKeys(DatabaseSnapshot databaseSnapshot, Schema sourceSchema, Schema targetSchema) {
        logger.debug("Synchronizing primary keys");
        for(liquibase.database.structure.PrimaryKey liquibasePK : databaseSnapshot.getPrimaryKeys()) {
            String pkTableName = liquibasePK.getTable().getName();

            Table sourceTable = DataModelLogic.findTableByName(sourceSchema, pkTableName);
            PrimaryKey sourcePK;
            if (sourceTable == null) {
                sourcePK = null;
            } else {
                sourcePK = sourceTable.getPrimaryKey();
            }

            Table targetTable = DataModelLogic.findTableByName(targetSchema, pkTableName);
            if (targetTable == null) {
                logger.error("Coud not find table: {}. Skipping PK.",
                        pkTableName
                );
            }

            PrimaryKey targetPK = new PrimaryKey(targetTable);
            String primaryKeyName = liquibasePK.getName();
            targetPK.setPrimaryKeyName(primaryKeyName);

            List<String> columnNamesAsList = liquibasePK.getColumnNamesAsList();
            if (columnNamesAsList == null || columnNamesAsList.isEmpty()) {
                logger.error("Primary key (table: {}, pk: {}) has no columns. Skipping PK.",
                        pkTableName,
                        primaryKeyName
                );
                continue;
            }

            boolean pkColumnsHaveErrors = false;
            for(String columnName : columnNamesAsList) {
                PrimaryKeyColumn targetPKColumn = new PrimaryKeyColumn(targetPK);
                targetPKColumn.setColumnName(columnName);

                Column pkColumn = targetTable.findColumnByName(columnName);
                if (pkColumn == null) {
                    logger.error("Primary key (table: {}, pk: {}) has invalid column: {}",
                            new Object[] {
                                pkTableName,
                                primaryKeyName,
                                columnName
                            }
                    );
                    pkColumnsHaveErrors = true;
                }

                if(sourcePK != null) {
                    PrimaryKeyColumn sourcePKColumn =
                            sourcePK.findPrimaryKeyColumnByName(columnName);
                    if(sourcePKColumn != null) {
                        logger.debug("Found source PK column: {}", columnName);
                        Generator sourceGenerator = sourcePKColumn.getGenerator();
                        if(sourceGenerator != null) {
                            logger.debug("Found generator: {}", sourceGenerator);
                            Generator targetGenerator =
                                    (Generator) ReflectionUtil.newInstance(sourceGenerator.getClass());

                            try {
                                BeanUtils.copyProperties(targetGenerator, sourceGenerator);
                            } catch (Exception e) {
                                logger.error("Couldn't copy generator", e);
                            }

                            targetGenerator.setPrimaryKeyColumn(targetPKColumn);
                            targetPKColumn.setGenerator(sourcePKColumn.getGenerator());
                        }
                    }
                }

                targetPK.getPrimaryKeyColumns().add(targetPKColumn);
            }

            if (pkColumnsHaveErrors) {
                logger.error("Primary key (table: {}, pk: {}) has problems with columns. Skipping PK.",
                        pkTableName,
                        primaryKeyName
                );
                continue;
            }

            logger.debug("PK creation successfull. Installing PK in table.");
            targetTable.setPrimaryKey(targetPK);
        }
    }

    protected void syncTables(DatabaseSnapshot databaseSnapshot, Schema sourceSchema, Schema targetSchema) {
        logger.debug("Synchronizing tables");
        for (liquibase.database.structure.Table liquibaseTable
                : databaseSnapshot.getTables()) {
            logger.debug("Processing table: {}", liquibaseTable.getName());
            Table sourceTable = DataModelLogic.findTableByName(sourceSchema, liquibaseTable.getName());
            if(sourceTable == null) {
                sourceTable = new Table();
            }

            Table targetTable = new Table(targetSchema);
            targetSchema.getTables().add(targetTable);

            targetTable.setTableName(liquibaseTable.getName());

            logger.debug("Merging table attributes and annotations");
            targetTable.setEntityName(sourceTable.getEntityName());
            targetTable.setJavaClass(sourceTable.getJavaClass());
            targetTable.setManyToMany(sourceTable.getManyToMany());
            copyAnnotations(sourceTable, targetTable);

            syncColumns(liquibaseTable, sourceTable, targetTable);

            copySelectionProviders(sourceTable, targetTable);
        }
    }

    protected void copySelectionProviders(Table sourceTable, Table targetTable) {
        for(ModelSelectionProvider sourceSP : sourceTable.getSelectionProviders()) {
            ModelSelectionProvider targetSP =
                    (ModelSelectionProvider) ReflectionUtil.newInstance(sourceSP.getClass());
            try {
                BeanUtils.copyProperties(targetSP, sourceSP);
                targetSP.setFromTable(targetTable);
                targetSP.setToTable(null);
                targetTable.getSelectionProviders().add(targetSP);
                for (Reference sourceReference : sourceSP.getReferences()) {
                    Reference targetReference = new Reference(targetSP);
                    targetSP.getReferences().add(targetReference);
                    targetReference.setFromColumn(sourceReference.getFromColumn());
                    targetReference.setToColumn(sourceReference.getToColumn());
                }
            } catch (Exception e) {
                logger.error("Couldn't copy selection provider {}", sourceSP);
            }
        }
    }

    protected void syncColumns(liquibase.database.structure.Table liquibaseTable, Table sourceTable, Table targetTable) {
        logger.debug("Synchronizing columns");
        for(liquibase.database.structure.Column liquibaseColumn : liquibaseTable.getColumns()) {
            logger.debug("Processing column: {}", liquibaseColumn.getName());

            Column targetColumn = new Column(targetTable);

            targetColumn.setColumnName(liquibaseColumn.getName());

            logger.debug("Merging column attributes and annotations");
            targetColumn.setAutoincrement(liquibaseColumn.isAutoIncrement());
            int jdbcType = liquibaseColumn.getDataType();
            String typeName = liquibaseColumn.getTypeName();
            targetColumn.setJdbcType(jdbcType);
            targetColumn.setColumnType(liquibaseColumn.getTypeName());
            targetColumn.setLength(liquibaseColumn.getColumnSize());
            targetColumn.setNullable(liquibaseColumn.isNullable());
            targetColumn.setScale(liquibaseColumn.getDecimalDigits());
            //TODO liquibaseColumn.getLengthSemantics()

            Column sourceColumn = DataModelLogic.findColumnByName(sourceTable, liquibaseColumn.getName());
            if(sourceColumn != null) {
                targetColumn.setPropertyName(sourceColumn.getPropertyName());
                targetColumn.setJavaType(sourceColumn.getJavaType());
                copyAnnotations(sourceColumn, targetColumn);
            }
            if(targetColumn.getJavaType() == null) {
                Class defaultJavaType = Type.getDefaultJavaType(jdbcType);

                if (defaultJavaType != null) {
                    targetColumn.setJavaType(defaultJavaType.getName());
                } else {
                    logger.error("Cannot find Java type for table: {}, column: {}, jdbc type: {}, type name: {}. Skipping column.",
                            new Object[]{targetTable.getTableName(),
                                    targetColumn.getColumnName(),
                                    jdbcType,
                                    typeName
                            });
                    continue;
                }

                org.hibernate.type.Type hibernateType =
                        DbUtil.getHibernateType(defaultJavaType, jdbcType);
                if (hibernateType == null) {
                    logger.error("Cannot find Hibernate type for table: {}, column: {}, jdbc type: {}, type name: {}. Skipping column.",
                            new Object[]{targetTable.getTableName(),
                                    targetColumn.getColumnName(),
                                    jdbcType,
                                    typeName
                            });
                    continue;
                }

            }

            logger.debug("Column creation successfull. Adding column to table.");
            targetTable.getColumns().add(targetColumn);
        }
    }

    protected void copyAnnotations(Annotated source, Annotated target) {
        for(Annotation ann : source.getAnnotations()) {
            Annotation annCopy = new Annotation();
            annCopy.setParent(target);
            annCopy.setType(ann.getType());
            for(String value : ann.getValues()) {
                annCopy.getValues().add(value);
            }
            target.getAnnotations().add(annCopy);
        }
    }

    protected List<String> readSchemaNames(Connection conn,
                                           ConnectionProvider connectionProvider,
                                           DatabaseMetaData metadata)
            throws SQLException {
        logger.debug("Searching for schemas in: {}",
                connectionProvider.getDatabaseName());

        List<String> result = new ArrayList<String>();

        Pattern includePattern = connectionProvider.getIncludeSchemasPattern();
        Pattern excludePattern = connectionProvider.getExcludeSchemasPattern();
        List<String> schemaNames =
                connectionProvider.getDatabasePlatform().getSchemaNames(metadata);
        for(String schemaName : schemaNames) {
            if (INFORMATION_SCHEMA.equalsIgnoreCase(schemaName)) {
                logger.info("Skipping information schema: {}", schemaName);
                continue;
            }
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
            logger.info("Found schema: {}", schemaName);
            result.add(schemaName);
        }
        return result;
    }




}
