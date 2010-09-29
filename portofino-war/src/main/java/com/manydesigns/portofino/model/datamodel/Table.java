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
import com.manydesigns.portofino.model.annotations.Annotation;
import org.apache.commons.lang.builder.ToStringBuilder;

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

    protected String databaseName;
    protected String schemaName;
    protected String tableName;
    protected List<Column> columns;
    protected final List<Relationship> manyToOneRelationships;
    protected final List<Relationship> oneToManyRelationships;
    protected PrimaryKey primaryKey;
    protected boolean m2m;
    protected String className;
    protected final List<Annotation> annotations;

    public static final Logger logger = LogUtil.getLogger(Table.class);


    //**************************************************************************
    // Constructors
    //**************************************************************************
    public Table(String databaseName, String schemaName, String tableName) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columns = new ArrayList<Column>();
        this.manyToOneRelationships = new ArrayList<Relationship>();
        this.oneToManyRelationships = new ArrayList<Relationship>();
        this.m2m = false;
        annotations = new ArrayList<Annotation>();
    }

    
    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Relationship> getManyToOneRelationships() {
        return manyToOneRelationships;
    }

    public List<Relationship> getOneToManyRelationships() {
        return oneToManyRelationships;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isM2m() {
        return m2m;
    }

    public void setM2m(boolean m2m) {
        this.m2m = m2m;
    }

    public String getQualifiedName() {
        return MessageFormat.format("{0}.{1}.{2}",
                databaseName, schemaName, tableName);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
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

    public Relationship findManyToOneByName(String relationshipName) {
        for (Relationship relationship : manyToOneRelationships) {
            if (relationship.getRelationshipName().equals(relationshipName)) {
                return relationship;
            }
        }
        LogUtil.fineMF(logger,
                "Many-to-one relationship not found: {0}", relationshipName);
        return null;
    }

    //**************************************************************************
    // toString()
    //**************************************************************************

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("databaseName", databaseName)
                .append("schemaName", schemaName)
                .append("tableName", tableName)
                .toString();
    }
}
