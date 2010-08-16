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

import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import com.manydesigns.portofino.context.Context;

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

    protected final Context context;
    protected final String requestUrl;
    protected final List<SiteNode> foundPath;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Navigation(Context context, String requestUrl) {
        this.context = context;
        this.requestUrl = requestUrl;
        Stack<SiteNode> stack = new Stack<SiteNode>();
        foundPath = new ArrayList<SiteNode>();
        searchPath(context.getModel().getSiteNodes(), stack);
    }

    protected boolean searchPath(List<SiteNode> nodes, Stack<SiteNode> stack) {
        if (nodes == null || nodes.isEmpty()) {
            return false;
        }
        for (SiteNode current : nodes) {
            boolean found;
            stack.push(current);
            String nodeUrl = current.getUrl();
            if (requestUrl.equals(nodeUrl)) {
                foundPath.clear();
                foundPath.addAll(stack);
                found = true;
            } else {
                found = searchPath(current.getChildNodes(), stack);
            }
            stack.pop();
            if (found) {
                return true;
            }
        }
        return false;
    }

    //**************************************************************************
    // XhtmlFragment implementation
    //**************************************************************************

    public void toXhtml(XhtmlBuffer xb) {
        print(context.getModel().getSiteNodes(), xb);
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
            if (requestUrl.equals(nodeUrl)) {
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

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public Context getContext() {
        return context;
    }

    public List<SiteNode> getFoundPath() {
        return foundPath;
    }

    public SiteNode getSelectedSiteNode() {
        if (foundPath == null || foundPath.size() == 0) {
            return null;
        }
        return foundPath.get(foundPath.size() - 1);
    }

    public String getRequestUrl() {
        return requestUrl;
    }
}
