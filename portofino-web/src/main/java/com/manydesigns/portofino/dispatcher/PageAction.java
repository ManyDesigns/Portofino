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

package com.manydesigns.portofino.dispatcher;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Resolution;

/**
 * An extension of ActionBean from the Stripes framework to handle Portofino's hierarchical page structure.
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface PageAction extends ActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    /**
     * A lifecycle method invoked during the dispatch phase, where a http request is translated to a dispatch
     * (a path in the page tree) ultimately leading to an action invocation. This method is invoked after
     * the PageInstance and the Dispatch have been injected by the framework.<br />
     * Also, this method is invoked on this object after it has been invoked on the PageAction corresponding
     * to the parent page, if any.
     * @return either null, meaning that the dispatch process is to go forward regularly, or a Resolution to be
     * executed, interrupting normal action invocation.
     */
    Resolution preparePage();

    /**
     * Returns a textual description of the page, for use e.g. in links, breadcrumbs, and the like.
     */
    String getDescription();

    /**
     * Returns the PageInstance of this action.
     */
    PageInstance getPageInstance();

    /**
     * Sets the PageInstance of this action. Invoked automatically by the framework before calling preparePage().
     */
    void setPageInstance(PageInstance pageInstance);

    /**
     * Sets the Dispatch which this action belongs to. The action is not necessarily the last one in the dispatch.<br />
     * This method is invoked automatically by the framework before calling preparePage().
     */
    void setDispatch(Dispatch dispatch);

    /**
     * Returns the Dispatch this action belongs to.
     */
    Dispatch getDispatch();

    /**
     * Returns whether this page is embedded in its parent.
     * @return true if the page is embedded, false otherwise.
     */
    boolean isEmbedded();
    
}
