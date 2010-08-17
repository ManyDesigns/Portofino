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

import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Relationship {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String RULE_NO_ACTION = "NO ACTION";
    public static final String RULE_CASCADE = "CASCADE";
    public static final String RULE_SET_NULL = "SET NULL";
    public static final String RULE_SET_DEFAULT = "SET DEFAULT";
    

    //**************************************************************************
    // Fields
    //**************************************************************************
    protected Table fromTable;
    protected Table toTable;
    protected String relationshipName;
    protected String onUpdate;
    protected String onDelete;

    protected final List<Reference>references;


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Relationship(String relationshipName, String onUpdate, String onDelete) {
        this.relationshipName = relationshipName;
        this.onUpdate = onUpdate;
        this.onDelete = onDelete;
        references = new ArrayList<Reference>();
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public String getRelationshipName() {
        return relationshipName;
    }

    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }

    public String getOnUpdate() {
        return onUpdate;
    }

    public void setOnUpdate(String onUpdate) {
        this.onUpdate = onUpdate;
    }

    public String getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(String onDelete) {
        this.onDelete = onDelete;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public Table getToTable() {
        return toTable;
    }

    public void setToTable(Table toTable) {
        this.toTable = toTable;
    }

    public Table getFromTable() {
        return fromTable;
    }

    public void setFromTable(Table fromTable) {
        this.fromTable = fromTable;
    }
}
