/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.admin.database.forms;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Updatable;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SelectableSchema {
    public static final String copyright =
            "Copyright (C) 2005-2021 ManyDesigns srl";

    @Updatable(false)
    public final String catalogName;
    @Updatable(false)
    public final String schema;
    @Updatable(false)
    public String schemaName;
    @Label("")
    public boolean selected;

    public SelectableSchema(String catalogName, String schemaName,  String schema,boolean selected) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.schema = schema;
        this.selected = selected;
    }
}
