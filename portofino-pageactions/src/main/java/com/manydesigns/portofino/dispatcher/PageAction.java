/*
 * Copyright (C) 2005-2017 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.portofino.dispatcher.security.SecureResource;
import org.apache.commons.collections.MultiMap;

import javax.ws.rs.core.Response;

/**
 * An element in Portofino's hierarchical resource structure.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface PageAction extends DispatchElement, SecureResource {
    String copyright = "Copyright (C) 2005-2017 ManyDesigns srl";

    /**
     * A lifecycle method invoked during the dispatch phase, where a http request is translated to a dispatch
     * (a path in the page tree) ultimately leading to an action invocation. This method is invoked after
     * the PageInstance has been injected by the framework.<br>
     * Also, this method is invoked on this object after it has been invoked on the PageAction corresponding
     * to the parent page, if any.
     * @return either null, meaning that the dispatch process is to go forward regularly, or a Resolution to be
     * executed, interrupting normal action invocation.
     */
    Response preparePage();

    /**
     * A lifecycle method invoked just before an action method is invoked.
     */
    void init();

    PageActionContext getContext();

    void setContext(PageActionContext context);

    PageAction getParent();

}
