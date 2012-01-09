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

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelVisitor;

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
            "Copyright (c) 2005-2011, ManyDesigns srl";

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

    public void visitChildren(ModelVisitor visitor) {}

    public String getQualifiedName() {
        return null;
    }

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
