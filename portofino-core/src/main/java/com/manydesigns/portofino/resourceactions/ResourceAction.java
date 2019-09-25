/*
 * Copyright (C) 2005-2019 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.resourceactions;

import com.manydesigns.portofino.dispatcher.security.SecureResource;

/**
 * An element in Portofino's hierarchical resource structure.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface ResourceAction extends SecureResource {
    String copyright = "Copyright (C) 2005-2019 ManyDesigns srl";

    ActionContext getContext();

    void setContext(ActionContext context);

    /**
     * Returns the action that comes before this action in the matched request path.
     * @since 5.0.0
     * @return the parent {@link ResourceAction}.
     */
    ResourceAction getParent();

    /**
     * Returns the ActionInstance of this element.
     * @return the {@link ActionInstance}.
     */
    ActionInstance getActionInstance();

    /**
     * Sets the ActionInstance of this element. Invoked automatically by the framework.
     */
    void setActionInstance(ActionInstance actionInstance);

    /**
     * Lifecycle method invoked just before the resource method is invoked via REST.
     * @since 5.0.0
     */
    void prepareForExecution();


}
