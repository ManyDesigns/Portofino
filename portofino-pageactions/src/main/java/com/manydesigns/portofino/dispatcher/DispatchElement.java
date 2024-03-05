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

package com.manydesigns.portofino.dispatcher;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface DispatchElement {

    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    /**
     * Returns the PageInstance of this element.
     */
    PageInstance getPageInstance();

    /**
     * Sets the PageInstance of this element. Invoked automatically by the framework.
     */
    void setPageInstance(PageInstance pageInstance);

    /**
     * Visits a path element.
     * @param pathFragment one of the elements of the path being visited (e.g. in /a/b/c, one path fragment might be
     *                     the string "a")
     * @return the next element in the dispatch, or null if not available. Typically it is either a sub-page action
     * matching the specified fragment, or `this` (the same element) if the fragment was used as a path parameter.
     * However, subclasses have freedom to return what they want.
     */
    public DispatchElement consumePathFragment(String pathFragment);

}
