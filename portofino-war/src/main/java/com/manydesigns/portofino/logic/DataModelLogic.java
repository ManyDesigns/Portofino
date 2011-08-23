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

package com.manydesigns.portofino.logic;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.*;
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
public class DataModelLogic {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(DataModelLogic.class);

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

    public static Database findDatabaseByName(Model model, String databaseName) {
        for (Database database : model.getDatabases()) {
            if (database.getDatabaseName().equals(databaseName)) {
                return database;
            }
        }
        logger.debug("Database not found: {}", databaseName);
        return null;
    }

    public static Schema findSchemaByQualifiedName(Model model,
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

    public static Table findTableByQualifiedName(Model model, String qualifiedTableName) {
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
                if (table.getTableName().equals(tableName)) {
                    return table;
                }
            }
        }
        logger.debug("Table not found: {}", qualifiedTableName);
        return null;
    }

    public static Column findColumnByQualifiedName(Model model,
                                            String qualifiedColumnName) {
        int lastDot = qualifiedColumnName.lastIndexOf(".");
        String qualifiedTableName = qualifiedColumnName.substring(0, lastDot);
        String columnName = qualifiedColumnName.substring(lastDot + 1);
        Table table = findTableByQualifiedName(model, qualifiedTableName);
        if (table != null) {
            for (Column column : table.getColumns()) {
                if (column.getColumnName().equals(columnName)) {
                    return column;
                }
            }
        }
        logger.debug("Column not found: {}", qualifiedColumnName);
        return null;
    }

    public static ForeignKey findOneToManyRelationship(Model model,
                                                String qualifiedTableName,
                                                String relationshipName) {
        Table table = findTableByQualifiedName(model, qualifiedTableName);
        return table.findOneToManyRelationshipByName(relationshipName);
    }



}
