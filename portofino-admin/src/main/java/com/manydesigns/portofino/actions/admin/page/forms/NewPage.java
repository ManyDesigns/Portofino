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

package com.manydesigns.portofino.actions.admin.page.forms;

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
            "Copyright (C) 2005-2024 ManyDesigns srl";

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

    @RegExp(value = "[a-zA-Z0-9][a-zA-Z0-9_\\-]*", errorMessage = "invalid.fragment.only.letters.numbers.etc.are.allowed")
    @Required
    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }
}
