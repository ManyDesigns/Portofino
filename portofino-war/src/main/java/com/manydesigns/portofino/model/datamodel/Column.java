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

import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.annotations.Annotation;
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
public class Column implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields (physical JDBC)
    //**************************************************************************

    protected Table table;
    protected String columnName;
    protected String columnType;
    protected boolean nullable;
    protected boolean autoincrement;
    protected Integer length;
    protected Integer scale;
    protected boolean searchable;
    
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

        for (Annotation annotation : annotations) {
            annotation.reset();
        }
    }

    public void init(Model model) {
        assert table != null;
        assert columnName != null;
        assert columnType != null;
        assert length != null;
        assert scale != null;

        if (propertyName == null) {
            actualPropertyName = columnName;
        } else {
            actualPropertyName = propertyName;
        }

        actualJavaType = ReflectionUtil.loadClass(javaType);
        if (actualJavaType == null) {
            logger.warn("Cannot load column {} of java type: {}", getQualifiedName(), javaType);
        }

        for (Annotation annotation : annotations) {
            annotation.init(model);
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
    @Identifier
    @XmlAttribute(required = true)
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
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

    @XmlAttribute(required = true)
    public boolean isSearchable() {
        return searchable;
    }

    public Class getActualJavaType() {
        return actualJavaType;
    }

    @Required
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

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    @XmlElementWrapper(name="annotations")
    @XmlElement(name = "annotation",
            type = com.manydesigns.portofino.model.annotations.Annotation.class)
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
