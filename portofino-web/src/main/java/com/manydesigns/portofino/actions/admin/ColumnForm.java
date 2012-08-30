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

package com.manydesigns.portofino.actions.admin;

import com.manydesigns.elements.annotations.*;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.model.database.Column;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ColumnForm extends Column {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected Type type;

    public ColumnForm(Column copyFrom, Type type) {
        try {
            BeanUtils.copyProperties(this, copyFrom);
        } catch (Exception e) {
            throw new Error(e);
        }
        this.type = type;
    }

    @Override
    @Updatable(false)
    @Insertable(false)
    @Label("Name")
    public String getColumnName() {
        return super.getColumnName();
    }

    @Override
    @FieldSize(4)
    @Updatable(false)
    @Insertable(false)
    public Integer getLength() {
        return super.getLength();
    }

    @Override
    @FieldSize(4)
    @Updatable(false)
    @Insertable(false)
    public Integer getScale() {
        return super.getScale();
    }

    @Label("Type")
    @Updatable(false)
    @Insertable(false)
    public Type getType() {
        return type;
    }

    @Override
    @Label("Class")
    @Select(nullOption = false)
    public String getJavaType() {
        return super.getJavaType();
    }

    @Override
    public boolean isNullable() {
        return super.isNullable();
    }

    public void copyTo(Column column) {
        column.setJavaType(getJavaType());
        column.setPropertyName(StringUtils.defaultIfEmpty(getPropertyName(), null));
    }
}
