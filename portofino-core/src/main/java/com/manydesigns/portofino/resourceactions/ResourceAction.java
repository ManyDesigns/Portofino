/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.security.SecurityFacade;
import org.apache.commons.vfs2.FileObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An element in Portofino's hierarchical resource structure.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface ResourceAction extends SecureResource {
    String copyright = "Copyright (C) 2005-2020 ManyDesigns srl";

    ActionContext getContext();

    SecurityFacade getSecurity();

    void setContext(ActionContext context);

    /**
     * Returns the action that comes before this action in the matched request path.
     * @since 5.0
     * @return the parent {@link ResourceAction}.
     */
    ResourceAction getParent();

    /**
     * Returns the ActionInstance of this element.
     * @return the {@link ActionInstance}.
     */
    ActionInstance getActionInstance();

    default ResourceActionConfiguration getConfiguration() {
        return getActionInstance().getConfiguration();
    }

    /**
     * Sets the ActionInstance of this element. Invoked automatically by the framework.
     * @param actionInstance the new {@link ActionInstance}.
     */
    void setActionInstance(ActionInstance actionInstance);

    /**
     * Lifecycle method invoked just before the resource method is invoked via REST.
     * @since 5.0
     */
    void prepareForExecution();

    /**
     * Security check method. It is invoked as part of the security check logic, and it can also be invoked by
     * the client to check if the action is accessible. If it returns false, the request is terminated with a 401 or 403
     * status (according to Portofino's security logic).
     * By default, it always returns true, but implementations can provide custom logic.
     * @return true if the action is accessible. Note that a client will either receive true or a 401/403 status.
     */
    boolean isAccessible();

    /**
     * Returns the list of accessible children (according to {@link #isAccessible()}).
     */
    List<String> getAccessibleChildren();

    Domain getConfigurationDomain();

    /**
     * Loads this action's configuration.
     * @return the loaded configuration.
     * @throws Exception in case the configuration cannot be loaded.
     */
    ResourceActionConfiguration loadConfiguration() throws Exception;
    void setConfiguration(ResourceActionConfiguration configuration);
    void saveConfiguration() throws Exception;
    void configured();

    //Mount points
    void mount(@NotNull String segment, @NotNull String path) throws Exception;

    default void mount(@NotNull String segment, @NotNull Class<?> actionClass) throws Exception {
        mount(segment, "res:" + actionClass.getName().replace('.', '/') + ".class");
    }

    default void mountPackage(@NotNull String segment, @NotNull String packageName) throws Exception {
        mount(segment, "res:" + packageName.replace('.', '/'));
    }

    default void mountPackage(@NotNull String segment, @NotNull Package pkg) throws Exception {
        mountPackage(segment, pkg.getName());
    }

    void unmount(@NotNull String segment) throws Exception;
}
