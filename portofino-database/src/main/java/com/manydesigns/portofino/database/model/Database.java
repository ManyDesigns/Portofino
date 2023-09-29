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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"databaseName","trueString","falseString","connectionProvider","schemas","entityMode","annotations"})
@XmlRootElement
public class Database implements ModelObject, Named, Unmarshallable, Annotated {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<Schema> schemas;
    protected final List<Annotation> annotations;

    protected Domain domain;

    protected String trueString = null;
    protected String falseString = null;
    protected String entityMode = null;

    protected ConnectionProvider connectionProvider;
    protected Properties settings;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(Database.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************
    public Database(Domain domain) {
        this.domain = domain;
        this.schemas = new ArrayList<>();
        this.annotations = new ArrayList<>();
        initAnnotations(domain);
    }

    public Database() {
        this(new Domain());
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {}

    public void setParent(Object parent) {}

    @Override
    public String getName() {
        return getDatabaseName();
    }

    public String getQualifiedName() {
        return domain.getName();
    }

    public void reset() {}

    public void init(Object context, Configuration configuration) {
        if(domain.getName() == null) {
            throw new IllegalStateException("Database name is null");
        }
        if(domain.getName().contains("/") || domain.getName().contains("\\")) {
            throw new IllegalStateException("Database name contains slashes or backslashes: " + domain.getName());
        }
    }

    public void link(Object context, Configuration configuration) {}

    public void visitChildren(ModelObjectVisitor visitor) {
        for (Annotation annotation : annotations) {
            visitor.visit(annotation);
        }
        for (Schema schema : schemas) {
            visitor.visit(schema);
        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    @XmlAttribute(required = true)
    public String getDatabaseName() {
        return domain.getName();
    }

    public void setDatabaseName(String databaseName) {
        domain.setName(databaseName);
    }

    @XmlElementWrapper(name="schemas")
    @XmlElement(name = "schema", type = Schema.class)
    public List<Schema> getSchemas() {
        return schemas;
    }

    public void addSchema(Schema schema) {
        if (schemas.contains(schema)) {
            return;
        }
        schema.setParent(this);
        getModelElement().getSubdomains().add(schema.getModelElement());
        schemas.add(schema);
    }

    public void removeSchema(Schema schema) {
        getModelElement().getSubdomains().remove(schema.getModelElement());
        schemas.remove(schema);
    }

    //**************************************************************************
    // Get all objects of a certain kind
    //**************************************************************************

    public List<Table> getAllTables() {
        List<Table> result = new ArrayList<>();
        for (Schema schema : schemas) {
            result.addAll(schema.getTables());
        }
        return result;
    }

    public List<Column> getAllColumns() {
        List<Column> result = new ArrayList<>();
        for (Schema schema : schemas) {
            for (Table table : schema.getTables()) {
                result.addAll(table.getColumns());
            }
        }
        return result;
    }

    public List<ForeignKey> getAllForeignKeys() {
        List<ForeignKey> result = new ArrayList<>();
        for (Schema schema : schemas) {
            for (Table table : schema.getTables()) {
                result.addAll(table.getForeignKeys());
            }
        }
        return result;
    }

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

    @XmlAttribute(required = false)
    public String getEntityMode() {
        return entityMode;
    }

    public void setEntityMode(String entityMode) {
        this.entityMode = entityMode;
    }

    public Domain getModelElement() {
        return domain;
    }

    @Override
    @XmlElementWrapper(name = "annotations")
    @XmlElement(name = "annotation", type = Annotation.class)
    @NotNull
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public Properties getSettings() {
        return settings;
    }

    public void setSettings(Properties settings) {
        this.settings = settings;
    }
}
