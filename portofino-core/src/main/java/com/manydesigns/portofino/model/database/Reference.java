/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.model.database;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelObjectVisitor;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(value = XmlAccessType.NONE)
public class Reference implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

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

    public void init(Model model) {
        assert owner != null;
        assert fromColumn != null;
    }

    public void link(Model model) {
        actualFromColumn =
                owner.getFromTable().findColumnByName(fromColumn);
        if (actualFromColumn == null) {
            throw new InternalError("Cannot resolve column: " + fromColumn);
        }

        Table toTable = owner.getToTable();
        if (toTable != null) {
            actualToColumn = toTable.findColumnByName(toColumn);
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
