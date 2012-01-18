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

package com.manydesigns.portofino.actions.jsp;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.portofino.actions.PortletAction;
import com.manydesigns.portofino.actions.jsp.configuration.JspConfiguration;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.model.pages.AccessLevel;
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions;
import com.manydesigns.portofino.util.FileUtils;
import net.sourceforge.stripes.action.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding("/actions/jsp")
@RequiresPermissions(level = AccessLevel.VIEW)
public class JspAction extends PortletAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected JspConfiguration jspConfiguration;
    protected String jsp;
    protected Form form;

    public static final Logger logger =
            LoggerFactory.getLogger(JspAction.class);

    //--------------------------------------------------------------------------
    // Scripting
    //--------------------------------------------------------------------------

    public static final String SCRIPT_TEMPLATE;

    static {
        String scriptTemplate;
        try {
            scriptTemplate = IOUtils.toString(JspAction.class.getResourceAsStream("script_template.txt"));
        } catch (Exception e) {
            throw new Error("Can't load script template", e);
        }
        SCRIPT_TEMPLATE = scriptTemplate;
    }

    @DefaultHandler
    public Resolution execute() {
        return forwardToJsp(jsp);
    }

    protected Resolution forwardToJsp(String jsp) {
        this.jsp = jsp;
        String fwd;
        if(StringUtils.isEmpty(jsp)) {
            fwd = PAGE_PORTLET_NOT_CONFIGURED;
        } else {
            fwd = "/layouts/jsp/view.jsp";
        }
        if(isEmbedded()) {
            return new ForwardResolution(fwd);
        } else {
            return forwardToPortletPage(fwd);
        }
    }

    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1, icon = "ui-icon-wrench")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/jsp/configure.jsp");
    }

    @Button(list = "configuration", key = "commons.updateConfiguration")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution updateConfiguration() {
        synchronized (application) {
            prepareConfigurationForms();
            readPageConfigurationFromRequest();
            form.readFromRequest(context.getRequest());
            boolean valid = validatePageConfiguration();
            valid = form.validate() && valid;
            if(valid) {
                updatePageConfiguration();
                form.writeToObject(jspConfiguration);
                saveConfiguration();

                SessionMessages.addInfoMessage(getMessage("commons.configuration.updated"));
            }
            return cancel();
        }
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();

        SelectionProvider jspSelectionProvider =
                createJspSelectionProvider();

        form = new FormBuilder(JspConfiguration.class)
                .configFields("jsp")
                .configSelectionProvider(jspSelectionProvider, "jsp")
                .build();
        form.readFromObject(jspConfiguration);
    }

    private SelectionProvider createJspSelectionProvider() {
        File appWebDir = application.getAppWebDir();
        File[] files = appWebDir.listFiles();
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider("jsp");
        visitJspFiles(appWebDir, files, selectionProvider);
        return selectionProvider;
    }

    private void visitJspFiles(File root, File[] files,
                               DefaultSelectionProvider selectionProvider) {
        for(File file : files) {
            if(file.isFile() && file.getName().endsWith(".jsp")) {
                String path = File.separator + FileUtils.getRelativePath(root, file);
                selectionProvider.appendRow(path, path, true);
            } else if(file.isDirectory()) {
                visitJspFiles(root, file.listFiles(), selectionProvider);
            }
        }
    }

    @Override
    public String getScriptTemplate() {
        return SCRIPT_TEMPLATE;
    }

    public JspConfiguration getJspConfiguration() {
        return jspConfiguration;
    }

    public String getJsp() {
        return jsp;
    }

    public Form getForm() {
        return form;
    }

    public String getJspPrefix() {
        return "/apps/" + application.getAppId() + "/web/";
    }

    public Class<?> getConfigurationClass() {
        return JspConfiguration.class;
    }

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        super.prepare(pageInstance, context);
        jspConfiguration = (JspConfiguration) getPageInstance().getConfiguration();
        jsp = jspConfiguration.getJsp();
        return null;
    }
}
