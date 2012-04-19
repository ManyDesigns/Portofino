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

package com.manydesigns.portofino.navigation;

import com.manydesigns.portofino.pages.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class NavigationItem {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected final Page page;
    protected final String path;
    protected final boolean inPath;
    protected final boolean selected;
    protected final boolean ghost;

    protected final List<NavigationItem> childNavigationItems;

    public NavigationItem(Page page, String path, boolean inPath, boolean selected, boolean ghost) {
        this.page = page;
        this.path = path;
        this.inPath = inPath;
        this.selected = selected;
        this.ghost = ghost;
        childNavigationItems = new ArrayList<NavigationItem>();
    }

    //------------------------------------------------------------------------
    // Getters
    //------------------------------------------------------------------------


    public Page getPage() {
        return page;
    }

    public String getPath() {
        return path;
    }

    public boolean isInPath() {
        return inPath;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isGhost() {
        return ghost;
    }

    public List<NavigationItem> getChildNavigationItems() {
        return childNavigationItems;
    }
}
