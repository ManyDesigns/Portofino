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
            "Copyright (c) 2005-2011, ManyDesigns srl";

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
