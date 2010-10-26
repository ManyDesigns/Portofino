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
import com.manydesigns.portofino.model.annotations.ModelAnnotation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Table {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Schema schema;
    protected String tableName;
    protected boolean m2m;
    protected String javaClassName;
    protected List<Column> columns;
    protected PrimaryKey primaryKey;
    protected final List<ForeignKey> foreignKeys;
    protected final List<ModelAnnotation> modelAnnotations;

    public static final Logger logger = LogUtil.getLogger(Table.class);

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected final List<ForeignKey> oneToManyRelationships;
    protected Class javaClass;

    //**************************************************************************
    // Constructors and init
    //**************************************************************************
    public Table(Schema schema, String tableName) {
        this.schema = schema;
        this.tableName = tableName;
        this.m2m = false;
        columns = new ArrayList<Column>();
        foreignKeys = new ArrayList<ForeignKey>();
        oneToManyRelationships = new ArrayList<ForeignKey>();
        modelAnnotations = new ArrayList<ModelAnnotation>();
    }

    public void init() {
        // wire up javaClass
        javaClass = ReflectionUtil.loadClass(javaClassName);

        for (Column column : columns) {
            column.init();
        }

        if (primaryKey == null) {
            LogUtil.warningMF(logger,
                    "Table {0} has no primary key", toString());
        } else {
            primaryKey.init();
        }

        for (ForeignKey foreignKey : foreignKeys) {
            foreignKey.init();
        }


        for (ModelAnnotation modelAnnotation : modelAnnotations) {
            modelAnnotation.init();
        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public Schema getSchema() {
        return schema;
    }

    public String getDatabaseName() {
        return schema.getDatabaseName();
    }

    public String getSchemaName() {
        return schema.getSchemaName();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isM2m() {
        return m2m;
    }

    public void setM2m(boolean m2m) {
        this.m2m = m2m;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public void setJavaClassName(String javaClassName) {
        this.javaClassName = javaClassName;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Class getJavaClass() {
        return javaClass;
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public List<ForeignKey> getOneToManyRelationships() {
        return oneToManyRelationships;
    }

    public Collection<ModelAnnotation> getModelAnnotations() {
        return modelAnnotations;
    }

    //**************************************************************************
    // Search objects of a certain kind
    //**************************************************************************

    public Column findColumnByName(String columnName) {
        for (Column column : columns) {
            if (column.getColumnName().equals(columnName)) {
                return column;
            }
        }
        LogUtil.fineMF(logger, "Column not found: {0}", columnName);
        return null;
    }

    public ForeignKey findForeignKeyByName(String fkName) {
        for (ForeignKey current : foreignKeys) {
            if (current.getForeignKeyName().equals(fkName)) {
                return current;
            }
        }
        LogUtil.fineMF(logger,
                "Foreign key not found: {0}", fkName);
        return null;
    }

    public ForeignKey findOneToManyRelationshipByName(String relationshipName) {
        for (ForeignKey current : getOneToManyRelationships()) {
            if (current.getForeignKeyName().equals(relationshipName)) {
                return current;
            }
        }
        LogUtil.fineMF(logger,
                "One to many relationship not found: {0}", relationshipName);
        return null;
    }

    public ModelAnnotation findModelAnnotationByType(String annotationType) {
        for (ModelAnnotation modelAnnotation : modelAnnotations) {
            if (modelAnnotation.getType().equals(annotationType)) {
                return modelAnnotation;
            }
        }
        return null;
    }


    //**************************************************************************
    // Overrides
    //**************************************************************************

    @Override
    public String toString() {
        return getQualifiedName();
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

    public String getQualifiedName() {
        return MessageFormat.format("{0}.{1}",
                schema.getQualifiedName(), tableName);
    }

    
}
