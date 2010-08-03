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
package com.manydesigns.portofino.model.io;

import com.manydesigns.portofino.model.io.ReferencePre;
import java.util.List;
import java.util.ArrayList;
/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
class RelationshipPre {
    public String fromDB;
    public String toDB;
    public String fromSchema;
    public String toSchema;
    public String fromTable;
    public String toTable;
    public String relationshipName;
    public String onUpdate;
    public String onDelete;
    List<ReferencePre> references = new ArrayList<ReferencePre>();

    public String getFromTable() {
        return fromTable;
    }

    public void setFromTable(String fromTable) {
        this.fromTable = fromTable;
    }

    public String getToTable() {
        return toTable;
    }

    public void setToTable(String toTable) {
        this.toTable = toTable;
    }

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

    public List<ReferencePre> getReferences() {
        return references;
    }

    public void setReferences(List<ReferencePre> references) {
        this.references = references;
    }

    public String getFromDB() {
        return fromDB;
    }

    public void setFromDB(String fromDB) {
        this.fromDB = fromDB;
    }

    public String getToDB() {
        return toDB;
    }

    public void setToDB(String toDB) {
        this.toDB = toDB;
    }

    public String getFromSchema() {
        return fromSchema;
    }

    public void setFromSchema(String fromSchema) {
        this.fromSchema = fromSchema;
    }

    public String getToSchema() {
        return toSchema;
    }

    public void setToSchema(String toSchema) {
        this.toSchema = toSchema;
    }
}
