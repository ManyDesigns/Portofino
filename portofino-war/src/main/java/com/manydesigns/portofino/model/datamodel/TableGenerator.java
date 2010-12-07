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

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.xml.XmlAttribute;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableGenerator extends Generator{
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************
    protected String table;
    protected String keyColumn;
    protected String keyValue;
    protected String valueColumn;

    public TableGenerator(PrimaryKeyColumn primaryKeyColumn) {
        super(primaryKeyColumn);
    }

    public void reset() {

    }

    public void init(Model model) {

    }

    public String getQualifiedName() {
        return table;
    }

    @XmlAttribute(required = true, order = 1)
    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @XmlAttribute(required = true, order = 2)
    public String getKeyColumn() {
        return keyColumn;
    }

    public void setKeyColumn(String keyColumn) {
        this.keyColumn = keyColumn;
    }

    @XmlAttribute(required = true, order = 3)
    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    @XmlAttribute(required = true, order = 4)
    public String getValueColumn() {
        return valueColumn;
    }

    public void setValueColumn(String valueColumn) {
        this.valueColumn = valueColumn;
    }
}
