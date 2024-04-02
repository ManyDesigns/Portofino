/*
 * Copyright (C) 2005-2019 ManyDesigns srl.  All rights reserved.
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
import org.apache.commons.configuration.Configuration;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(value = XmlAccessType.NONE)
@XmlType(propOrder = {"fromColumn","toColumn"})
public class Reference implements ModelObject {
    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected HasReferences owner;
    protected String fromColumn;
    protected String toColumn;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected Column actualFromColumn;
    protected Column actualToColumn;


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Reference() {}

    public Reference(HasReferences owner) {
        this.owner = owner;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        this.owner = (HasReferences) parent;
    }

    public void reset() {
        actualFromColumn = null;
        actualToColumn = null;
    }

    public void init(Model model, Configuration configuration) {
        assert owner != null;
        assert fromColumn != null;
    }

    public void link(Model model, Configuration configuration) {
        actualFromColumn =
                DatabaseLogic.findColumnByName(owner.getFromTable(), fromColumn);
        if (actualFromColumn == null) {
            throw new RuntimeException("Cannot resolve column: " + owner.getFromTable().getQualifiedName() + "." + fromColumn);
        }

        Table toTable = owner.getToTable();
        if (toTable != null) {
            actualToColumn = DatabaseLogic.findColumnByName(toTable, toColumn);
        }
    }

    public void visitChildren(ModelObjectVisitor visitor) {}

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public HasReferences getOwner() {
        return owner;
    }

    public void setOwner(HasReferences owner) {
        this.owner = owner;
    }

    @XmlAttribute(required = true)
    public String getFromColumn() {
        return fromColumn;
    }

    public void setFromColumn(String fromColumn) {
        this.fromColumn = fromColumn;
    }

    @XmlAttribute(required = true)
    public String getToColumn() {
        return toColumn;
    }

    public void setToColumn(String toColumn) {
        this.toColumn = toColumn;
    }

    public Column getActualFromColumn() {
        return actualFromColumn;
    }

    public Column getActualToColumn() {
        return actualToColumn;
    }
}
