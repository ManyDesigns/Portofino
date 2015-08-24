/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelObjectVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"catalog", "schemaName","immediateTables"})
public class Schema implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Database database;
    protected final List<Table> immediateTables;
    protected final List<Table> tables = new ArrayList<Table>();

    protected String schemaName;
    protected String catalog;
    
    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(Schema.class);


    //**************************************************************************
    // Constructors and init
    //**************************************************************************
    public Schema() {
        immediateTables = new ArrayList<Table>();
    }

    public Schema(Database database) {
        this();
        this.database = database;
    }

    //**************************************************************************
    // DatamodelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        database = (Database) parent;
        tables.addAll(immediateTables);
        immediateTables.clear();
    }

    public String getQualifiedName() {
        if(getDatabaseName() == null) {
            return schemaName;
        }
        return MessageFormat.format("{0}.{1}", getDatabaseName(), schemaName);
    }

    public void reset() {}

    public void init(Model model) {
        assert database != null;
        assert schemaName != null;
    }

    public void link(Model model) {}

    public void visitChildren(ModelObjectVisitor visitor) {
        for (Table table : tables) {
            visitor.visit(table);
        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public String getDatabaseName() {
        return database != null ? database.getDatabaseName() : null;
    }

    @XmlAttribute(required = true)
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @XmlAttribute(required = false)
    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    @XmlElementWrapper(name="tables")
    @XmlElement(name = "table",
            type = com.manydesigns.portofino.model.database.Table.class)
    public List<Table> getImmediateTables() {
        return immediateTables;
    }

    public List<Table> getTables() {
        return tables;
    }


    //**************************************************************************
    // Get all objects of a certain kind
    //**************************************************************************

    public List<Column> getAllColumns() {
        List<Column> result = new ArrayList<Column>();
        for (Table table : tables) {
            for (Column column : table.getColumns()) {
                result.add(column);
            }
        }
        return result;
    }

    //**************************************************************************
    // Search objects of a certain kind
    //**************************************************************************

    public Table findTableByQualifiedName(String qualifiedTableName) {
        int lastDot = qualifiedTableName.lastIndexOf(".");
        String tableName = qualifiedTableName.substring(lastDot + 1);
        for (Table table : tables) {
            if (table.getTableName().equalsIgnoreCase(tableName)) {
                return table;
            }
        }
        logger.debug("Table not found: {}", qualifiedTableName);
        return null;
    }

    public Column findColumnByQualifiedName(String qualifiedColumnName) {
        int lastDot = qualifiedColumnName.lastIndexOf(".");
        String qualifiedTableName = qualifiedColumnName.substring(0, lastDot);
        String columnName = qualifiedColumnName.substring(lastDot + 1);
        Table table = findTableByQualifiedName(qualifiedTableName);
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

    //**************************************************************************
    // toString() override
    //**************************************************************************

    @Override
    public String toString() {
        return MessageFormat.format("schema {0}", getQualifiedName());
    }

}
