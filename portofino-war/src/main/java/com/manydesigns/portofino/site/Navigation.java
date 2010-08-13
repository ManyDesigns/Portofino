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

package com.manydesigns.portofino.site;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Util;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Navigation implements XhtmlFragment {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<SiteNode> rootNodes;
    protected Stack<SiteNode> path;
    protected List<SiteNode> foundPath;
    protected String currentUrl;

    //**************************************************************************
    // Constructiors
    //**************************************************************************

    public Navigation(List<SiteNode> rootNodes) {
        this.rootNodes = rootNodes;
        HttpServletRequest request =
                ElementsThreadLocals.getHttpServletRequest();
        currentUrl = Util.getAbsoluteUrl(request.getServletPath());
    }

    //**************************************************************************
    // XhtmlFragment implementation
    //**************************************************************************

    public void toXhtml(XhtmlBuffer xb) {
        path = new Stack<SiteNode>();

        boolean found = searchPath(rootNodes);
        print(rootNodes, xb);
    }

    private void print(List<SiteNode> nodes, XhtmlBuffer xb) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        xb.openElement("ul");
        List<SiteNode> expand = null;
        for (SiteNode current : nodes) {
            xb.openElement("li");
            String nodeUrl = current.getUrl();
            if (currentUrl.equals(nodeUrl)) {
                xb.addAttribute("class", "selected");
                expand = current.getChildNodes();
            } else if (foundPath != null && foundPath.contains(current)) {
                xb.addAttribute("class", "path");
                expand = current.getChildNodes();
            }
            xb.writeAnchor(nodeUrl, current.getTitle());
            xb.closeElement("li");
        }
        xb.closeElement("ul");
        if (expand != null && !expand.isEmpty()) {
            xb.writeHr();
            print(expand, xb);
        }
    }

    private boolean searchPath(List<SiteNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return false;
        }
        for (SiteNode current : nodes) {
            boolean found;
            path.push(current);
            String nodeUrl = current.getUrl();
            if (currentUrl.equals(nodeUrl)) {
                foundPath = new ArrayList<SiteNode>(path);
                found = true;
            } else {
                found = searchPath(current.getChildNodes());
            }
            path.pop();
            if (found) {
                return true;
            }
        }
        return false;
    }
}
