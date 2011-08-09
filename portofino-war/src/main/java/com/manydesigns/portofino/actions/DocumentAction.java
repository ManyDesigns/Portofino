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

import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.annotations.InjectServerInfo;
import com.manydesigns.portofino.context.ServerInfo;
import com.manydesigns.portofino.model.site.DocumentNode;
import net.sourceforge.stripes.action.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@UrlBinding("/document.action")
public class DocumentAction extends PortletAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";
    public static final String CONTENT_ENCODING = "UTF-8";

    public String title;
    public String content;

    //**************************************************************************
    // Injections
    //**************************************************************************

    @InjectServerInfo
    public ServerInfo serverInfo;

    public DocumentNode documentNode;

    public static final Logger logger =
            LoggerFactory.getLogger(DocumentAction.class);

    //**************************************************************************
    // Setup
    //**************************************************************************

    @Before
    public void prepare() {
        documentNode = (DocumentNode) siteNodeInstance.getSiteNode();
    }

    //**************************************************************************
    // Handlers
    //**************************************************************************


    @DefaultHandler
    public Resolution execute() throws IOException {
        loadContent();
        return forwardToPortletPage("/layouts/document/read.jsp");
    }

    protected void loadContent() throws IOException {
        String fileName = documentNode.getFileName();
        File file = serverInfo.getWebAppFile(fileName);
        content = FileUtils.readFileToString(file, CONTENT_ENCODING);
    }

    protected void saveContent() throws IOException {
        String fileName = documentNode.getFileName();
        File file = serverInfo.getWebAppFile(fileName);
        FileUtils.writeStringToFile(file, content, CONTENT_ENCODING);
    }

    public Resolution configure() throws IOException {
        title = documentNode.getTitle();
        loadContent();
        return new ForwardResolution("/layouts/document/configure.jsp");
    }

    public Resolution updateConfiguration() throws IOException {
        synchronized (application) {
            title = StringUtils.trimToNull(title);
            boolean valid = true;
            if (title == null) {
                SessionMessages.addErrorMessage("Title cannot be empty");
                valid = false;
            }
            if (valid) {
                documentNode.setTitle(title);
                saveModel();
                saveContent();
                SessionMessages.addInfoMessage("Configuration updated successfully");
                return cancel();
            } else {
                return new ForwardResolution("/layouts/document/configure.jsp");
            }
        }

    }
    //**************************************************************************
    // Getters/setters
    //**************************************************************************


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DocumentNode getDocumentNode() {
        return documentNode;
    }
}
