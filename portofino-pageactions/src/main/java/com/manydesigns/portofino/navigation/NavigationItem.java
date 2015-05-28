/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.navigation;

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
            "Copyright (c) 2005-2015, ManyDesigns srl";

    protected final String title;
    protected final String description;
    protected final String path;
    protected final boolean inPath;
    protected final boolean selected;
    protected final boolean ghost;

    protected final List<NavigationItem> childNavigationItems;

    public NavigationItem(
            String title, String description, String path, boolean inPath, boolean selected, boolean ghost) {
        this.title = title;
        this.description = description;
        this.path = path;
        this.inPath = inPath;
        this.selected = selected;
        this.ghost = ghost;
        childNavigationItems = new ArrayList<NavigationItem>();
    }

    //------------------------------------------------------------------------
    // Getters
    //------------------------------------------------------------------------

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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
