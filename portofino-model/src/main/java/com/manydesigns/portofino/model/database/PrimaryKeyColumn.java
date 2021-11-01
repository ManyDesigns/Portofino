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

package com.manydesigns.portofino.model.database;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelObjectVisitor;
import com.manydesigns.portofino.model.Unmarshallable;
import org.apache.commons.configuration2.Configuration;
import org.eclipse.emf.ecore.EModelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;


/** A column in a {@link PrimaryKey primary key}.
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public class PrimaryKeyColumn implements ModelObject, Unmarshallable {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************
    protected PrimaryKey primaryKey;
    protected String columnName;
    protected Generator generator;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected Column actualColumn;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(PrimaryKeyColumn.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public PrimaryKeyColumn() {}

    public PrimaryKeyColumn(PrimaryKey primaryKey) {
        this();
        this.primaryKey = primaryKey;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        primaryKey = (PrimaryKey) parent;
    }

    public void setParent(Object parent) {
        primaryKey = (PrimaryKey) parent;
    }

    public void reset() {
        actualColumn = null;
    }

    public void link(Model model, Configuration configuration) {}

    public void visitChildren(ModelObjectVisitor visitor) {}

    public void init(Model model, Configuration configuration) {
        assert primaryKey != null;
        assert columnName != null;

        actualColumn = DatabaseLogic.findColumnByName(primaryKey.getTable(), columnName);
        if (actualColumn == null) {
            logger.warn("Cannor wire primary key column '{}' to primary key '{}'",
                    columnName, primaryKey);

        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    @XmlAttribute(required = true)
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Column getActualColumn() {
        return actualColumn;
    }

    public void setActualColumn(Column actualColumn) {
        Table table = primaryKey.getTable();
        if(actualColumn.getTable() != table) {
            throw new IllegalArgumentException("Column " + actualColumn.getQualifiedName() + " does not belong to the primary key table " + table.getQualifiedName());
        }
        this.actualColumn = actualColumn;
        this.columnName = actualColumn.getColumnName();
    }

    @XmlElements({
          @XmlElement(name="sequenceGenerator",type=SequenceGenerator.class),
          @XmlElement(name="incrementGenerator",type=IncrementGenerator.class),
          @XmlElement(name="tableGenerator",type=TableGenerator.class)
    })
    public Generator getGenerator() {
        return generator;
    }

    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    @Override
    public EModelElement getModelElement() {
        return null;
    }
}
