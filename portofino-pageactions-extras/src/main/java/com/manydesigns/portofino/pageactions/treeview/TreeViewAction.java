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

package com.manydesigns.portofino.pageactions.treeview;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.treeview.configuration.TreeViewConfiguration;
import com.manydesigns.portofino.pageactions.treeview.model.Node;
import com.manydesigns.portofino.pageactions.treeview.model.TreeModel;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@ConfigurationClass(TreeViewConfiguration.class)
@PageActionName("TreeView")
public class TreeViewAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************


    //**************************************************************************
    // Variables
    //**************************************************************************
    protected TreeModel treeModel = new TreeModel();

    //**************************************************************************
    // Support objects
    //**************************************************************************

    protected Form configurationForm;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(TreeViewAction.class);

    //**************************************************************************
    // Setup & configuration
    //**************************************************************************

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        this.pageInstance = pageInstance;
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        if(pageInstance.getConfiguration() == null) {
            pageInstance.setConfiguration(new TreeViewConfiguration());
        }
        return null;
    }




    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1, icon = "ui-icon-wrench")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/tree-view/configure.jsp");
    }

    @Button(list = "configuration", key = "commons.updateConfiguration")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution updateConfiguration() {
        prepareConfigurationForms();
        readPageConfigurationFromRequest();
        configurationForm.readFromRequest(context.getRequest());
        boolean valid = validatePageConfiguration();
        valid = valid && configurationForm.validate();
        if(valid) {
            updatePageConfiguration();
            configurationForm.writeToObject(pageInstance.getConfiguration());
            saveConfiguration(pageInstance.getConfiguration());
            SessionMessages.addInfoMessage(getMessage("commons.configuration.updated"));
            return cancel();
        } else {
            SessionMessages.addErrorMessage(getMessage("commons.configuration.notUpdated"));
            return new ForwardResolution("/layouts/tree-view/configure.jsp");
        }
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        configurationForm = new FormBuilder(TreeViewConfiguration.class)
                .build();
        configurationForm.readFromObject(pageInstance.getConfiguration());
    }



    //**************************************************************************
    // Default view
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        return forwardTo("/layouts/tree-view/view.jsp");
    }

    //--------------------------------------------------------------------------
    // Data provider
    //--------------------------------------------------------------------------
    public void loadTreeModel() {
        int i=1;

        Node n1 = new Node(i++, new Object[]{"Node 1", "data", 0.00});
        Node n2 = new Node(i++, new Object[]{"Node 2", "data", 10.00});
        Node n3 = new Node(i++, new Object[]{"Node 3", "data", 340.00});
        Node n4 = new Node(i++, new Object[]{"Node 4", "data", 210.00});
        Node n5 = new Node(i++, new Object[]{"Node 5", "data", 120.00});
        n1.getChildren().add(n2);
        n1.getChildren().add(n3);
        n3.getChildren().add(n4);
        n4.getChildren().add(n5);

        treeModel.getNodes().add(n1);
        treeModel.getHeaders().add("Name");
        treeModel.getHeaders().add("Data");
        treeModel.getHeaders().add("Decimal");
    }
    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------
    public Form getConfigurationForm() {
        return configurationForm;
    }

    public TreeModel getTreeModel() {
        return treeModel;
    }

    public TreeViewConfiguration getConfiguration() {
        return (TreeViewConfiguration) pageInstance.getConfiguration();
    }

}
