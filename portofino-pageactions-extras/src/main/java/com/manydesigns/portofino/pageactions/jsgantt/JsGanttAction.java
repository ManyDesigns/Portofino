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

package com.manydesigns.portofino.pageactions.jsgantt;

import com.manydesigns.elements.xml.XmlBuffer;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.jsgantt.configuration.JsGanttConfiguration;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.stripes.NoCacheStreamingResolution;
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
@ConfigurationClass(JsGanttConfiguration.class)
public class JsGanttAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************


    //**************************************************************************
    // Variables
    //**************************************************************************



    //**************************************************************************
    // Injections
    //**************************************************************************

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(JsGanttAction.class);

    //**************************************************************************
    // Setup & configuration
    //**************************************************************************

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        this.pageInstance = pageInstance;
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        if(pageInstance.getConfiguration() == null) {
            pageInstance.setConfiguration(new JsGanttConfiguration());
        }
        return null;
    }


    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1, icon = "ui-icon-wrench")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/jsgantt/configure.jsp");
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
        XmlBuffer xb = new XmlBuffer();


        String text = "<project>\n" +
                "<task>\n" +
                "\t<pID>10</pID>\n" +
                "\t<pName>WCF Changes</pName>\n" +
                "\t<pStart></pStart>\n" +
                "\t<pEnd></pEnd>\n" +
                "\t<pColor>0000ff</pColor>\n" +
                "\t<pLink></pLink>\n" +
                "\t<pMile>0</pMile>\n" +
                "\t<pRes></pRes>\n" +
                "\t<pComp>0</pComp>\n" +
                "\t<pGroup>1</pGroup>\n" +
                "\t<pParent>0</pParent>\n" +
                "\t<pOpen>1</pOpen>\n" +
                "\t<pDepend />\n" +
                "</task>\n" +
                "<task>\n" +
                "\t<pID>20</pID>\n" +
                "\t<pName>Move to WCF from remoting</pName>\n" +
                "\t<pStart>9/11/2008</pStart>\n" +
                "\t<pEnd>9/15/2008</pEnd>\n" +
                "\t<pColor>0000ff</pColor>\n" +
                "\t<pLink></pLink>\n" +
                "\t<pMile>0</pMile>\n" +
                "\t<pRes>Rich</pRes>\n" +
                "\t<pComp>10</pComp>\n" +
                "\t<pGroup>0</pGroup>\n" +
                "\t<pParent>10</pParent>\n" +
                "\t<pOpen>1</pOpen>\n" +
                "\t<pDepend></pDepend>\n" +
                "\t<pCaption>Brian</pCaption>\n" +
                "</task>\n" +
                "<task>\n" +
                "\t<pID>30</pID>\n" +
                "\t<pName>add Auditing</pName>\n" +
                "\t<pStart>9/19/2008</pStart>\n" +
                "\t<pEnd>9/21/2008</pEnd>\n" +
                "\t<pColor>0000ff</pColor>\n" +
                "\t<pLink></pLink>\n" +
                "\t<pMile>0</pMile>\n" +
                "\t<pRes>Shlomy</pRes>\n" +
                "\t<pComp>50</pComp>\n" +
                "\t<pGroup>0</pGroup>\n" +
                "\t<pParent>10</pParent>\n" +
                "\t<pOpen>1</pOpen>\n" +
                "\t<pDepend>20</pDepend>\n" +
                "\t<pCaption>Shlomy</pCaption>\n" +
                "</task>\n" +
                "<task>\n" +
                "\t<pID>40</pID>\n" +
                "\t<pName>Yet another task</pName>\n" +
                "\t<pStart>9/23/2008</pStart>\n" +
                "\t<pEnd>9/24/2008</pEnd>\n" +
                "\t<pColor>0000ff</pColor>\n" +
                "\t<pLink></pLink>\n" +
                "\t<pMile>0</pMile>\n" +
                "\t<pRes>Shlomy</pRes>\n" +
                "\t<pComp>30</pComp>\n" +
                "\t<pGroup>0</pGroup>\n" +
                "\t<pParent>0</pParent>\n" +
                "\t<pOpen>1</pOpen>\n" +
                "\t<pDepend>20,30</pDepend>\n" +
                "\t<pCaption>Shlomy</pCaption>\n" +
                "</task>\n" +
                "</project>";

        return new NoCacheStreamingResolution("text/xml", text);
    }




    //--------------------------------------------------------------------------
    // Data provider
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

}
