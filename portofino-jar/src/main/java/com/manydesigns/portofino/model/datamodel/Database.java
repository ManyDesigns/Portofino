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

package com.manydesigns.portofino.model.datamodel;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelVisitor;
import com.manydesigns.portofino.xml.Identifier;
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
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<Schema> schemas;

    protected String databaseName;

    protected String trueString = null;
    protected String falseString = null;

    
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

    public void visitChildren(ModelVisitor visitor) {
        for (Schema schema : schemas) {
            visitor.visit(schema);
        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    @Identifier
    @XmlAttribute(required = true)
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @XmlElementWrapper(name="schemas")
    @XmlElement(name = "schema",
            type = com.manydesigns.portofino.model.datamodel.Schema.class)
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


    public Table findTableByEntityName(String entityName) {
        for(Schema schema : getSchemas()) {
            for(Table table : schema.getTables()) {
                if(table.getActualEntityName().equalsIgnoreCase(entityName)) {
                    return table;
                }
            }
        }
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
}
