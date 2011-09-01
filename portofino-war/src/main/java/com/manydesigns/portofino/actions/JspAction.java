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

package com.manydesigns.portofino.actions;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.context.ServerInfo;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.model.pages.JspPage;
import net.sourceforge.stripes.action.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding("/jsp.action")
public class JspAction extends PortletAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected JspPage jspPage;

    protected Form form;

    public static final Logger logger =
            LoggerFactory.getLogger(JspAction.class);

    @Inject(ApplicationAttributes.SERVER_INFO)
    public ServerInfo serverInfo;

    @Before
    @Override
    public void prepare() {
        super.prepare();
        jspPage = (JspPage) getPageInstance().getPage();
    }

    @DefaultHandler
    public Resolution execute() {
        String fwd;
        if(StringUtils.isEmpty(jspPage.getJsp())) {
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

    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/jsp/configure.jsp");
    }

    public Resolution updateConfiguration() {
        synchronized (application) {
            prepareConfigurationForms();
            readPageConfigurationFromRequest();
            form.readFromRequest(context.getRequest());
            boolean valid = validatePageConfiguration();
            valid = form.validate() && valid;
            if(valid) {
                updatePageConfiguration();
                form.writeToObject(jspPage);
                saveModel();
                SessionMessages.addInfoMessage("Configuration updated successfully");
            }
            return cancel();
        }
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();

        SelectionProvider jspSelectionProvider =
                createJspSelectionProvider();

        form = new FormBuilder(JspPage.class)
                .configFields("jsp")
                .configSelectionProvider(jspSelectionProvider, "jsp")
                .build();
        form.readFromObject(jspPage);
    }

    private SelectionProvider createJspSelectionProvider() {
        File webAppDirFile = new File(serverInfo.getRealPath());
        List<String> values = new ArrayList<String>();
        List<String> labels = new ArrayList<String>();
        File[] files = webAppDirFile.listFiles();
        visitJspFiles(webAppDirFile, files, values, labels);
        String[] valuesArr = values.toArray(new String[values.size()]);
        String[] labelsArr = labels.toArray(new String[labels.size()]);
        return DefaultSelectionProvider.create("jsp", valuesArr, labelsArr);
    }

    private void visitJspFiles(File root, File[] files, List<String> values, List<String> labels) {
        for(File file : files) {
            if(file.isFile() && file.getName().endsWith(".jsp")) {
                String path = getRelativeFilePath(file, root);
                values.add(path);
                labels.add(path);
            } else if(file.isDirectory()) {
                visitJspFiles(root, file.listFiles(), values, labels);
            }
        }
    }

    private String getRelativeFilePath(File file, File root) {
        String path = "";
        File parent = file;
        do {
            path = "/" + parent.getName() + path;
            parent = parent.getParentFile();
        } while(parent != null && !parent.equals(root));
        return path;
    }

    public JspPage getJspPage() {
        return jspPage;
    }

    public Form getForm() {
        return form;
    }

    public String getTargetJsp() {
        String jsp = jspPage.getJsp();
        if(!jsp.startsWith("/")) {
            jsp = "/" + jsp;
        }
        ServletContext c = context.getServletContext();
        return jsp;
    }

    /*
   File webAppDirFile = new File(serverInfo.getRealPath());
       File skinDirFile = new File(webAppDirFile, "skins");
       File[] skinFiles = skinDirFile.listFiles(new FileFilter() {
           public boolean accept(File file) {
               return file.isDirectory();
           }
       });

       String[] skins = new String[skinFiles.length];
       for (int i = 0; i < skinFiles.length; i++) {
           File current = skinFiles[i];
           String skinName = current.getName();
           skins[i] = skinName;
           logger.debug("Found skin: {}", skinName);
       }

       SelectionProvider skinSelectionProvider =
               DefaultSelectionProvider.create("skins", skins, skins);

       return skinSelectionProvider;
    */

}
