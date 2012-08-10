/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.portofino.actions.model;

import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.annotations.Label;

import java.io.Serializable;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PrimaryKeyColumnModel implements Serializable {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public PrimaryKeyColumnModel(String column, String genType, String seqName, String tabName, String colName, String colValue) {
        this.column = column;
        this.genType = genType;
        this.seqName = seqName;
        this.tabName = tabName;
        this.colName = colName;
        this.colValue = colValue;
    }

    public PrimaryKeyColumnModel(String column, String genType) {
        this.column = column;
        this.genType = genType;
    }

    public PrimaryKeyColumnModel() {
    }

    @Required
    public String column;
    @Required
    @Label("Generator")
    public String genType;
    @Label("Sequence")
    public String seqName;
    @Label("Table name")
    public String tabName;
    @Label("Column name")
    public String colName;
    @Label("Column value")
    public String colValue;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimaryKeyColumnModel that = (PrimaryKeyColumnModel) o;

        if (column != null ? !column.equals(that.column) : that.column != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return column != null ? column.hashCode() : 0;
    }
}
