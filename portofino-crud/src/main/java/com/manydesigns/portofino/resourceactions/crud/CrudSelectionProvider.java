/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.resourceactions.crud;

import com.manydesigns.elements.options.SelectionProvider;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class CrudSelectionProvider {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    protected final SelectionProvider selectionProvider;
    protected final String[] fieldNames;
    protected final boolean enforced;

    public CrudSelectionProvider(SelectionProvider selectionProvider, String[] fieldNames, boolean enforced) {
        this.selectionProvider = selectionProvider;
        this.fieldNames = fieldNames;
        this.enforced = enforced;
    }

    public SelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }

    /**
     * Whether Portofino will enforce the selection provider, making sure that submitted values fall within the
     * possible values, upon save and update. Foreign keys are not enforced, because we assume that the DB will
     * check them anyway, so we can avoid loading unnecessary data.
     * @return whether this s.p. is enforced by Portofino.
     */
    public boolean isEnforced() {
        return enforced;
    }
}
