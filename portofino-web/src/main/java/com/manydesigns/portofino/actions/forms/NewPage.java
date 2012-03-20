/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.actions.forms;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.RegExp;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.options.DisplayMode;
import com.manydesigns.portofino.pages.Page;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class NewPage extends Page {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected String actionClassName;
    protected String insertPositionName;
    protected String fragment;

    @Label("Page type")
    @Required
    public String getActionClassName() {
        return actionClassName;
    }

    public void setActionClassName(String actionClassName) {
        this.actionClassName = actionClassName;
    }

    @Label("Where")
    @Select(displayMode = DisplayMode.RADIO)
    @Required
    public String getInsertPositionName() {
        return insertPositionName;
    }

    public void setInsertPositionName(String insertPositionName) {
        this.insertPositionName = insertPositionName;
    }

    @RegExp(value = "[a-zA-Z0-9][a-zA-Z0-9_\\-]*", errorMessage = "page.invalid.fragment")
    @Required
    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }
}
