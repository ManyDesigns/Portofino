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
public class Database implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<Schema> schemas;

    protected String databaseName;

    protected String trueString = null;
    protected String falseString = null;

    protected ConnectionProvider connectionProvider;

    
    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(Database.class);


    //**************************************************************************
    // Constructors
    //**************************************************************************
    public Database() {
        this.schemas = new ArrayList<Schema>();
    }

    //**************************************************************************
    // DatamodelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {}

    public String getQualifiedName() {
        return databaseName;
    }

    public void reset() {}

    public void init(Model model) {
        assert databaseName != null;
    }

    public void link(Model model) {}

    public void visitChildren(ModelObjectVisitor visitor) {
        visitor.visit(connectionProvider);
        for (Schema schema : schemas) {
            visitor.visit(schema);
        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    @XmlAttribute(required = true)
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @XmlElementWrapper(name="schemas")
    @XmlElement(name = "schema",
            type = com.manydesigns.portofino.model.database.Schema.class)
    public List<Schema> getSchemas() {
        return schemas;
    }

    //**************************************************************************
    // Get all objects of a certain kind
    //**************************************************************************

    public List<Table> getAllTables() {
        List<Table> result = new ArrayList<Table>();
        for (Schema schema : schemas) {
            for (Table table : schema.getTables()) {
                result.add(table);
            }
        }
        return result;
    }

    public List<Column> getAllColumns() {
        List<Column> result = new ArrayList<Column>();
        for (Schema schema : schemas) {
            for (Table table : schema.getTables()) {
                for (Column column : table.getColumns()) {
                    result.add(column);
                }
            }
        }
        return result;
    }

    public List<ForeignKey> getAllForeignKeys() {
        List<ForeignKey> result = new ArrayList<ForeignKey>();
        for (Schema schema : schemas) {
            for (Table table : schema.getTables()) {
                for (ForeignKey foreignKey : table.getForeignKeys()) {
                    result.add(foreignKey);
                }
            }
        }
        return result;
    }


    //**************************************************************************
    // Search objects of a certain kind
    //**************************************************************************

    public Schema findSchemaByQualifiedName(String qualifiedSchemaName) {
        int lastDot = qualifiedSchemaName.lastIndexOf(".");
        String schemaName = qualifiedSchemaName.substring(lastDot + 1);
        for (Schema schema : schemas) {
            if (schema.getSchemaName().equalsIgnoreCase(schemaName)) {
                return schema;
            }
        }
        logger.debug("Schema not found: {}", qualifiedSchemaName);
        return null;
    }

    public Table findTableByQualifiedName(String qualifiedTableName) {
        int lastDot = qualifiedTableName.lastIndexOf(".");
        String qualifiedSchemaName = qualifiedTableName.substring(0, lastDot);
        String tableName = qualifiedTableName.substring(lastDot + 1);
        Schema schema = findSchemaByQualifiedName(qualifiedSchemaName);
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
        return MessageFormat.format("database {0}", getQualifiedName());
    }

    @XmlAttribute(required = false)
    public String getTrueString() {
        return trueString;
    }

    public void setTrueString(String trueString) {
        this.trueString = trueString;
    }

    @XmlAttribute(required = false)
    public String getFalseString() {
        return falseString;
    }

    public void setFalseString(String falseString) {
        this.falseString = falseString;
    }

    @XmlElements({
        @XmlElement(name="jdbcConnection", type=JdbcConnectionProvider.class),
        @XmlElement(name="jndiConnection", type=JndiConnectionProvider.class)
    })
    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    public void setConnectionProvider(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }
}
