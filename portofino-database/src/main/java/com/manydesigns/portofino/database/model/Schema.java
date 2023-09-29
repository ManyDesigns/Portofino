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

package com.manydesigns.portofino.database.model;

import com.manydesigns.portofino.model.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;
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
@XmlType(propOrder = {"catalog", "schemaName", "immediateTables", "annotations"})
public class Schema implements ModelObject, Annotated, Named, Unmarshallable {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Database database;
    protected final List<Table> tables = new ArrayList<>();
    @Deprecated
    protected final List<Table> immediateTables;

    protected Domain domain;
    protected String actualSchemaName;
    protected String catalog;

    protected Configuration configuration;
    protected String key;
    protected final List<Annotation> annotations = new ArrayList<>();

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(Schema.class);


    //**************************************************************************
    // Constructors and init
    //**************************************************************************
    public Schema() {
        this(new Domain());
    }

    public Schema(Domain domain) {
        this.domain = domain;
        initAnnotations(domain);
        immediateTables = new ArrayList<>();
    }

    public Schema(Database database) {
        this();
        setDatabase(database);
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************
    @Override
    public String getName() {
        return getSchemaName();
    }

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        setParent(parent);
    }

    public void setParent(Object parent) {
        setDatabase((Database) parent);
        tables.addAll(immediateTables);
        immediateTables.clear();
    }

    public String getQualifiedName() {
        String name = actualSchemaName != null ? actualSchemaName : getSchemaName();
        if(getDatabaseName() == null) {
            return name;
        }
        return MessageFormat.format("{0}.{1}", getDatabaseName(), name);
    }

    public void reset() {}

    public void init(Object context, Configuration configuration) {
        assert database != null;
        this.configuration = configuration;
        key = "portofino.database." + getDatabase().getDatabaseName() + ".schemas." + getSchemaName();
    }

    public void link(Object context, Configuration configuration) {
        // We need annotations to be initialized
        if(actualSchemaName == null) {
            actualSchemaName = configuration.getString(key);
            domain.getEAnnotations().removeIf(
                    a -> a.getSource().equals(com.manydesigns.portofino.database.model.annotations.Schema.class.getName()));
        } else {
            Annotation annotation = ensureAnnotation(com.manydesigns.portofino.database.model.annotations.Schema.class);
            annotation.setPropertyValue("name", actualSchemaName);
        }
        if(actualSchemaName == null) {
            actualSchemaName = getSchemaName();
        }
    }

    public void visitChildren(ModelObjectVisitor visitor) {
        for (Table table : tables) {
            visitor.visit(table);
        }
        for (Annotation a : annotations) {
            visitor.visit(a);
        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        if (this.database == database) {
            return;
        } else if (this.database != null) {
            this.database.removeSchema(this);
        }
        this.database = database;
        if(this.database != null) {
            database.addSchema(this);
        }
    }

    public String getDatabaseName() {
        return database != null ? database.getDatabaseName() : null;
    }

    @XmlAttribute(required = true)
    public String getSchemaName() {
        return domain.getName();
    }

    public void setSchemaName(String schemaName) {
        domain.setName(schemaName);
    }

    public String getActualSchemaName() {
        return actualSchemaName;
    }

    public void setActualSchemaName(String actualSchemaName) {
        if(key != null) { //key is null when creating/synchronizing a new schema
            if (StringUtils.isEmpty(actualSchemaName)) {
                configuration.clearProperty(key);
            } else if (configuration.containsKey(key) || !getSchemaName().equals(actualSchemaName)) {
                configuration.setProperty(key, actualSchemaName);
            }
        }
        this.actualSchemaName = actualSchemaName;
    }

    @XmlAttribute
    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    @Deprecated
    @XmlElementWrapper(name="tables")
    @XmlElement(name = "table", type = Table.class)
    public List<Table> getImmediateTables() {
        return immediateTables;
    }

    public List<Table> getTables() {
        return tables;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    //**************************************************************************
    // Get all objects of a certain kind
    //**************************************************************************

    public List<Column> getAllColumns() {
        List<Column> result = new ArrayList<>();
        for (Table table : tables) {
            result.addAll(table.getColumns());
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

    @Override
    @XmlElementWrapper(name = "annotations")
    @XmlElement(name = "annotation", type = Annotation.class)
    @NotNull
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public Domain getModelElement() {
        return domain;
    }

}
