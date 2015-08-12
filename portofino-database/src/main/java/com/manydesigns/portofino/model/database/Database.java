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
@XmlType(propOrder = {"databaseName","trueString","falseString","connectionProvider","schemas"})
public class Database implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

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
