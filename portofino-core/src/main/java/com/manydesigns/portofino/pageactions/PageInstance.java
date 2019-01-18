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

package com.manydesigns.portofino.pageactions;

import com.manydesigns.portofino.pages.PageLogic;
import com.manydesigns.portofino.pages.Page;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A PageInstance is the realization of a Portofino page on permanent storage (page.xml + configuration.xml +
 * action.groovy) into a live object in the context of a single http request.<br>
 * A PageInstance includes the parameters extracted from the decomposition of the URL in a sequence of
 * <em>fragments</em> (not to be confused with query string parameters).</p>
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla - alessio.stalla@manydesigns.com
 */
public class PageInstance {

    protected final Page page;
    protected final FileObject directory;
    protected final List<String> parameters;
    protected final PageInstance parent;
    protected final Class<? extends PageAction> actionClass;
    protected Object configuration;
    protected PageAction actionBean;
    protected boolean prepared;

    public static final String DETAIL = "_detail";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(PageInstance.class);

    public PageInstance(PageInstance parent, FileObject directory,
                        Page page, Class<? extends PageAction> actionClass) {
        this.parent = parent;
        this.directory = directory;
        this.page = page;
        this.actionClass = actionClass;
        parameters = new ArrayList<String>();
    }

    public PageInstance copy() {
        PageInstance pageInstance = new PageInstance(parent, directory, page, actionClass);
        pageInstance.prepared = false;
        pageInstance.parameters.addAll(parameters);
        pageInstance.configuration = configuration;
        pageInstance.actionBean = actionBean;
        return pageInstance;
    }

    public Page getPage() {
        return page;
    }

    //**************************************************************************
    // Utility Methods
    //**************************************************************************

    /**
     * Returns the portion of the URL that identifies this PageInstance, including any parameters.
     */
    public String getUrlFragment() {
        String fragment = directory.getName().getBaseName();
        for(String param : parameters) {
            fragment += "/" + param;
        }
        return fragment;
    }

    /**
     * Reconstructs the URL path to this PageInstance from the rootFactory of the application (not including the
     * webapp's context path).
     */
    public String getPath() {
        if(getParent() == null) {
            return "";
        } else {
            return getParent().getPath() + "/" + getUrlFragment();
        }
    }

    /**
     * Returns the directory from which this page was loaded.
     */
    public FileObject getDirectory() {
        return directory;
    }

    /**
     * Returns the parameters extracted from the URL for this page.
     */
    public List<String> getParameters() {
        return parameters;
    }

    /**
     * Returns the configuration object for this page.
     */
    public Object getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Object configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns the class that implements this page.
     */
    public Class<? extends PageAction> getActionClass() {
        return actionClass;
    }

    /**
     * Returns an object that implements this page.
     * @see #getActionClass()
     */
    public PageAction getActionBean() {
        return actionBean;
    }

    public void setActionBean(PageAction actionBean) {
        this.actionBean = actionBean;
    }

    /**
     * Returns the page instance that precedes this one in the path, or null if this is an instance of the rootFactory page.
     */
    public PageInstance getParent() {
        return parent;
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
                baseDir = baseDir.getChild(DETAIL);
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
