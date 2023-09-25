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

import com.manydesigns.elements.annotations.Required;
import com.manydesigns.portofino.model.*;
import com.manydesigns.portofino.model.annotations.Id;
import org.apache.commons.configuration2.Configuration;
import org.eclipse.emf.ecore.EModelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The primary key identifying records in the owning table.
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@XmlAccessorType(value = XmlAccessType.NONE)
public class PrimaryKey implements ModelObject, Named, Unmarshallable {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<PrimaryKeyColumn> primaryKeyColumns;

    protected Table table;
    protected String primaryKeyName;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected final List<Column> columns;
    protected boolean valid;

    public static final Logger logger = LoggerFactory.getLogger(Table.class);

    //**************************************************************************
    // Constructors and wire up
    //**************************************************************************

    public PrimaryKey() {
        columns = new ArrayList<>();
        primaryKeyColumns = new ArrayList<>();
    }

    public PrimaryKey(Table table) {
        this();
        setTable(table);
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    @Override
    public String getName() {
        return primaryKeyName;
    }

    public String getQualifiedName() {
        return MessageFormat.format("{0}#{1}",
                table.getQualifiedName(), primaryKeyName);
    }

    public void setParent(Object parent) {
        setTable((Table) parent);
    }

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        setParent(parent);
    }

    public void reset() {
        columns.clear();
        valid = true;
    }

    public void init(Object context, Configuration configuration) {
        assert table != null;

        // Liquibase on MySQL returns null primaryKey name if the name is "PRIMARY"
        // assert primaryKeyName != null;

        if (primaryKeyColumns.isEmpty()) {
            throw new Error(MessageFormat.format("Primary key {0} has no columns", getQualifiedName()));
        }

    }

    public void link(Object context, Configuration configuration) {
        for (PrimaryKeyColumn pkc : primaryKeyColumns) {
            Column column = pkc.getActualColumn();
            if (column == null) {
                valid = false;
                logger.error("Invalid primary key column: {}-{}",
                        getQualifiedName(), pkc.getColumnName());
            } else {
                columns.add(column);
            }
        }

        if (columns.isEmpty()) {
            logger.warn("Primary key '{}' has no columns", this);
        }

        table.getColumns().forEach(c -> c.removeAnnotation(Id.class));
        for(int i = 0; i < columns.size(); i++) {
            columns.get(i).ensureAnnotation(Id.class).setPropertyValue("order", i + "");
        }
    }

    public void visitChildren(ModelObjectVisitor visitor) {
        for (PrimaryKeyColumn pkc : primaryKeyColumns) {
            visitor.visit(pkc);
        }
    }

    public void add(Column column) {
        PrimaryKeyColumn pkColumn = new PrimaryKeyColumn(this);
        pkColumn.setActualColumn(column);
        primaryKeyColumns.add(pkColumn); //This also checks that the column belongs to the right table
        columns.add(column);
    }

    //**************************************************************************
    // Find methods
    //**************************************************************************

    public PrimaryKeyColumn findPrimaryKeyColumnByNameIgnoreCase(String columnName) {
        for (PrimaryKeyColumn primaryKeyColumn : primaryKeyColumns) {
            if (primaryKeyColumn.getColumnName().equalsIgnoreCase(columnName)) {
                return primaryKeyColumn;
            }
        }
        return null;
    }

    public PrimaryKeyColumn findPrimaryKeyColumnByName(String columnName) {
        for (PrimaryKeyColumn primaryKeyColumn : primaryKeyColumns) {
            if (primaryKeyColumn.getColumnName().equals(columnName)) {
                return primaryKeyColumn;
            }
        }
        return null;
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

    public List<Column> getColumns() {
        return columns;
    }

    public String getDatabaseName() {
        return table.getTableName();
    }

    public String getSchemaName() {
        return table.getSchemaName();
    }

    public String getTableName() {
        return table.getTableName();
    }

    @Required
    @XmlAttribute(required = true)
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    @XmlElement(name="column",type=PrimaryKeyColumn.class)
    public List<PrimaryKeyColumn> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public EModelElement getModelElement() {
        return null;
    }

    @Override
    public String toString() {
        return MessageFormat.format("primary key {0}", getQualifiedName());
    }
}
