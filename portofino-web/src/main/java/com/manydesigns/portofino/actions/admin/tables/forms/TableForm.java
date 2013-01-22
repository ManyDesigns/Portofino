/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.admin.tables.forms;

import com.manydesigns.elements.annotations.*;
import com.manydesigns.portofino.model.database.Table;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class TableForm extends Table {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public TableForm(Table copyFrom) {
        try {
            BeanUtils.copyProperties(this, copyFrom);
            actualEntityName = copyFrom.getActualEntityName();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public Table copyTo(Table table) {
        try {
            BeanUtils.copyProperties(table, this);
        } catch (Exception e) {
            throw new Error(e);
        }
        return table;
    }

    @Override
    @FieldSize(50)
    @RegExp(
        value = "(_|$|[a-z]|[A-Z]|[\u0080-\ufffe])(_|$|[a-z]|[A-Z]|[\u0080-\ufffe]|[0-9])*",
        errorMessage = "layouts.admin.tables.invalidEntityName")
    @LabelI18N("layouts.admin.tables.entityName")
    public String getEntityName() {
        return super.getEntityName();
    }

    @Override
    @FieldSize(50)
    @LabelI18N("layouts.admin.tables.javaClass")
    public String getJavaClass() {
        return super.getJavaClass();
    }

    @Override
    @FieldSize(50)
    @LabelI18N("layouts.admin.tables.shortName")
    public String getShortName() {
        return super.getShortName();
    }

    @Insertable(false)
    @Updatable(false)
    public String getHqlQuery() {
        return "from " + StringUtils.defaultIfEmpty(entityName, actualEntityName);
    }
}
