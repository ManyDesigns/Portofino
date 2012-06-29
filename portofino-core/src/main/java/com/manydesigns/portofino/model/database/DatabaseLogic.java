/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.model.database;

import com.manydesigns.portofino.model.Model;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DatabaseLogic {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(DatabaseLogic.class);

    //**************************************************************************
    // Get all objects of a certain kind
    //**************************************************************************

    public static List<Schema> getAllSchemas(Model model) {
        List<Schema> result = new ArrayList<Schema>();
        for (Database database : model.getDatabases()) {
            for (Schema schema : database.getSchemas()) {
                result.add(schema);
            }
        }
        return result;
    }

    public static List<Table> getAllTables(Model model) {
        List<Table> result = new ArrayList<Table>();
        for (Database database : model.getDatabases()) {
            for (Schema schema : database.getSchemas()) {
                for (Table table : schema.getTables()) {
                    result.add(table);
                }
            }
        }
        return result;
    }

    public static List<Column> getAllColumns(Model model) {
        List<Column> result = new ArrayList<Column>();
        for (Database database : model.getDatabases()) {
            for (Schema schema : database.getSchemas()) {
                for (Table table : schema.getTables()) {
                    for (Column column : table.getColumns()) {
                        result.add(column);
                    }
                }
            }
        }
        return result;
    }

    public static List<ForeignKey> getAllForeignKeys(Model model) {
        List<ForeignKey> result = new ArrayList<ForeignKey>();
        for (Database database : model.getDatabases()) {
            for (Schema schema : database.getSchemas()) {
                for (Table table : schema.getTables()) {
                    for (ForeignKey foreignKey : table.getForeignKeys()) {
                        result.add(foreignKey);
                    }
                }
            }
        }
        return result;
    }

    //**************************************************************************
    // Search objects of a certain kind
    //**************************************************************************

    public static @Nullable Database findDatabaseByName(
            Model model, String databaseName) {
        for (Database database : model.getDatabases()) {
            if (database.getDatabaseName().equals(databaseName)) {
                return database;
            }
        }
        logger.debug("Database not found: {}", databaseName);
        return null;
    }

    public static @Nullable Schema findSchemaByName(
            Database database, String schemaName) {
        for (Schema schema : database.getSchemas()) {
            if (schema.getSchemaName().equals(schemaName)) {
                return schema;
            }
        }
        logger.debug("Schema not found: {}", schemaName);
        return null;
    }

    public static @Nullable Schema findSchemaByNameIgnoreCase(
            Database database, String schemaName) {
        for (Schema schema : database.getSchemas()) {
            if (schema.getSchemaName().equalsIgnoreCase(schemaName)) {
                return schema;
            }
        }
        logger.debug("Schema not found: {}", schemaName);
        return null;
    }

    public static @Nullable Schema findSchemaByName(Model model, String databaseName, String schemaName) {
        Database database = findDatabaseByName(model, databaseName);
        if (database != null) {
            return findSchemaByName(database, schemaName);
        }
        logger.debug("Schema not found: {}", schemaName);
        return null;
    }

    public static @Nullable Table findTableByName(
            Model model, String databaseName, String schemaName, String tableName) {
        Schema schema = findSchemaByName(model, databaseName, schemaName);
        if (schema != null) {
            return findTableByName(schema, tableName);
        }
        logger.debug("Table not found: {}", tableName);
        return null;
    }

    public static @Nullable Table findTableByName(Schema schema, String tableName) {
        for (Table table : schema.getTables()) {
            if (table.getTableName().equals(tableName)) {
                return table;
            }
        }
        logger.debug("Table {} not found in {}", tableName, schema);
        return null;
    }

    public static @Nullable Table findTableByNameIgnoreCase(Schema schema, String tableName) {
        for (Table table : schema.getTables()) {
            if (table.getTableName().equalsIgnoreCase(tableName)) {
                return table;
            }
        }
        logger.debug("Table {} not found in {}", tableName, schema);
        return null;
    }

    public static @Nullable Column findColumnByName(
            Table table, String columnName) {
        for (Column column : table.getColumns()) {
            if (column.getColumnName().equals(columnName)) {
                return column;
            }
        }
        logger.debug("Column {} not found in {}", columnName, table);
        return null;
    }

    public static @Nullable Column findColumnByNameIgnoreCase(
            Table table, String columnName) {
        for (Column column : table.getColumns()) {
            if (column.getColumnName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }
        logger.debug("Column {} not found in {}", columnName, table);
        return null;
    }

    public static Column findColumnByPropertyName(Table table, String propertyName) {
        for (Column column : table.getColumns()) {
            if (column.getActualPropertyName().equals(propertyName)) {
                return column;
            }
        }
        logger.debug("Property {} not found in {}", propertyName, table);
        return null;
    }

    public static @Nullable Column findColumnByName(
            Model model, String databaseName, String schemaName, String tableName, String columnName) {
        Table table = findTableByName(model, databaseName, schemaName, tableName);
        if (table != null) {
            for (Column column : table.getColumns()) {
                if (column.getColumnName().equals(columnName)) {
                    return column;
                }
            }
        }
        logger.debug("Column not found: {}", columnName);
        return null;
    }

    public static @Nullable ForeignKey findOneToManyRelationship(Model model,
                                                String databaseName, String entityName,
                                                String relationshipName) {
        Database database = findDatabaseByName(model, databaseName);
        Table table = findTableByEntityName(database, entityName);
        assert table != null;
        return findOneToManyRelationshipByName(table, relationshipName);
    }

    public static Table findTableByEntityName(Database database, String entityName) {
        for(Schema schema : database.getSchemas()) {
            for(Table table : schema.getTables()) {
                if(entityName.equals(table.getActualEntityName())) {
                    return table;
                }
            }
        }
        return null;
    }

    public static ForeignKey findForeignKeyByName(Table table, String fkName) {
        for (ForeignKey current : table.foreignKeys) {
            if (current.getName().equals(fkName)) {
                return current;
            }
        }
        logger.debug("Foreign key not found: {}", fkName);
        return null;
    }

    public static ForeignKey findForeignKeyByNameIgnoreCase(Table table, String fkName) {
        for (ForeignKey current : table.foreignKeys) {
            if (current.getName().equalsIgnoreCase(fkName)) {
                return current;
            }
        }
        logger.debug("Foreign key not found: {}", fkName);
        return null;
    }

    public static ModelSelectionProvider findSelectionProviderByName(Table table, String selectionProviderName) {
        for (ModelSelectionProvider current : table.selectionProviders) {
            if (current.getName().equalsIgnoreCase(selectionProviderName)) {
                return current;
            }
        }
        logger.debug("Selection provider not found: {}", selectionProviderName);
        return null;
    }

    public static ForeignKey findOneToManyRelationshipByName(Table table, String relationshipName) {
        for (ForeignKey current : table.getOneToManyRelationships()) {
            if (current.getName().equalsIgnoreCase(relationshipName)) {
                return current;
            }
        }
        Table.logger.debug("One to many relationship not found: {}", relationshipName);
        return null;
    }

    public static String[] splitQualifiedTableName(String qualifiedName) {
        String[] name = qualifiedName.split("\\.");
        if(name.length == 3) {
            return name;
        } else {
            throw new IllegalArgumentException("Not a qualified table name: " + qualifiedName);
        }
    }
}
