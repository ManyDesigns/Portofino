/*
 * Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.annotations.FieldSize;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.portofino.model.database.DatabaseSelectionProvider;
import org.apache.commons.beanutils.BeanUtils;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DatabaseSelectionProviderForm extends DatabaseSelectionProvider {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    protected String columns;

    public DatabaseSelectionProviderForm(DatabaseSelectionProvider copyFrom) {
        try {
            BeanUtils.copyProperties(this, copyFrom);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public DatabaseSelectionProvider copyTo(DatabaseSelectionProvider dsp) {
        try {
            BeanUtils.copyProperties(dsp, this);
        } catch (Exception e) {
            throw new Error(e);
        }
        return dsp;
    }

    @Override
    @Required
    @FieldSize(50)
    public String getName() {
        return super.getName();
    }

    @Override
    @Required
    public String getToDatabase() {
        return super.getToDatabase();
    }

    @Override
    @Multiline
    public String getHql() {
        return super.getHql();
    }

    @Override
    @Multiline
    public String getSql() {
        return super.getSql();
    }

    @FieldSize(75)
    @Required
    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }
}
