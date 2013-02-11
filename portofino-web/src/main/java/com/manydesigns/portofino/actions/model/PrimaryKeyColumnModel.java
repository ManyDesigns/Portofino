/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
