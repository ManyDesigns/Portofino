/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.modules;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.admin.modules.ModulesAction;
import com.manydesigns.portofino.actions.admin.servletcontext.ServletContextAction;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.head.HtmlHead;
import com.manydesigns.portofino.head.HtmlHeadAppender;
import com.manydesigns.portofino.head.HtmlHeadBuilder;
import com.manydesigns.portofino.menu.MenuBuilder;
import com.manydesigns.portofino.menu.SimpleMenuAppender;
import org.apache.commons.configuration.Configuration;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class BaseModule implements Module {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final static String SERVLET_CONTEXT = "com.manydesigns.portofino.servletContext";
    public final static String PORTOFINO_CONFIGURATION = "portofinoConfiguration";
    public final static String APPLICATION_DIRECTORY = "com.manydesigns.portofino.application.directory";
    public final static String RESOURCE_BUNDLE_MANAGER = "com.manydesigns.portofino.resourceBundleManager";
    public final static String ELEMENTS_CONFIGURATION = "com.manydesigns.portofino.elementsConfiguration";
    public final static String SERVER_INFO = "com.manydesigns.portofino.serverInfo";
    public final static String APP_LISTENERS = "com.manydesigns.portofino.application.listeners";
    public final static String CLASS_LOADER = "com.manydesigns.portofino.application.classLoader";
    public final static String MODULE_REGISTRY = "com.manydesigns.portofino.modules.ModuleRegistry";
    public final static String USER_MENU = "com.manydesigns.portofino.menu.Menu.user";
    public final static String APP_MENU = "com.manydesigns.portofino.menu.Menu.app";
    public final static String ADMIN_MENU = "com.manydesigns.portofino.menu.Menu.admin";
    public final static String HTML_HEAD_BUILDER = "com.manydesigns.portofino.head.builder";

    //**************************************************************************
    // Injected objects
    //**************************************************************************

    @Inject(PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Inject(ADMIN_MENU)
    public MenuBuilder adminMenu;

    @Inject(BaseModule.HTML_HEAD_BUILDER)
    public HtmlHeadBuilder headBuilder;

    //**************************************************************************
    // Module implementation
    //**************************************************************************

    @Override
    public String getModuleVersion() {
        return configuration.getString(PortofinoProperties.PORTOFINO_VERSION);
    }

    @Override
    public int getMigrationVersion() {
        return 1;
    }

    @Override
    public double getPriority() {
        return 0;
    }

    @Override
    public String getId() {
        return "base";
    }

    @Override
    public String getName() {
        return "Portofino Base";
    }

    @Override
    public int install() {
        return getMigrationVersion();
    }

    @Override
    public void init() {
        SimpleMenuAppender group = SimpleMenuAppender.group("configuration", null, "Configuration", 1.0);
        adminMenu.menuAppenders.add(group);

        SimpleMenuAppender link = SimpleMenuAppender.link(
                "configuration", "modules", null, "Modules", ModulesAction.URL_BINDING, 1.0);
        adminMenu.menuAppenders.add(link);

        link = SimpleMenuAppender.link(
                "configuration", "servlet-context", null, "Servlet Context", ServletContextAction.URL_BINDING, 2.0);
        adminMenu.menuAppenders.add(link);

        setupHead();

        status = ModuleStatus.ACTIVE;
    }

    protected void setupHead() {
        headBuilder.appenders.add(new HtmlHeadAppender() {
            @Override
            public void append(HtmlHead head) {
                XhtmlBuffer xb = new XhtmlBuffer();
                xb.openElement("meta");
                xb.addAttribute("http-equiv", "Content-Type");
                xb.addAttribute("content", "text/html;charset=UTF-8");
                xb.closeElement("meta");
                xb.openElement("meta");
                xb.addAttribute("name", "viewport");
                xb.addAttribute("content", "width=device-width, initial-scale=1.0");
                xb.closeElement("meta");

                //HTML5 shim, for IE6-8 support of HTML5 elements
                xb.writeNoHtmlEscape(
                        "<!--[if lt IE 9]>\n" +
                        "      <script src=\"http://html5shim.googlecode.com/svn/trunk/html5.js\"></script>\n" +
                        "<![endif]-->");

                String contextPath = ElementsThreadLocals.getHttpServletRequest().getContextPath();
                xb.writeLink("stylesheet", "text/css", contextPath + "/elements/bootstrap/css/bootstrap.min.css");
                xb.writeLink("stylesheet", "text/css", contextPath + "/elements/bootstrap/css/bootstrap-responsive.min.css");
                xb.writeLink("stylesheet", "text/css", contextPath + "/elements/datepicker/css/datepicker.css");

                xb.writeStyle("body { padding-top: 50px; }");

                xb.openElement("script");
                xb.addAttribute("src", contextPath + "/elements/jquery/jquery.min.js");
                xb.closeElement("script");
                xb.openElement("script");
                xb.addAttribute("src", contextPath + "/elements/elements.js");
                xb.closeElement("script");

                xb.openElement("script");
                xb.addAttribute("src", contextPath + "/elements/bootstrap/js/bootstrap.min.js");
                xb.closeElement("script");
                xb.openElement("script");
                xb.addAttribute("src", contextPath + "/elements/datepicker/js/bootstrap-datepicker.js");
                xb.closeElement("script");
                head.fragments.add(xb);
            }
        });
    }

    @Override
    public void destroy() {
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
