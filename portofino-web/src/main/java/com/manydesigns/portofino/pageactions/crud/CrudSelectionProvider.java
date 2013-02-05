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

package com.manydesigns.portofino.pageactions.crud;

import com.manydesigns.elements.options.SelectionProvider;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class CrudSelectionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final SelectionProvider selectionProvider;
    protected final String[] fieldNames;

    protected final String createNewValueHref;
    protected final String createNewValueText;

    public CrudSelectionProvider(SelectionProvider selectionProvider,
                                 String[] fieldNames, String createNewValueHref, String createNewValueText) {
        this.selectionProvider = selectionProvider;
        this.fieldNames = fieldNames;
        this.createNewValueHref = createNewValueHref;
        this.createNewValueText = createNewValueText;
    }

    public SelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }

    public String getCreateNewValueHref() {
        return createNewValueHref;
    }

    public String getCreateNewValueText() {
        return createNewValueText;
    }
}
