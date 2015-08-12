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
import com.manydesigns.portofino.database.Type;
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
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"columnName", "columnType", "length", "scale", "jdbcType" ,"autoincrement","nullable","javaType","propertyName","annotations"})
public class Column implements ModelObject, Annotated {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields (physical JDBC)
    //**************************************************************************

    protected Table table;
    protected String columnName;
    protected int jdbcType;
    protected String  columnType;
    protected boolean nullable;
    protected boolean autoincrement;
    protected Integer length;
    protected Integer scale;

    //**************************************************************************
    // Fields (logical)
    //**************************************************************************

    protected String javaType;
    protected String propertyName;
    protected final List<Annotation> annotations;


    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected String actualPropertyName;
    protected Class actualJavaType;

    public static final Logger logger = LoggerFactory.getLogger(Column.class);

    //**************************************************************************
    // Constructors and init
    //**************************************************************************
    public Column() {
        annotations = new ArrayList<Annotation>();
    }

    public Column(Table table) {
        this();
        this.table = table;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public String getQualifiedName() {
        return MessageFormat.format("{0}.{1}",
                table.getQualifiedName(), columnName);
    }

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        table = (Table) parent;
    }

    public void reset() {
        actualPropertyName = null;
        actualJavaType = null;
    }

    public void init(Model model) {
        assert table != null;
        //TODO questi assert dovrebbero essere test + throw exception
        assert columnName != null;
        assert columnType != null;

        if (propertyName == null) {
            actualPropertyName = DatabaseLogic.getUniquePropertyName(table, DatabaseLogic.normalizeName(columnName));
        } else {
            actualPropertyName = propertyName; //AS do not normalize (can be mixed-case Java properties)
        }

        if(javaType != null) {
            actualJavaType = ReflectionUtil.loadClass(javaType);
            if (actualJavaType == null) {
                logger.warn("Cannot load column {} of java type: {}", getQualifiedName(), javaType);
            }
        } else {
            actualJavaType = Type.getDefaultJavaType(jdbcType, length, scale);
            if (actualJavaType == null) {
                logger.error("Cannot determine default Java type for table: {}, column: {}, jdbc type: {}, type name: {}. Skipping column.",
                        new Object[]{table.getTableName(),
                                getColumnName(),
                                jdbcType,
                                javaType
                        });
            }
        }
    }

    public void link(Model model) {}

    public void visitChildren(ModelObjectVisitor visitor) {
        for (Annotation annotation : annotations) {
            visitor.visit(annotation);
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
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @XmlAttribute(required = true)
    public int getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(int jdbcType) {
        this.jdbcType = jdbcType;
    }

    @Required
    @XmlAttribute(required = true)
    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    @XmlAttribute(required = true)
    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    @XmlAttribute(required = true)
    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    @XmlAttribute(required = true)
    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    @XmlAttribute(required = true)
    public boolean isAutoincrement() {
        return autoincrement;
    }

    public void setAutoincrement(boolean autoincrement) {
        this.autoincrement = autoincrement;
    }

    public boolean isSearchable() {
        // TODO: Blobs are not searchable but Liquibase does not return this information
        return true;
    }

    public Class getActualJavaType() {
        return actualJavaType;
    }

    @XmlAttribute(required = false)
    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getActualPropertyName() {
        return actualPropertyName;
    }

    @XmlAttribute(required = false)
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @XmlElementWrapper(name="annotations")
    @XmlElement(name = "annotation",
            type = Annotation.class)
    public List<Annotation> getAnnotations() {
        return annotations;
    }

        
    @Override
    public String toString() {
        return MessageFormat.format("column {0} {1}({2},{3}){4}",
                getQualifiedName(),
                columnType,
                Integer.toString(length),
                Integer.toString(scale),
                nullable ? "" : " NOT NULL");
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

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

    public Annotation findModelAnnotationByType(String annotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.getType().equals(annotationType)) {
                return annotation;
            }
        }
        return null;
    }
}
