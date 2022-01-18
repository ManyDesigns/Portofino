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

package com.manydesigns.portofino.sync;

import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.database.Type;
import com.manydesigns.portofino.model.Annotated;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import liquibase.CatalogAndSchema;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.ForeignKeyConstraintType;
import liquibase.structure.core.Relation;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.JSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Types;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DatabaseSyncer {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(DatabaseSyncer.class);

    protected final ConnectionProvider connectionProvider;

    public DatabaseSyncer(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public Database syncDatabase(Model sourceModel) throws Exception {
        String databaseName = connectionProvider.getDatabase().getDatabaseName();
        Database targetDatabase = new Database();
        targetDatabase.setDatabaseName(databaseName);

        logger.debug("Acquiring connection");
        try(Connection conn = connectionProvider.acquireConnection()) {
            logger.debug("Creating Liquibase connection");
            DatabaseConnection liquibaseConnection = new JdbcConnection(conn);

            logger.debug("Retrieving source database");
            Database sourceDatabase =
                    DatabaseLogic.findDatabaseByName(sourceModel, databaseName);
            if (sourceDatabase == null) {
                logger.debug("Source database not found. Creating an empty one.");
                sourceDatabase = new Database();
            } else {
                logger.debug("Source database was already configured. Copying true string and false string.");
                targetDatabase.setTrueString(sourceDatabase.getTrueString());
                targetDatabase.setFalseString(sourceDatabase.getFalseString());
                targetDatabase.setEntityMode(sourceDatabase.getEntityMode());
            }

            logger.debug("Reading schema names from metadata");
            List<Schema> schemas = connectionProvider.getDatabase().getSchemas();

            SnapshotGeneratorFactory dsgf = SnapshotGeneratorFactory.getInstance();

            logger.debug("Finding Liquibase database");
            DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
            liquibase.database.Database liquibaseDatabase =
                    databaseFactory.findCorrectDatabaseImplementation(liquibaseConnection);

            for (Schema schema : schemas) {
                String schemaName = schema.getSchemaName();
                String actualSchemaName = schema.getActualSchemaName();
                logger.info("Processing schema: {}", actualSchemaName);
                Schema sourceSchema = DatabaseLogic.findSchemaByNameIgnoreCase(sourceDatabase, schemaName);
                if (sourceSchema == null) {
                    logger.debug("Source schema not found. Creating an empty one.");
                    sourceSchema = new Schema();
                    sourceSchema.setSchemaName(schemaName);
                    sourceSchema.setActualSchemaName(actualSchemaName);
                    sourceSchema.setCatalog(schema.getCatalog());
                    sourceSchema.setDatabase(sourceDatabase);
                }

                logger.debug("Creating Liquibase database snapshot");
                String catalog = null;
                if(sourceSchema.getCatalog() != null) {
                    catalog = sourceSchema.getCatalog();
                }
                SnapshotControl snapshotControl = new SnapshotControl(liquibaseDatabase);
                DatabaseSnapshot snapshot =
                        dsgf.createSnapshot(new CatalogAndSchema(catalog, actualSchemaName), liquibaseDatabase, snapshotControl);

                logger.debug("Synchronizing schema");
                Schema targetSchema = new Schema();
                targetSchema.setDatabase(targetDatabase);
                targetSchema.setCatalog(catalog);
                targetSchema.setSchemaName(sourceSchema.getSchemaName());
                targetSchema.setActualSchemaName(sourceSchema.getActualSchemaName());
                targetDatabase.getSchemas().add(targetSchema);
                syncSchema(snapshot, sourceSchema, targetSchema);
            }
        }
        targetDatabase.setConnectionProvider(connectionProvider);
        connectionProvider.setDatabase(targetDatabase);
        return targetDatabase;
    }

    public Schema syncSchema(DatabaseSnapshot databaseSnapshot, Schema sourceSchema, Schema targetSchema) {
        logger.info("Synchronizing schema: {}", sourceSchema.getActualSchemaName());
        copyAnnotations(sourceSchema, targetSchema);
        syncTables(databaseSnapshot, sourceSchema, targetSchema);
        syncViews(databaseSnapshot, sourceSchema, targetSchema);
        syncPrimaryKeys(databaseSnapshot, sourceSchema, targetSchema);
        syncForeignKeys(databaseSnapshot, sourceSchema, targetSchema);
        return targetSchema;
    }

    protected void syncForeignKeys(DatabaseSnapshot databaseSnapshot, Schema sourceSchema, Schema targetSchema) {
        logger.info("Synchronizing foreign keys");
        for(liquibase.structure.core.ForeignKey liquibaseFK : databaseSnapshot.get(liquibase.structure.core.ForeignKey.class)) {
            String fkName = liquibaseFK.getName();
            logger.info("Synchronizing foreign key {}", fkName);
            String fkTableName = liquibaseFK.getForeignKeyTable().getName();
            Table sourceTable = DatabaseLogic.findTableByNameIgnoreCase(sourceSchema, fkTableName);

            Table targetFromTable = DatabaseLogic.findTableByNameIgnoreCase(targetSchema, fkTableName);
            if (targetFromTable == null) {
                logger.error("Table '{}' not found in schema '{}'. Skipping foreign key: {}",
                                fkTableName, targetSchema.getSchemaName(), fkName);
                continue;
            }

            ForeignKey targetFK = new ForeignKey(targetFromTable);
            targetFK.setName(fkName);

            liquibase.structure.core.Table liquibasePkTable =
                    liquibaseFK.getPrimaryKeyTable();

            targetFK.setToDatabase(targetSchema.getDatabaseName());

            String pkSchemaName = liquibasePkTable.getSchema().getName();
            String pkTableName = normalizeTableName(liquibasePkTable, databaseSnapshot);
            String logicalSchemaName = pkSchemaName;
            for(Schema schema : sourceSchema.getDatabase().getSchemas()) {
                if(schema.getActualSchemaName().equals(pkSchemaName)) {
                    logicalSchemaName = schema.getSchemaName();
                    logger.debug("Logical name for schema " + pkSchemaName + " is " + logicalSchemaName);
                    break;
                }
            }
            targetFK.setToSchema(logicalSchemaName);
            targetFK.setToTableName(pkTableName);
            if (logicalSchemaName == null || pkTableName == null) {
                logger.error("Null schema or table name: foreign key " +
                        "(schema: {}, table: {}, fk: {}) " +
                        "references primary key (schema: {}, table: {}). Skipping foreign key.",
                        targetFromTable.getSchemaName(),
                        targetFromTable.getTableName(),
                        fkName,
                        logicalSchemaName,
                        pkTableName);
                continue;
            }

            Database targetDatabase = targetSchema.getDatabase();
            Schema pkSchema = DatabaseLogic.findSchemaByNameIgnoreCase(
                    targetDatabase, logicalSchemaName);
            if (pkSchema == null) {
                logger.error("Cannot find referenced schema: {}. Skipping foreign key.", logicalSchemaName);
                continue;
            }
            Table pkTable =
                    DatabaseLogic.findTableByNameIgnoreCase(pkSchema, pkTableName);
            if (pkTable == null) {
                logger.error("Cannot find referenced table (schema: {}, table: {}). Skipping foreign key.",
                        logicalSchemaName, pkTableName);
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

            List<liquibase.structure.core.Column> fromColumns = liquibaseFK.getForeignKeyColumns();
            List<liquibase.structure.core.Column> toColumns = liquibaseFK.getPrimaryKeyColumns();
            if(fromColumns.size() != toColumns.size()) {
                logger.error("Invalid foreign key {} - columns don't match", fkName);
                continue;
            }

            boolean referencesHaveErrors = false;
            for(int i = 0; i < fromColumns.size(); i++) {
                String fromColumnName = fromColumns.get(i).getName();
                String toColumnName = toColumns.get(i).getName();

                Column fromColumn =
                        DatabaseLogic.findColumnByNameIgnoreCase(
                                targetFromTable, fromColumnName);
                if (fromColumn == null) {
                    logger.error("Cannot find from column (schema: {}, table: {}, column: {}).",
                            targetFromTable.getSchemaName(),
                            targetFromTable.getTableName(),
                            fromColumnName);
                    referencesHaveErrors = true;
                    break;
                }

                Column toColumn =
                        DatabaseLogic.findColumnByNameIgnoreCase(pkTable, toColumnName);
                if (toColumn == null) {
                    logger.error("Cannot find to column (schema: {}, table: {}, column: {}).",
                            pkTable.getSchemaName(),
                            pkTable.getTableName(),
                            toColumnName);
                    referencesHaveErrors = true;
                    break;
                }

                Reference reference = new Reference();
                reference.setOwner(targetFK);
                reference.setFromColumn(fromColumn.getColumnName());
                reference.setToColumn(toColumn.getColumnName());
                targetFK.getReferences().add(reference);
            }

            if (referencesHaveErrors) {
                logger.error("Skipping foreign key (schema: {}, table: {}, fk: {}) because of errors.",
                        pkTable.getSchemaName(),
                        pkTable.getTableName(),
                        fkName);
                continue;
            }

            //TODO ricercare per struttura? Es. rename
            ForeignKey sourceFK;
            if (sourceTable == null) {
                sourceFK = null;
            } else {
                sourceFK = DatabaseLogic.findForeignKeyByNameIgnoreCase(sourceTable, fkName);
            }

            if(sourceFK != null) {
                logger.debug("Found a foreign key with the same name in the previous version of the schema");
                targetFK.setManyPropertyName(sourceFK.getManyPropertyName());
                targetFK.setOnePropertyName(sourceFK.getOnePropertyName());
            }

            logger.debug("FK creation successful. Adding FK to table.");
            targetFromTable.getForeignKeys().add(targetFK);
        }
    }

    protected String normalizeTableName(liquibase.structure.core.Table table, DatabaseSnapshot databaseSnapshot) {
        //Work around MySQL & case-insensitive dbs
        liquibase.structure.core.Table fkTable = databaseSnapshot.get(table);
        String fkTableName;

        if(fkTable != null) {
            fkTableName = fkTable.getName();
        } else {
            logger.warn("Could not normalize table name: " + table.getName() + "; most probably this is a sign of a foreign key to another schema, which is not supported at the moment.");
            fkTableName = null;
        }

        return fkTableName;
    }

    protected void syncPrimaryKeys(DatabaseSnapshot databaseSnapshot, Schema sourceSchema, Schema targetSchema) {
        logger.info("Synchronizing primary keys");
        for(liquibase.structure.core.PrimaryKey liquibasePK : databaseSnapshot.get(liquibase.structure.core.PrimaryKey.class)) {
            String pkTableName = liquibasePK.getTable().getName();

            Table sourceTable = DatabaseLogic.findTableByNameIgnoreCase(sourceSchema, pkTableName);
            PrimaryKey sourcePK;
            if (sourceTable == null) {
                sourcePK = null;
            } else {
                sourcePK = sourceTable.getPrimaryKey();
            }

            Table targetTable = DatabaseLogic.findTableByNameIgnoreCase(targetSchema, pkTableName);
            if (targetTable == null) {
                logger.error("Could not find table: {}. Skipping PK.", pkTableName);
                continue;
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

                Column pkColumn = DatabaseLogic.findColumnByNameIgnoreCase(targetTable, columnName);
                if (pkColumn == null) {
                    logger.error("Primary key (table: {}, pk: {}) has invalid column: {}",
                            new Object[] {
                                pkTableName,
                                primaryKeyName,
                                columnName
                            }
                    );
                    pkColumnsHaveErrors = true;
                    break;
                }
                targetPKColumn.setColumnName(pkColumn.getColumnName());

                if(sourcePK != null) {
                    PrimaryKeyColumn sourcePKColumn =
                            sourcePK.findPrimaryKeyColumnByNameIgnoreCase(columnName);
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
        logger.info("Synchronizing tables");
        for (liquibase.structure.core.Table liquibaseTable : databaseSnapshot.get(liquibase.structure.core.Table.class)) {
            String tableName = liquibaseTable.getName();
            logger.info("Processing table: {}", tableName);
            Table sourceTable = DatabaseLogic.findTableByNameIgnoreCase(sourceSchema, tableName);
            if(sourceTable == null) {
                logger.debug("Added new table: {}", tableName);
                sourceTable = new Table();
            }
            Table targetTable = new Table(targetSchema);
            targetSchema.getTables().add(targetTable);

            targetTable.setTableName(tableName);

            logger.debug("Merging table attributes and annotations");
            targetTable.setEntityName(sourceTable.getEntityName());
            targetTable.setJavaClass(sourceTable.getJavaClass());
            targetTable.setShortName(sourceTable.getShortName());
            copyAnnotations(sourceTable, targetTable);

            syncColumns(liquibaseTable, sourceTable, targetTable);

            copySelectionProviders(sourceTable, targetTable);
        }
    }

    protected void syncViews(DatabaseSnapshot databaseSnapshot, Schema sourceSchema, Schema targetSchema) {
        logger.info("Synchronizing views");
        for (liquibase.structure.core.View liquibaseView : databaseSnapshot.get(liquibase.structure.core.View.class)) {
            String viewName = liquibaseView.getName();
            logger.info("Processing view: {}", viewName);
            Table sourceTable = DatabaseLogic.findTableByNameIgnoreCase(sourceSchema, viewName);
            if(sourceTable == null) {
                logger.debug("Added new table: {}", viewName);
                sourceTable = new Table();
                //Attempt to deduce the primary key from the referenced table
            }
            if(sourceTable.getPrimaryKey() == null ||
               sourceTable.getPrimaryKey().getPrimaryKeyColumns() == null ||
               sourceTable.getPrimaryKey().getPrimaryKeyColumns().isEmpty()) {
                //tryToDeterminePrimaryKey(liquibaseView);
            }
            View targetView = new View(targetSchema);
            if(sourceTable instanceof View) {
                View sourceView = (View) sourceTable;
                targetView.setInsertable(sourceView.isInsertable());
                targetView.setUpdatable(sourceView.isUpdatable());
            }
            targetSchema.getTables().add(targetView);

            targetView.setTableName(viewName);

            logger.debug("Merging view attributes and annotations");
            targetView.setEntityName(sourceTable.getEntityName());
            targetView.setJavaClass(sourceTable.getJavaClass());
            targetView.setShortName(sourceTable.getShortName());
            copyAnnotations(sourceTable, targetView);

            syncColumns(liquibaseView, sourceTable, targetView);

            copySelectionProviders(sourceTable, targetView);

            PrimaryKey sourcePk = sourceTable.getPrimaryKey();
            if(sourcePk != null) {
                PrimaryKey targetPk = new PrimaryKey(targetView);
                for (PrimaryKeyColumn sourcePkColumn : sourcePk.getPrimaryKeyColumns()) {
                    targetPk.add(DatabaseLogic.findColumnByName(targetView, sourcePkColumn.getColumnName()));
                }
                targetView.setPrimaryKey(targetPk);
            }
        }
    }

    /* TODO too sophisticated for now
    protected PrimaryKey tryToDeterminePrimaryKey(liquibase.structure.core.View view) {
        PrimaryKey candidate = null;
        liquibase.structure.core.Column idColumn = null;
        for (liquibase.structure.core.Column column : view.getColumns()) {
            if (column.getName().equalsIgnoreCase("id")) {
                if(idColumn != null) {
                    logger.debug();
                    break;
                }
                idColumn = column;
            }
        }
        String viewDef = view.getDefinition();
        if(viewDef != null) {
            CCJSqlParserManager parserManager = new CCJSqlParserManager();
            try {
                Statement statement = parserManager.parse(new StringReader(viewDef));
                if(!(statement instanceof Select)) {
                    logger.warn("The definition of the view " + view.getName() + "is not a select query: " + viewDef);
                    return candidate;
                }
                SelectBody selectBody = ((Select) statement).getSelectBody();
                if(!(selectBody instanceof PlainSelect)) {
                    logger.warn("The definition of the view " + view.getName() + "is not a plain select query: " + viewDef);
                    return candidate;
                }
                PlainSelect select = (PlainSelect) selectBody;
                FromItem fromItem = select.getFromItem();
                if(!(fromItem instanceof net.sf.jsqlparser.schema.Table)) {
                    logger.info("Cannot determine the primary key for the view " + view.getName() + " as it's not a select from a single table");
                    return candidate;
                }
                net.sf.jsqlparser.schema.Table table = (net.sf.jsqlparser.schema.Table) fromItem;
                //TODO
            } catch (JSQLParserException e) {
                logger.warn("Could not parse the definition of the view " + view.getName() + " to determine the primary key", e);
            }
        }
        return candidate;
    }*/

    protected void copySelectionProviders(Table sourceTable, Table targetTable) {
        for(ModelSelectionProvider sourceSP : sourceTable.getSelectionProviders()) {
            ModelSelectionProvider targetSP =
                    (ModelSelectionProvider) ReflectionUtil.newInstance(sourceSP.getClass());
            try {
                BeanUtils.copyProperties(targetSP, sourceSP);
                targetSP.setFromTable(targetTable);
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

    protected void syncColumns
            (Relation relation, final Table sourceTable, Table targetTable) {
        logger.debug("Synchronizing columns");
        for(liquibase.structure.core.Column liquibaseColumn : relation.getColumns()) {
            logger.debug("Processing column: {}", liquibaseColumn.getName());

            Column targetColumn = new Column(targetTable);

            targetColumn.setColumnName(liquibaseColumn.getName());

            logger.debug("Merging column attributes and annotations");
            targetColumn.setAutoincrement(liquibaseColumn.isAutoIncrement());
            Integer jdbcType = liquibaseColumn.getType().getDataTypeId();
            String jdbcTypeName = liquibaseColumn.getType().getTypeName();
            if("Oracle".equals(connectionProvider.getDatabaseProductName())) {
                //Wait for a bugfix in Liquibase TODO is this still necessary?
                if(jdbcType == null || jdbcType == Types.OTHER) {
                    if(jdbcTypeName != null && jdbcTypeName.toUpperCase().startsWith("TIMESTAMP")) {
                        jdbcType = Types.TIMESTAMP;
                    }
                } else if(jdbcType == Types.DATE) {
                    boolean dateTypePresent = false;
                    for(Type type : connectionProvider.getTypes()) {
                        if(type.getTypeName().equals("DATE")) {
                            dateTypePresent = true;
                            break;
                        }
                    }
                    if(!dateTypePresent) {
                        //DATE type not present, map to TIMESTAMP
                        jdbcType = Types.TIMESTAMP;
                    }
                }
            }
            if(jdbcType == null) {
                jdbcType = Types.OTHER;
            }
            if(jdbcType == Types.OTHER) {
                logger.debug("jdbcType = OTHER, trying to determine more specific type from type name");

                try {
                    Field field = Types.class.getField(jdbcTypeName);
                    jdbcType = (Integer) field.get(null);
                } catch (Exception e) {
                    logger.debug("Could not determine JDBC type (type name = {})", jdbcTypeName);
                }
            }
            targetColumn.setJdbcType(jdbcType);
            targetColumn.setColumnType(jdbcTypeName);
            targetColumn.setLength(liquibaseColumn.getType().getColumnSize());
            targetColumn.setNullable(liquibaseColumn.isNullable());
            targetColumn.setScale(liquibaseColumn.getType().getDecimalDigits());
            //TODO liquibaseColumn.getLengthSemantics()

            Column sourceColumn = DatabaseLogic.findColumnByNameIgnoreCase(sourceTable, liquibaseColumn.getName());
            if(sourceColumn != null) {
                targetColumn.setPropertyName(sourceColumn.getPropertyName());
                targetColumn.setJavaType(sourceColumn.getJavaType());
                copyAnnotations(sourceColumn, targetColumn);
            }

            logger.debug("Column creation successfull. Adding column to table.");
            targetTable.getColumns().add(targetColumn);
        }

        logger.debug("Sorting columns to preserve their previous order as much as possible");
        targetTable.getColumns().sort(new Comparator<Column>() {
            private int oldIndex(Column c) {
                int i = 0;
                for (Column old : sourceTable.getColumns()) {
                    if (old.getColumnName().equals(c.getColumnName())) {
                        return i;
                    }
                    i++;
                }
                return -1;
            }

            public int compare(Column c1, Column c2) {
                int index1 = oldIndex(c1);
                int index2 = oldIndex(c2);
                if (index1 != -1) {
                    if (index2 != -1) {
                        return Integer.compare(index1, index2);
                    } else {
                        return -1;
                    }
                } else {
                    return index2 == -1 ? 0 : 1;
                }
            }
        });
    }

    protected void copyAnnotations(Annotated source, Annotated target) {
        for(Annotation ann : source.getAnnotations()) {
            Annotation annCopy = new Annotation();
            annCopy.setParent(target);
            annCopy.setType(ann.getType());
            annCopy.setValues(ann.getValues());
            annCopy.setProperties(ann.getProperties());
            target.getAnnotations().add(annCopy);
        }
    }

}
