/*
* Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.model;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.pages.Page;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class NewPage extends Page {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected String pageClassName;
    protected String insertPositionName;
    protected InsertPosition insertPosition;

    @Override
    public void init(Model model) {
        super.init(model);
        insertPosition = InsertPosition.valueOf(insertPositionName);
    }

    @Label("Page type")
    @Required
    public String getPageClassName() {
        return pageClassName;
    }

    public void setPageClassName(String pageClassName) {
        this.pageClassName = pageClassName;
    }

    @Label("Put the new page")
    @Select(displayMode = SelectField.DisplayMode.RADIO)
    @Required
    public String getInsertPositionName() {
        return insertPositionName;
    }

    public void setInsertPositionName(String insertPositionName) {
        this.insertPositionName = insertPositionName;
    }

    public InsertPosition getInsertPosition() {
        return insertPosition;
    }

    public void setInsertPosition(InsertPosition insertPosition) {
        this.insertPosition = insertPosition;
    }

    public static enum InsertPosition {
        TOP, CHILD, SIBLING;
    }
}
