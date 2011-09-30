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

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class Generator implements ModelObject{
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";
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

    public void init() {
        assert primaryKeyColumn != null;
    }

    public void link(Model model) {}

    public void visitChildren(ModelVisitor visitor) {}
}
