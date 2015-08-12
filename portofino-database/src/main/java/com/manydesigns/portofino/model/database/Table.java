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

import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.*;
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
@XmlRootElement(name = "table")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"tableName", "entityName", "shortName", "javaClass","annotations","columns","foreignKeys","primaryKey","selectionProviders"})
public class Table implements ModelObject, Annotated {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<Column> columns;
    protected final List<ForeignKey> foreignKeys;
    protected final List<Annotation> annotations;
    protected final List<ModelSelectionProvider> selectionProviders;

    protected Schema schema;
    protected String tableName;
    protected String entityName;

    protected String javaClass;

    protected String shortName;

    protected PrimaryKey primaryKey;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected final List<ForeignKey> oneToManyRelationships;
    protected Class actualJavaClass;
    protected String actualEntityName;
    protected final List<String> syntheticPropertyNames = new ArrayList<String>();

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(Table.class);

    //**************************************************************************
    // Constructors and init
    //**************************************************************************
    public Table() {
        columns = new ArrayList<Column>();
        foreignKeys = new ArrayList<ForeignKey>();
        oneToManyRelationships = new ArrayList<ForeignKey>();
        annotations = new ArrayList<Annotation>();
        selectionProviders = new ArrayList<ModelSelectionProvider>();
    }

    public Table(Schema schema) {
        this();
        this.schema = schema;
    }

    //**************************************************************************
    // DatamodelObject implementation
    //**************************************************************************

    public String getQualifiedName() {
        if(schema.getQualifiedName() == null) {
            return tableName;
        }
        return MessageFormat.format("{0}.{1}",
                schema.getQualifiedName(), tableName);
    }

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        schema = (Schema) parent;
    }

    public void reset() {
        actualEntityName = null;
        actualJavaClass = null;
        oneToManyRelationships.clear();
    }

    public void init(Model model) {
        assert schema != null;
        assert tableName != null;
        
        // wire up javaClass
        actualJavaClass = ReflectionUtil.loadClass(javaClass);

        String baseEntityName;
        if (entityName == null) {
            baseEntityName = DatabaseLogic.normalizeName(getTableName());
        } else {
            baseEntityName = DatabaseLogic.normalizeName(entityName);
        }

        String calculatedEntityName = baseEntityName;

        int i = 2;
        Database database = schema.getDatabase();
        while(DatabaseLogic.findTableByEntityName(database, calculatedEntityName) != null) {
            logger.warn("Entity name {} already taken, generating a new one", calculatedEntityName);
            calculatedEntityName = baseEntityName + "_" + (i++);
        }

        actualEntityName = calculatedEntityName;
    }

    public void link(Model model) {}

    public void visitChildren(ModelObjectVisitor visitor) {
        for (Column column : columns) {
            visitor.visit(column);
        }

        if (primaryKey == null) {
            logger.warn("Table {} has no primary key", toString());
        } else {
            visitor.visit(primaryKey);
        }

        for (ForeignKey foreignKey : foreignKeys) {
            visitor.visit(foreignKey);
        }

        for (Annotation annotation : annotations) {
            visitor.visit(annotation);
        }

        for(ModelSelectionProvider selectionProvider : selectionProviders) {
            visitor.visit(selectionProvider);
        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @Required
    public String getDatabaseName() {
        return schema.getDatabaseName();
    }

    @Required
    public String getSchemaName() {
        return schema.getSchemaName();
    }

    @Required
    @XmlAttribute(required = true)
    public String getTableName() {
        return tableName;
    }


    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @XmlAttribute(required = false)
    public String getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }

    @XmlElementWrapper(name="columns")
    @XmlElement(name = "column",
            type = com.manydesigns.portofino.model.database.Column.class)
    public List<Column> getColumns() {
        return columns;
    }

    @XmlElement()
    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Class getActualJavaClass() {
        return actualJavaClass;
    }

    @XmlElementWrapper(name="foreignKeys")
    @XmlElement(name = "foreignKey",
            type = com.manydesigns.portofino.model.database.ForeignKey.class)
    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    @XmlAttribute(required = false)
    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getActualEntityName() {
        return actualEntityName;
    }

    public List<ForeignKey> getOneToManyRelationships() {
        return oneToManyRelationships;
    }

    @XmlElementWrapper(name="annotations")
    @XmlElement(name = "annotation",
            type = Annotation.class)
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    @XmlElementWrapper(name="selectionProviders")
    @XmlElement(name="query",type=DatabaseSelectionProvider.class)
    public List<ModelSelectionProvider> getSelectionProviders() {
        return selectionProviders;
    }

    @XmlAttribute(required = false)
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public List<String> getSyntheticPropertyNames() {
        return syntheticPropertyNames;
    }

    //**************************************************************************
    // toString() override
    //**************************************************************************

    @Override
    public String toString() {
        return MessageFormat.format("table {0}", getQualifiedName());
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    public static String composeQualifiedName(String databaseName,
                                              String schemaName,
                                              String tableName) {
        return MessageFormat.format(
                "{0}.{1}.{2}", databaseName, schemaName, tableName);
    }

}
