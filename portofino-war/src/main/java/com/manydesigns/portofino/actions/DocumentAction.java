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

import com.manydesigns.portofino.annotations.InjectSiteNodeInstance;
import com.manydesigns.portofino.dispatcher.SiteNodeInstance;
import com.manydesigns.portofino.model.site.DocumentNode;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

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

    public String content;

    //**************************************************************************
    // Injections
    //**************************************************************************

    @InjectSiteNodeInstance
    public SiteNodeInstance siteNodeInstance;

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
    public Resolution execute() throws Exception {
        String fileName = documentNode.getFileName();
        InputStream is = null;
        try {
            is = context.getServletContext().getResourceAsStream(fileName);
            if (is != null) {
                content = IOUtils.toString(is, "UTF-8");
            } else {
                logger.warn("Cannot get stream for file: {}", fileName);
                content = "There was a problem loading this content.";
            }
        } finally {
            IOUtils.closeQuietly(is);
        }
        return forwardToPortletPage("/layouts/document.jsp");
    }


    //**************************************************************************
    // Getters/setters
    //**************************************************************************


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
