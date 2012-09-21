/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.pages.Layout;
import com.manydesigns.portofino.pages.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A PageInstance is the realization of a Portofino page on permanent storage (page.xml + configuration.xml +
 * action.groovy) into a live object in the context of a single http request.<br />
 * A PageInstance includes the parameters extracted from the decomposition of the URL in a sequence of
 * <i>fragments</i> (not to be confused with query string parameters).</p>
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla - alessio.stalla@manydesigns.com
 */
public class PageInstance {

    protected final Application application;
    protected final Page page;
    protected final File directory;
    protected final List<String> parameters;
    protected final PageInstance parent;
    protected final Class<? extends PageAction> actionClass;
    protected Object configuration;
    protected PageAction actionBean;
    protected String description;
    protected boolean prepared;

    public static final String DETAIL = "_detail";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(PageInstance.class);

    public PageInstance(PageInstance parent, File directory, Application application,
                        Page page, Class<? extends PageAction> actionClass) {
        this.parent = parent;
        this.directory = directory;
        this.application = application;
        this.page = page;
        this.actionClass = actionClass;
        parameters = new ArrayList<String>();
    }

    public PageInstance copy() {
        PageInstance pageInstance = new PageInstance(parent, directory, application, page, actionClass);
        pageInstance.prepared = false;
        pageInstance.parameters.addAll(parameters);
        pageInstance.configuration = configuration;
        pageInstance.actionBean = actionBean;
        pageInstance.description = description;
        return pageInstance;
    }

    public Page getPage() {
        return page;
    }

    /**
     * The application which the page belongs to.
     */
    public Application getApplication() {
        return application;
    }

    //**************************************************************************
    // Utility Methods
    //**************************************************************************

    /**
     * Returns the portion of the URL that identifies this PageInstance, including any parameters.
     */
    public String getUrlFragment() {
        String fragment = directory.getName();
        for(String param : parameters) {
            fragment += "/" + param;
        }
        return fragment;
    }

    /**
     * Reconstructs the URL path to this PageInstance from the root of the application (not including the
     * webapp's context path).
     */
    public String getPath() {
        if(getParent() == null) {
            return "/";
        } else {
            String parentPath = getParent().getPath();
            if(!parentPath.endsWith("/")) {
                parentPath += "/";
            }
            return parentPath + getUrlFragment();
        }
    }

    /**
     * Returns the directory from which this page was loaded.
     */
    public File getDirectory() {
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
     * Returns the page instance that precedes this one in the path, or null if this is an instance of the root page.
     */
    public PageInstance getParent() {
        return parent;
    }

    /**
     * Returns the layout that drives the display of the embedded children of this page.
     * If this page instance has parameters, the detail layout is returned; else, the regular layout is returned.
     * @return the layout.
     */
    public Layout getLayout() {
        if(getParameters().isEmpty()) {
            return getPage().getLayout();
        } else {
            return getPage().getDetailLayout();
        }
    }

    /**
     * Sets the layout that drives the display of the embedded children of this page.
     * If this page instance has parameters, the detail layout is set; else, the regular layout is set.
     * @param layout the new layout.
     */
    public void setLayout(Layout layout) {
        if(getParameters().isEmpty()) {
            getPage().setLayout(layout);
        } else {
            getPage().setDetailLayout(layout);
        }
    }

    public Page getChildPage(String name) throws Exception {
        File childDirectory = getChildPageDirectory(name);
        return DispatcherLogic.getPage(childDirectory);
    }

    public File getChildPageDirectory(String name) {
        File baseDir = getChildrenDirectory();
        return new File(baseDir, name);
    }

    public File getChildrenDirectory() {
        File baseDir = directory;
        if(!parameters.isEmpty()) {
            baseDir = new File(baseDir, DETAIL);
        }
        return baseDir;
    }

    public String getName() {
        return directory.getName();
    }

    public String getPathFromRoot() {
        return ElementsFileUtils.getRelativePath(application.getPagesDir(), directory);
    }

    public String getDescription() {
        return description != null ? description : getName();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPrepared() {
        return prepared;
    }

    public void setPrepared(boolean prepared) {
        this.prepared = prepared;
    }
}
