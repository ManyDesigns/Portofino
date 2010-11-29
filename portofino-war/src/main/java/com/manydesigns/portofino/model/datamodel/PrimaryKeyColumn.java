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

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.xml.XmlAttribute;

import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class PrimaryKeyColumn implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************
    protected final PrimaryKey primaryKey;

    protected String columnName;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected Column actualColumn;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LogUtil.getLogger(PrimaryKeyColumn.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public PrimaryKeyColumn(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public PrimaryKeyColumn(PrimaryKey primaryKey, String columnName) {
        this(primaryKey);
        this.columnName = columnName;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void reset() {
        actualColumn = null;
    }

    public String getQualifiedName() {
        return null;
    }

    public void init(Model model) {
        actualColumn = primaryKey.getTable().findColumnByName(columnName);
        if (actualColumn == null) {
            LogUtil.warningMF(logger,
                    "Cannor wire primary key column ''{0}'' to primary key ''{1}''",
                    columnName, primaryKey);

        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    @XmlAttribute(required = true, order = 1)
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Column getActualColumn() {
        return actualColumn;
    }
}
