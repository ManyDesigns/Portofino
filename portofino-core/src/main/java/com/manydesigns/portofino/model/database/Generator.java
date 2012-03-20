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

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(value = XmlAccessType.NONE)
public abstract class Generator implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";
    //**************************************************************************
    // Fields
    //**************************************************************************
    protected PrimaryKeyColumn primaryKeyColumn;


    //**************************************************************************
    // Constructor
    //**************************************************************************

    public Generator() {}

    protected Generator(PrimaryKeyColumn primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    //**************************************************************************
    // Getter/setter
    //**************************************************************************

    public PrimaryKeyColumn getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public void setPrimaryKeyColumn(PrimaryKeyColumn primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    //**************************************************************************
    // DatamodelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        primaryKeyColumn = (PrimaryKeyColumn) parent;
    }

    public void reset() {}

    public void init(Model model) {
        assert primaryKeyColumn != null;
    }

    public void link(Model model) {}

    public void visitChildren(ModelObjectVisitor visitor) {}
}
