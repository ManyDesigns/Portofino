/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.model.database;

import com.manydesigns.portofino.model.Annotated;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.Model;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(DatabaseLogic.class);
    public static final String[] HQL_KEYWORDS = { "member", "order", "group", "select", "update", "from" };

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

    public static Annotation findAnnotation(
            Annotated object, Class<? extends java.lang.annotation.Annotation> annotationClass) {
        for(Annotation candidate : object.getAnnotations()) {
            if(candidate.getType().equals(annotationClass.getName())) {
                return candidate;
            }
        }
        return null;
    }

    //**************************************************************************
    // Disambiguate names
    //**************************************************************************

    public static String getUniquePropertyName(Table table, String initialName) {
        String prefix = initialName;
        int prog = 2;
        boolean changed = true;
        while(changed) {
            changed = false;
            for(Column column : table.getColumns()) {
                if(StringUtils.equals(initialName, column.getActualPropertyName())) {
                    initialName = prefix + "_" + prog;
                    logger.warn("Duplicate property found, renaming to {}", initialName);
                    prog++;
                    changed = true;
                    break;
                }
            }
            if(!changed) {
                for(ForeignKey fk : table.getForeignKeys()) {
                    if(StringUtils.equals(initialName, fk.getActualOnePropertyName())) {
                        initialName = prefix + "_" + prog;
                        logger.warn("Duplicate property found, renaming to {}", initialName);
                        prog++;
                        changed = true;
                        break;
                    }
                }
            }
            if(!changed) {
                for(ForeignKey fk : table.getOneToManyRelationships()) {
                    if(StringUtils.equals(initialName, fk.getActualManyPropertyName())) {
                        initialName = prefix + "_" + prog;
                        logger.warn("Duplicate property found, renaming to {}", initialName);
                        prog++;
                        changed = true;
                        break;
                    }
                }
            }
        }
        return initialName;
    }

    public static boolean isInPk(Column column) {
        return column.getTable().getPrimaryKey().getColumns().contains(column);
    }

    public static boolean isInFk(Column column) {
        Table table = column.getTable();
        for(ForeignKey fk : table.getForeignKeys()) {
            for(Reference ref : fk.getReferences()) {
                if(ref.getActualFromColumn().equals(column)) {
                    return true;
                }
            }
        }
        return false;
    }

    //**************************************************************************
    // Rende un nome secondo le regole che specificano gli identificatori in HQL
    // protected
    //ID_START_LETTER
    //    :    '_'
    //    |    '$'
    //|    'a'..'z'
    //|    '\u0080'..'\ufffe'       // HHH-558 : Allow unicode chars in identifiers
    //;
    //
    //protected
    //ID_LETTER
    //:    ID_START_LETTER
    //|    '0'..'9'
    //;
    //**************************************************************************
    public static String normalizeName(String name) {
        name = StringUtils.replaceChars(name, ".", "_");
        String firstLetter = name.substring(0,1);
        String others = name.substring(1);

        StringBuilder result = new StringBuilder();
        result.append(checkFirstLetter(firstLetter));

        for(int i=0; i< others.length();i++){
            String letter = String.valueOf(others.charAt(i));
            result.append(checkOtherLetters(letter));
        }
        String normalizedName = result.toString();
        if(ArrayUtils.contains(HQL_KEYWORDS, normalizedName.toLowerCase())) {
            normalizedName = "_" + normalizedName;
        }
        if(!name.equals(normalizedName)) {
            logger.info("Name " + name + " normalized to " + normalizedName);
        }
        return normalizedName;
    }

    static String checkFirstLetter(String letter) {
        letter = StringUtils.replaceChars(letter, "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                                                  "abcdefghijklmnopqrstuvwxyz");

        if (letter.equals("_") || letter.equals("$")
                || StringUtils.isAlpha(letter)){
            return letter;
        } else if (StringUtils.isNumeric(letter)) {
            return "_"+letter;
        } else {
            return "_";
        }
    }

    static String checkOtherLetters(String letter) {
        letter = StringUtils.replaceChars(letter, "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                                                  "abcdefghijklmnopqrstuvwxyz");
        if (letter.equals("_") || letter.equals("$")
                || StringUtils.isAlphanumeric(letter)){
            return letter;
        } else {
            return "_";
        }
    }
}
