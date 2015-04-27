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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.pages.NavigationRoot;
import net.sourceforge.stripes.action.ActionBean;

/**
 * A <i>Dispatch</i> is an object representing a path of page instances from the root of the application,
 * where each page except the first is an instance of a child (subdirectory) of the previous one.
 * Additionally, a dispatch retains information about the path that was requested to the Web server
 * and that generated the dispatch itself.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Dispatch {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    protected final PageInstance[] pageInstancePath;

    public Dispatch(PageInstance... pageInstancePath) {
        this.pageInstancePath = pageInstancePath;
    }

    /**
     * Returns the path of pages that constitute the dispatch.
     */
    public PageInstance[] getPageInstancePath() {
        return pageInstancePath;
    }

    /**
     * Returns a subpath of the page instance path.
     * @param startIndex the index in the path that the subpath starts from.
     */
    public PageInstance[] getPageInstancePath(int startIndex) {
        return Util.copyOfRange(pageInstancePath, startIndex, pageInstancePath.length);
    }

    /**
     * Returns the root page instance, the first in the path.
     */
    public PageInstance getRootPageInstance() {
        return pageInstancePath[0];
    }

    /**
     * Returns the last page instance in the path.
     */
    public PageInstance getLastPageInstance() {
        return pageInstancePath[pageInstancePath.length - 1];
    }

    public PageInstance getPageInstance(int index) {
        if(index >= 0) {
            return getPageInstancePath()[index];
        } else {
            return getPageInstancePath()[getPageInstancePath().length + index];
        }
    }

    public int getClosestSubtreeRootIndex() {
        PageInstance[] path = getPageInstancePath();
        for(int i = path.length - 1; i > 0; i--) {
            if(path[i].getPage().getActualNavigationRoot() != NavigationRoot.INHERIT) {
                return i;
            }
        }
        return 0;
    }

    public Class<? extends ActionBean> getActionBeanClass() {
        return getLastPageInstance().getActionClass();
    }

}
