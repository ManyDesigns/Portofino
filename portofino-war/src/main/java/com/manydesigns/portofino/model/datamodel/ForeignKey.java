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

import com.manydesigns.portofino.model.annotations.ModelAnnotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ForeignKey {
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
    protected final Table fromTable;
    protected String foreignKeyName;

    protected Table toTable;
    protected String toDatabaseName;
    protected String toSchemaName;
    protected String toTableName;

    protected String onUpdate;
    protected String onDelete;
    
    protected String manyPropertyName;
    protected String onePropertyName;

    protected final List<Reference> references;
    protected final List<ModelAnnotation> modelAnnotations;


    //**************************************************************************
    // Constructors and init
    //**************************************************************************

    public ForeignKey(Table fromTable,
                      String foreignKeyName,
                      String toDatabaseName,
                      String toSchemaName,
                      String toTableName,
                      String onUpdate, String onDelete) {
        this.fromTable = fromTable;
        this.foreignKeyName = foreignKeyName;

        this.toDatabaseName = toDatabaseName;
        this.toSchemaName = toSchemaName;
        this.toTableName = toTableName;

        this.onUpdate = onUpdate;
        this.onDelete = onDelete;

        references = new ArrayList<Reference>();
        modelAnnotations = new ArrayList<ModelAnnotation>();
    }

    public void init() {
        for (Reference reference : references) {
            // wire up Referenece.fromColumn
            reference.fromColumn =
                    fromTable.findColumnByName(reference.getFromColumnName());

            // wire up Referenece.toColumn
            if (toTable != null) {
                reference.toColumn =
                        toTable.findColumnByName(reference.getToColumnName());
            }
        }
    }


    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public Table getFromTable() {
        return fromTable;
    }

    public String getFromDatabaseName() {
        return fromTable.getDatabaseName();
    }

    public String getFromSchemaName() {
        return fromTable.getSchemaName();
    }

    public String getFromTableName() {
        return fromTable.getTableName();
    }

    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public void setForeignKeyName(String foreignKeyName) {
        this.foreignKeyName = foreignKeyName;
    }

    public String getToDatabaseName() {
        return toDatabaseName;
    }

    public void setToDatabaseName(String toDatabaseName) {
        this.toDatabaseName = toDatabaseName;
    }

    public String getToSchemaName() {
        return toSchemaName;
    }

    public void setToSchemaName(String toSchemaName) {
        this.toSchemaName = toSchemaName;
    }

    public String getToTableName() {
        return toTableName;
    }

    public void setToTableName(String toTableName) {
        this.toTableName = toTableName;
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

    public String getManyPropertyName() {
        return manyPropertyName;
    }

    public void setManyPropertyName(String manyPropertyName) {
        this.manyPropertyName = manyPropertyName;
    }

    public String getOnePropertyName() {
        return onePropertyName;
    }

    public void setOnePropertyName(String onePropertyName) {
        this.onePropertyName = onePropertyName;
    }

    public Collection<ModelAnnotation> getAnnotations() {
        return modelAnnotations;
    }

    public Table getToTable() {
        return toTable;
    }

    public void setToTable(Table toTable) {
        this.toTable = toTable;
    }
}
