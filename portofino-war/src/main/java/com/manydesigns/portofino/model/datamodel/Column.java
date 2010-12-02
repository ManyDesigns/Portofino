/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.annotations.Annotation;
import com.manydesigns.portofino.xml.XmlAttribute;
import com.manydesigns.portofino.xml.XmlCollection;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Column implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields (physical JDBC)
    //**************************************************************************

    protected final Table table;
    protected String columnName;
    protected String columnType;
    protected boolean nullable;
    protected boolean autoincrement;
    protected int length;
    protected int scale;
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

    public static final Logger logger = LogUtil.getLogger(Column.class);

    //**************************************************************************
    // Constructors and init
    //**************************************************************************
    public Column(Table table) {
        this.table = table;
        annotations = new ArrayList<Annotation>();
    }

    public Column(Table table, String columnName, String columnType,
                  boolean nullable, boolean autoincrement,
                  int length, int scale, boolean searchable) {
        this(table);
        this.columnName = columnName;
        this.columnType = columnType;
        this.nullable = nullable;
        this.autoincrement = autoincrement;
        this.length = length;
        this.scale = scale;
        this.searchable = searchable;
    }

    public Column(Table table,
                  String columnName,
                  String columnType,
                  boolean nullable,
                  boolean autoincrement,
                  int length,
                  int scale,
                  boolean searchable,
                  String javaType,
                  String propertyName) {
        this(table, columnName, columnType, nullable,
                autoincrement, length, scale, searchable);
        this.javaType = javaType;
        this.propertyName = propertyName;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public String getQualifiedName() {
        return MessageFormat.format("{0}.{1}",
                table.getQualifiedName(), columnName);
    }

    public void reset() {
        actualPropertyName = null;
        actualJavaType = null;

        for (Annotation annotation : annotations) {
            annotation.reset();
        }
    }

    public void init(Model model) {
        if (propertyName == null) {
            actualPropertyName = columnName;
        } else {
            actualPropertyName = propertyName;
        }

        actualJavaType = ReflectionUtil.loadClass(javaType);
        if (actualJavaType == null) {
            LogUtil.warningMF(logger,
                    "Cannot load column java type: {0}", javaType);
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

    public String getDatabaseName() {
        return table.getDatabaseName();
    }

    public String getSchemaName() {
        return table.getSchemaName();
    }

    public String getTableName() {
        return table.getTableName();
    }

    @XmlAttribute(required = true, order = 1)
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @XmlAttribute(required = true, order = 2)
    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    @XmlAttribute(required = true, order = 3)
    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    @XmlAttribute(required = true, order = 4)
    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @XmlAttribute(required = true, order = 5)
    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    @XmlAttribute(required = true, order = 6)
    public boolean isAutoincrement() {
        return autoincrement;
    }

    public void setAutoincrement(boolean autoincrement) {
        this.autoincrement = autoincrement;
    }

    @XmlAttribute(required = true, order = 7)
    public boolean isSearchable() {
        return searchable;
    }

    public Class getActualJavaType() {
        return actualJavaType;
    }

    @XmlAttribute(required = false, order = 8)
    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getActualPropertyName() {
        return actualPropertyName;
    }

    @XmlAttribute(required = false, order = 9)
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    @XmlCollection(itemClasses = Annotation.class, itemNames = "annotation", order = 1)
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
