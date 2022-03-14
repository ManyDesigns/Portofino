/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.manydesigns.portofino.model.PortofinoPackage.ensureType;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"columnName", "columnType", "length", "scale", "jdbcType" ,"autoincrement","nullable","javaType","propertyName","annotations"})
public class Column implements ModelObject, Annotated, Named, Unmarshallable {
    public static final String copyright =
            "Copyright (C) 2005-2021 ManyDesigns srl";

    //**************************************************************************
    // Fields (physical JDBC)
    //**************************************************************************

    protected Table table;

    //**************************************************************************
    // Fields (logical)
    //**************************************************************************

    protected String javaType;
    protected EAttribute property;
    protected Annotation columnInfo;
    protected List<Annotation> annotations = new ArrayList<>();

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected String propertyName;

    public static final Logger logger = LoggerFactory.getLogger(Column.class);

    //**************************************************************************
    // Constructors and init
    //**************************************************************************
    public Column() {
        this(EcoreFactory.eINSTANCE.createEAttribute());
    }

    public Column(EAttribute property) {
        this.property = property;
        initAnnotations(property);
        columnInfo = ensureAnnotation(
                com.manydesigns.portofino.model.database.annotations.Column.class);
    }

    public Column(Table table) {
        this();
        setTable(table);
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    @Override
    public String getName() {
        return getColumnName();
    }

    public String getQualifiedName() {
        return MessageFormat.format("{0}.{1}",
                table.getQualifiedName(), getColumnName());
    }

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        setTable((Table) parent);
    }

    public void setParent(Object parent) {
        setTable((Table) parent);
    }

    public void reset() {}

    public void init(Model model, Configuration configuration) {
        assert table != null;
        if(getColumnName() == null) {
            throw new IllegalStateException("columnName must not be null");
        }

        if (StringUtils.isEmpty(propertyName)) {
            String initialName = DatabaseLogic.normalizeName(getColumnName());
            if(!initialName.equals(property.getName())) {
                property.setName(DatabaseLogic.getUniquePropertyName(table, initialName));
            }
        } else {
            property.setName(propertyName); //AS do not normalize (can be mixed-case Java properties)
        }
        // Re-set the column name so that if it's equal to the property name it's not saved in the annotation
        setColumnName(getColumnName());

        if(javaType != null) {
            property.setEType(ensureType(javaType));
        } else if(property.getEType() == null) {
            if(getColumnType() == null) {
                throw new IllegalStateException(getQualifiedName() + ": columnType must not be null when property type is null");
            }
            Class<?> actualJavaType = Type.getDefaultJavaType(getJdbcType(), getColumnType(), getLength(), getScale());
            if (actualJavaType != null) {
                property.setEType(ensureType(actualJavaType));
            } else {
                logger.error("Cannot determine default Java type for table: {}, column: {}, jdbc type: {}, type name: {}. Skipping column.",
                        table.getTableName(),
                        getColumnName(),
                        getJdbcType(),
                        javaType);
            }
        } else {
            if (getActualJavaType() == null) {
                logger.warn("Cannot load column {} of java type: {}", getQualifiedName(), property.getEType().getName());
            }
        }
    }

    public void link(Model model, Configuration configuration) {}

    public void visitChildren(ModelObjectVisitor visitor) {
        for (Annotation a : annotations) {
            visitor.visit(a);
        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
        if(this.table != null) {
            this.table.getModelElement().getEStructuralFeatures().add(property);
        }
    }

    public String getDatabaseName() {
        return table.getDatabaseName();
    }

    public String getSchemaName() {
        return table.getSchemaName();
    }

    public String getTableName() {
        return table.getTableName();
    }

    @Required
    @XmlAttribute(required = true)
    public String getColumnName() {
        String columnName = columnInfo.getPropertyValue("name");
        return StringUtils.defaultIfEmpty(columnName, getActualPropertyName());
    }

    public void setColumnName(String columnName) {
        if(columnName.equals(getActualPropertyName())) {
            columnInfo.removePropertyValue("name");
        } else {
            columnInfo.setPropertyValue("name", columnName);
        }
    }

    @XmlAttribute(required = true)
    public int getJdbcType() {
        return Integer.parseInt(columnInfo.getPropertyValue("jdbcType", String.valueOf(Integer.MIN_VALUE)));
    }

    public void setJdbcType(int jdbcType) {
        columnInfo.setPropertyValue("jdbcType", String.valueOf(jdbcType));
    }

    @Required
    @XmlAttribute(required = true)
    public String getColumnType() {
        return columnInfo.getPropertyValue("columnType");
    }

    public void setColumnType(String columnType) {
        columnInfo.setPropertyValue("columnType", columnType);
    }

    @XmlAttribute(required = true)
    public boolean isNullable() {
        return Boolean.parseBoolean(columnInfo.getPropertyValue("nullable"));
    }

    public void setNullable(boolean nullable) {
        columnInfo.setPropertyValue("nullable", String.valueOf(nullable));
    }

    @XmlAttribute(required = true)
    public Integer getLength() {
        String length = columnInfo.getPropertyValue("length");
        if(length != null) {
            return Integer.parseInt(length);
        } else {
            return null;
        }
    }

    public void setLength(Integer length) {
        columnInfo.setPropertyValue("length", length == null ? null : length.toString());
    }

    @XmlAttribute(required = true)
    public Integer getScale() {
        String scale = columnInfo.getPropertyValue("scale");
        if(scale != null) {
            return Integer.parseInt(scale);
        } else {
            return null;
        }
    }

    public void setScale(Integer scale) {
        columnInfo.setPropertyValue("scale", scale == null ? null : scale.toString());
    }

    @XmlAttribute(required = true)
    public boolean isAutoincrement() {
        return Boolean.parseBoolean(columnInfo.getPropertyValue("autoincrement"));
    }

    public void setAutoincrement(boolean autoincrement) {
        this.columnInfo.setPropertyValue("autoincrement", String.valueOf(autoincrement));
    }

    public boolean isSearchable() {
        // TODO: Blobs are not searchable but Liquibase does not return this information
        return true;
    }

    public Class<?> getActualJavaType() {
        return property.getEType() != null ? property.getEType().getInstanceClass() : null;
    }

    @XmlAttribute()
    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getActualPropertyName() {
        return property.getName();
    }

    @XmlAttribute()
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @XmlElementWrapper(name = "annotations")
    @XmlElement(name = "annotation", type = Annotation.class)
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return MessageFormat.format("column {0} {1}({2},{3}){4}",
                getQualifiedName(),
                getColumnType(),
                String.valueOf(getLength()),
                String.valueOf(getScale()),
                isNullable() ? "" : " NOT NULL");
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    @Deprecated
    public static String composeQualifiedName(String databaseName,
                                              String schemaName,
                                              String tableName,
                                              String columnName) {
        return MessageFormat.format("{0}.{1}.{2}.{3}",
                databaseName,
                schemaName,
                tableName,
                columnName);
    }

    @Deprecated
    public Annotation findModelAnnotationByType(String annotationType) {
        for (Annotation annotation : getAnnotations()) {
            if (annotation.getType().equals(annotationType)) {
                return annotation;
            }
        }
        return null;
    }

    public EAttribute getModelElement() {
        return property;
    }
}
