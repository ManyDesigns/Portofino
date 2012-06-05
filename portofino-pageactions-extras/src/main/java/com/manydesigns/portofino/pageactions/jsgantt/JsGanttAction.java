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

package com.manydesigns.portofino.pageactions.jsgantt;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.xml.XmlBuffer;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.jsgantt.configuration.JsGanttConfiguration;
import com.manydesigns.portofino.pageactions.jsgantt.model.ProjectModel;
import com.manydesigns.portofino.pageactions.jsgantt.model.Task;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.stripes.NoCacheStreamingResolution;
import net.sourceforge.stripes.action.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@ConfigurationClass(JsGanttConfiguration.class)
@PageActionName("jsGantt")
public class JsGanttAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************


    //**************************************************************************
    // Variables
    //**************************************************************************
    protected ProjectModel projectModel;

    //**************************************************************************
    // Support objects
    //**************************************************************************

    protected Form configurationForm;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(JsGanttAction.class);

    //**************************************************************************
    // Setup & configuration
    //**************************************************************************

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        Resolution resolution = super.prepare(pageInstance, context);
        if(resolution != null) {
            return resolution;
        }
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        if(pageInstance.getConfiguration() == null) {
            pageInstance.setConfiguration(new JsGanttConfiguration());
        }
        return null;
    }




    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1, icon = "ui-icon-wrench")
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/jsgantt/configure.jsp");
    }

    @Button(list = "configuration", key = "commons.updateConfiguration")
    @RequiresPermissions(level = AccessLevel.DEVELOP)
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
            return new ForwardResolution("/layouts/jsgantt/configure.jsp");
        }
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        configurationForm = new FormBuilder(JsGanttConfiguration.class)
                .configFields( "captionType", "dateDisplayFormat", "showStartDate",
                        "showEndDate", "showResource", "showDuration", "showComplete" ).build();
        configurationForm.readFromObject(pageInstance.getConfiguration());
    }



    //**************************************************************************
    // Default view
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
//        return new ForwardResolution("/layouts/jsgantt/view.jsp");
        return forwardTo("/layouts/jsgantt/view.jsp");
    }

    public Resolution xmlData() {
        projectModel = new ProjectModel();
        loadProjectModel();
        XmlBuffer xb = new XmlBuffer();
        xb.openElement("project");

        for(Task task : projectModel.getTasks()){
            xb.openElement("task");
            printTaskElement(xb, "pID", Integer.toString(task.getId()));
            printTaskElement(xb, "pName", task.getName());
            printTaskElement(xb, "pStart", task.getStart());
            printTaskElement(xb, "pEnd", task.getEnd());
            printTaskElement(xb, "pColor", task.getColor());
            printTaskElement(xb, "pLink", task.getLink());
            printTaskElement(xb, "pMile", Integer.toString(task.getMile()));
            printTaskElement(xb, "pRes", task.getResource());
            printTaskElement(xb, "pComp", Integer.toString(task.getComp()));
            printTaskElement(xb, "pGroup", Integer.toString(task.getGroup()));
            if(task.getParent()!=null) {
                printTaskElement(xb, "pParent", Integer.toString(task.getParent().getId()));
            } else {
                printTaskElement(xb, "pParent", "0");
            }
            printTaskElement(xb, "pOpen", Integer.toString(task.getOpen()));
            printTaskElement(xb, "pDepend", StringUtils.join(task.getDepend(), ","));
            xb.closeElement("task");
        }
        xb.closeElement("project");


        return new NoCacheStreamingResolution("text/xml", xb.toString());
    }

    private void printTaskElement(XmlBuffer xb, String pStart, DateTime start) {
        String text = (start == null) ? "" : start.toString("dd/MM/yyyy") ;
        printTaskElement(xb, pStart, text);
    }

    private void printTaskElement(XmlBuffer xb, String pId, String s) {
        xb.openElement(pId);
        xb.write(s);
        xb.closeElement(pId);
    }


    //--------------------------------------------------------------------------
    // Data provider
    //--------------------------------------------------------------------------
    protected void loadProjectModel() {
        DateTime d1 = new DateTime();
        DateTime d2 = d1.plusDays(4);
        DateTime d3 = d2.plusDays(4);
        DateTime d4 = d3.plusDays(2);
        DateTime d5 = d4.plusDays(2);
        DateTime d6 = d5.plusDays(2);

        Task t1 = new Task(10, "WCF Changes", null, null, "0000ff", null, 0, "Giampiero", 0, 1, 1);
        Task t2 = new Task(20, "Move to WCF from remoting", d1, d2, "99ff55", null, 0, "Angelo", 100, 0,1);
        Task t3 = new Task(30, "add Auditing", d3, d4, "99ff55", null, 0, "Paolo", 20, 0, 1);
        Task t4 = new Task(40, "Yet another task", d5, d6, "99ff55", null, 0, "Alessio", 3, 0, 1);
        Task t5 = new Task(50, "Project End", d6, d6, "99ff55", null, 1, "Giampiero", 100, 0, 1);

        t3.getDepend().add(20);
        t2.setParent(t1);
        t3.setParent(t1);

        projectModel.getTasks().add(t1);
        projectModel.getTasks().add(t2);
        projectModel.getTasks().add(t3);
        projectModel.getTasks().add(t4);
        projectModel.getTasks().add(t5);


    }
    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------
    public Form getConfigurationForm() {
        return configurationForm;
    }

    public ProjectModel getProjectModel() {
        return projectModel;
    }

    public JsGanttConfiguration getConfiguration() {
        return (JsGanttConfiguration) pageInstance.getConfiguration();
    }

}
