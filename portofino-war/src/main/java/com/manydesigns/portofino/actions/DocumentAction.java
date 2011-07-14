/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
@UrlBinding("/Document.action")
public class DocumentAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public String content;

    //**************************************************************************
    // Injections
    //**************************************************************************

    @InjectSiteNodeInstance
    public SiteNodeInstance siteNodeInstance;

    public static final Logger logger =
            LoggerFactory.getLogger(DocumentAction.class);

    @DefaultHandler
    public Resolution execute() throws Exception {
        DocumentNode node = (DocumentNode) siteNodeInstance.getSiteNode();
        String fileName = node.getFileName();
        try {
            content = convertStreamToString(
                    context.getServletContext().getResourceAsStream(fileName));
        } catch (IOException e) {
            logger.warn(MessageFormat.format("IOException opening file {0}",
                    fileName));
        }
        return new ForwardResolution("/skins/default/document.jsp");
    }


    private String convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            }
            finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
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
}
