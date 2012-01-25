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
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(DatabaseLogic.class);
    
    public static final String USER_ENTITY_NAME = "users";
    public static final String GROUP_ENTITY_NAME = "groups";
    public static final String PASSWORD = "pwd";
    public static final String GROUPS = "groups";
    public static final int ACTIVE = 1;
    public static final int SUSPENDED = 2;
    public static final int BANNED = 3;
    public static final int SELFREGISTERED = 4;

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
            if (database.getDatabaseName().equalsIgnoreCase(databaseName)) {
                return database;
            }
        }
        logger.debug("Database not found: {}", databaseName);
        return null;
    }

    public static @Nullable Schema findSchemaByName(
            Database database, String schemaName) {
        for (Schema schema : database.getSchemas()) {
            if (schema.getSchemaName().equalsIgnoreCase(schemaName)) {
                return schema;
            }
        }
        logger.debug("Schema not found: {}", schemaName);
        return null;
    }

    public static @Nullable Schema findSchemaByQualifiedName(Model model,
                                            String qualifiedSchemaName) {
        int lastDot = qualifiedSchemaName.lastIndexOf(".");
        String databaseName = qualifiedSchemaName.substring(0, lastDot);
        Database database = findDatabaseByName(model,  databaseName);
        if (database != null) {
            return database.findSchemaByQualifiedName(qualifiedSchemaName);
        }
        logger.debug("Schema not found: {}", qualifiedSchemaName);
        return null;
    }

    public static @Nullable Table findTableByQualifiedName(
            Model model, String qualifiedTableName) {
        if (qualifiedTableName == null) {
            return null;
        }

        int lastDot = qualifiedTableName.lastIndexOf(".");
        if(lastDot==-1) {
            return null;
        }

        String qualifiedSchemaName = qualifiedTableName.substring(0, lastDot);
        String tableName = qualifiedTableName.substring(lastDot + 1);
        Schema schema = findSchemaByQualifiedName(model, qualifiedSchemaName);
        if (schema != null) {
            for (Table table : schema.getTables()) {
                if (table.getTableName().equalsIgnoreCase(tableName)) {
                    return table;
                }
            }
        }
        logger.debug("Table not found: {}", qualifiedTableName);
        return null;
    }

    public static @Nullable Table findTableByName(Schema schema, String tableName) {
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
            if (column.getColumnName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }
        logger.debug("Column {} not found in {}", columnName, table);
        return null;
    }

    public static @Nullable Column findColumnByQualifiedName(
            Model model, String qualifiedColumnName) {
        int lastDot = qualifiedColumnName.lastIndexOf(".");
        String qualifiedTableName = qualifiedColumnName.substring(0, lastDot);
        String columnName = qualifiedColumnName.substring(lastDot + 1);
        Table table = findTableByQualifiedName(model, qualifiedTableName);
        if (table != null) {
            for (Column column : table.getColumns()) {
                if (column.getColumnName().equalsIgnoreCase(columnName)) {
                    return column;
                }
            }
        }
        logger.debug("Column not found: {}", qualifiedColumnName);
        return null;
    }

    public static @Nullable ForeignKey findOneToManyRelationship(Model model,
                                                String qualifiedTableName,
                                                String relationshipName) {
        Table table = findTableByQualifiedName(model, qualifiedTableName);
        assert table != null;
        return table.findOneToManyRelationshipByName(relationshipName);
    }

    public static @Nullable ForeignKey findOneToManyRelationship(Model model,
                                                String databaseName, String entityName,
                                                String relationshipName) {
        Database database = findDatabaseByName(model, databaseName);
        Table table = findTableByEntityName(database, entityName);
        assert table != null;
        return table.findOneToManyRelationshipByName(relationshipName);
    }

    public static Table findTableByEntityName(Database database, String entityName) {
        for(Schema schema : database.getSchemas()) {
            for(Table table : schema.getTables()) {
                if(entityName.equalsIgnoreCase(table.getActualEntityName())) {
                    return table;
                }
            }
        }
        return null;
    }
}
