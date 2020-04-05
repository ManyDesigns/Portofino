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

import com.manydesigns.portofino.actions.ActionDescriptor;
import com.manydesigns.portofino.actions.Permissions;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>An ActionInstance is the realization of a Portofino resource action on permanent storage (action.xml +
 * configuration.xml + action.groovy) into a live object in the context of a single http request.<br>
 * A ActionInstance includes the parameters extracted from the decomposition of the URL in a sequence of
 * <em>segments</em> (not to be confused with query string parameters).</p>
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla - alessio.stalla@manydesigns.com
 */
public class ActionInstance {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    protected final ActionDescriptor actionDescriptor;
    protected final FileObject directory;
    protected final List<String> parameters;
    protected final ActionInstance parent;
    protected final Class<? extends ResourceAction> actionClass;
    protected Object configuration;
    protected ResourceAction actionBean;
    protected boolean prepared;

    public static final String DETAIL = "_detail";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(ActionInstance.class);

    public ActionInstance(ActionInstance parent, FileObject directory,
                          ActionDescriptor actionDescriptor, Class<? extends ResourceAction> actionClass) {
        this.parent = parent;
        this.directory = directory;
        this.actionDescriptor = actionDescriptor;
        this.actionClass = actionClass;
        parameters = new ArrayList<>();
    }

    public ActionInstance copy() {
        ActionInstance actionInstance = new ActionInstance(parent, directory, actionDescriptor, actionClass);
        actionInstance.prepared = false;
        actionInstance.parameters.addAll(parameters);
        actionInstance.configuration = configuration;
        actionInstance.actionBean = actionBean;
        return actionInstance;
    }

    public ActionDescriptor getActionDescriptor() {
        return actionDescriptor;
    }

    //**************************************************************************
    // Utility Methods
    //**************************************************************************

    /**
     * Returns the portion of the URL that identifies this ActionInstance, including any parameters.
     */
    public String getUrlSegment() {
        String segment = directory.getName().getBaseName();
        for(String param : parameters) {
            segment += "/" + param;
        }
        return segment;
    }

    /**
     * Reconstructs the URL path to this ActionInstance from the rootFactory of the application (not including the
     * webapp's context path).
     */
    public String getPath() {
        if(getParent() == null) {
            return "";
        } else {
            return getParent().getPath() + "/" + getUrlSegment();
        }
    }

    /**
     * Returns the directory from which this action was loaded.
     */
    public FileObject getDirectory() {
        return directory;
    }

    /**
     * Returns the parameters extracted from the URL for this action.
     */
    public List<String> getParameters() {
        return parameters;
    }

    /**
     * Returns the configuration object for this action.
     */
    public Object getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Object configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns the class that implements this action.
     */
    public Class<? extends ResourceAction> getActionClass() {
        return actionClass;
    }

    /**
     * Returns an object that implements this action.
     * @see #getActionClass()
     */
    public ResourceAction getActionBean() {
        return actionBean;
    }

    public void setActionBean(ResourceAction actionBean) {
        this.actionBean = actionBean;
    }

    /**
     * Returns the ActionInstance that precedes this one in the path, or null if this is an instance of the root action.
     */
    public ActionInstance getParent() {
        return parent;
    }

    public Permissions getPermissions() {
        return actionDescriptor.getPermissions();
    }

    public FileObject getChildPageDirectory(String name) {
        FileObject baseDir = getChildrenDirectory();
        try {
            return baseDir.resolveFile(name);
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    public FileObject getChildrenDirectory() {
        FileObject baseDir = directory;
        if(!parameters.isEmpty()) {
            try {
                baseDir = baseDir.resolveFile(DETAIL);
            } catch (FileSystemException e) {
                throw new RuntimeException(e);
            }
        }
        return baseDir;
    }

    public String getName() {
        return directory.getName().getBaseName();
    }

}
