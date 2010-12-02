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

package com.manydesigns.portofino.model;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.model.site.RootNode;
import com.manydesigns.portofino.xml.XmlCollection;
import com.manydesigns.portofino.xml.XmlElement;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Model {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final ArrayList<Database> databases;
    protected RootNode rootNode;


    public static final Logger logger = LogUtil.getLogger(Model.class);

    //**************************************************************************
    // Constructors and init
    //**************************************************************************

    public Model() {
        this.databases = new ArrayList<Database>();
    }

    //**************************************************************************
    // Reset / init
    //**************************************************************************

    protected void reset() {
        // databases
        for (Database database : databases) {
            database.reset();
        }

        // site nodes
        if (rootNode != null) {
            rootNode.reset();
        }
    }

    public void init() {
        reset();
        
        // databases
        for (Database database : databases) {
            database.init(this);
        }

        // site nodes
        if (rootNode != null) {
            rootNode.init(this);
        }
    }

    //**************************************************************************
    // Get all objects of a certain kind
    //**************************************************************************

    public List<Schema> getAllSchemas() {
        List<Schema> result = new ArrayList<Schema>();
        for (Database database : getDatabases()) {
            for (Schema schema : database.getSchemas()) {
                result.add(schema);
            }
        }
        return result;
    }

    public List<Table> getAllTables() {
        List<Table> result = new ArrayList<Table>();
        for (Database database : getDatabases()) {
            for (Schema schema : database.getSchemas()) {
                for (Table table : schema.getTables()) {
                    result.add(table);
                }
            }
        }
        return result;
    }

    public List<Column> getAllColumns() {
        List<Column> result = new ArrayList<Column>();
        for (Database database : getDatabases()) {
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

    public List<ForeignKey> getAllForeignKeys() {
        List<ForeignKey> result = new ArrayList<ForeignKey>();
        for (Database database : getDatabases()) {
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

    public Database findDatabaseByName(String databaseName) {
        for (Database database : getDatabases()) {
            if (database.getDatabaseName().equals(databaseName)) {
                return database;
            }
        }
        LogUtil.fineMF(logger, "Database not found: {0}", databaseName);
        return null;
    }

    public Schema findSchemaByQualifiedName(String qualifiedSchemaName) {
        int lastDot = qualifiedSchemaName.lastIndexOf(".");
        String databaseName = qualifiedSchemaName.substring(0, lastDot);
        Database database = findDatabaseByName(databaseName);
        if (database != null) {
            return database.findSchemaByQualifiedName(qualifiedSchemaName);
        }
        LogUtil.fineMF(logger, "Schema not found: {0}", qualifiedSchemaName);
        return null;
    }

    public Table findTableByQualifiedName(String qualifiedTableName) {
        int lastDot = qualifiedTableName.lastIndexOf(".");
        if(lastDot==-1) {
            return null;
        }

        String qualifiedSchemaName = qualifiedTableName.substring(0, lastDot);
        String tableName = qualifiedTableName.substring(lastDot + 1);
        Schema schema = findSchemaByQualifiedName(qualifiedSchemaName);
        if (schema != null) {
            for (Table table : schema.getTables()) {
                if (table.getTableName().equals(tableName)) {
                    return table;
                }
            }
        }
        LogUtil.fineMF(logger, "Table not found: {0}", qualifiedTableName);
        return null;
    }

    public Column findColumnByQualifiedName(String qualifiedColumnName) {
        int lastDot = qualifiedColumnName.lastIndexOf(".");
        String qualifiedTableName = qualifiedColumnName.substring(0, lastDot);
        String columnName = qualifiedColumnName.substring(lastDot + 1);
        Table table = findTableByQualifiedName(qualifiedTableName);
        if (table != null) {
            for (Column column : table.getColumns()) {
                if (column.getColumnName().equals(columnName)) {
                    return column;
                }
            }
        }
        LogUtil.fineMF(logger, "Column not found: {0}", qualifiedColumnName);
        return null;
    }

    public ForeignKey findOneToManyRelationship(String qualifiedTableName,
                                                String relationshipName) {
        Table table = findTableByQualifiedName(qualifiedTableName);
        return table.findOneToManyRelationshipByName(relationshipName);
    }


    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    @XmlCollection(itemClasses = Database.class, itemNames = "database", order = 1)
    public List<Database> getDatabases() {
        return databases;
    }

    @XmlElement(required = false, order = 2)
    public RootNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    public Table findTableByEntityName(String entityName) {
        for(Database db : this.getDatabases()) {
            for(Schema schema : db.getSchemas()){
                for (Table tb : schema.getTables()){
                    if(entityName.equals(tb.getActualEntityName())){
                        return tb;
                    }
                }
            }

        }
        return null;
    }
}