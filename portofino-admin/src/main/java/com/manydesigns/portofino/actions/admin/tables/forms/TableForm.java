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
        errorMessage = "invalid.entity.name")
    @LabelI18N("entity.name")
    public String getEntityName() {
        return super.getEntityName();
    }

    @Override
    @FieldSize(50)
    @LabelI18N("java.class")
    public String getJavaClass() {
        return super.getJavaClass();
    }

    @Override
    @FieldSize(50)
    @LabelI18N("short.name")
    public String getShortName() {
        return super.getShortName();
    }

    @Insertable(false)
    @Updatable(false)
    public String getHqlQuery() {
        return "from " + StringUtils.defaultIfEmpty(entityName, actualEntityName);
    }
}
