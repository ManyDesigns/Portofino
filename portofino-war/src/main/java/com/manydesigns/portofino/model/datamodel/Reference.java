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

import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.xml.XmlAttribute;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Reference implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final ForeignKey foreignKey;
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

    public Reference(ForeignKey foreignKey) {
        this.foreignKey = foreignKey;
    }

    public Reference(ForeignKey foreignKey,
                     String fromColumn, String toColumn) {
        this(foreignKey);
        this.fromColumn = fromColumn;
        this.toColumn = toColumn;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void reset() {
        actualFromColumn = null;
        actualToColumn = null;
    }

    public void init(Model model) {
        actualFromColumn =
                foreignKey.getFromTable().findColumnByName(fromColumn);

        Table actualToTable = foreignKey.getActualToTable();
        if (actualToTable != null) {
            actualToColumn = actualToTable.findColumnByName(toColumn);
        }
    }

    public String getQualifiedName() {
        return null;
    }

//**************************************************************************
    // Getters/setter
    //**************************************************************************

    public String getFromDatabaseName() {
        return foreignKey.getFromDatabaseName();
    }

    public String getFromSchemaName() {
        return foreignKey.getFromSchemaName();
    }

    public String getFromTableName() {
        return foreignKey.getFromTableName();
    }

    @XmlAttribute(required = true)
    public String getFromColumn() {
        return fromColumn;
    }

    public void setFromColumn(String fromColumn) {
        this.fromColumn = fromColumn;
    }

    public String getToDatabaseName() {
        return foreignKey.getToDatabase();
    }

    public String getToSchemaName() {
        return foreignKey.getToSchema();
    }

    public String getToTableName() {
        return foreignKey.getToTable();
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
