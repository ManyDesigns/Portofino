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

package com.manydesigns.portofino.navigation;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.model.site.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

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
    protected final String normalizedRequestUrl;
    protected final List<NavigationNode> foundPath;
    protected final List<NavigationNode> rootNodes;

    public static final Logger logger = LogUtil.getLogger(Navigation.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Navigation(Context context, String requestUrl) {
        this.context = context;
        this.requestUrl = requestUrl;
        int i = requestUrl.lastIndexOf("!");
        int j = requestUrl.lastIndexOf(".");
        if (i >= 0 && j > i) {
            normalizedRequestUrl =
                    requestUrl.substring(0, i) +
                            requestUrl.substring(j, requestUrl.length());
        } else {
            normalizedRequestUrl = requestUrl;
        }
        Stack<NavigationNode> stack = new Stack<NavigationNode>();
        foundPath = new ArrayList<NavigationNode>();
        rootNodes = new ArrayList<NavigationNode>();
        final List<SiteNode> rootChildNodes = context.getModel().getRootNode().getChildNodes();
        generateNavigationNodes(rootChildNodes, rootNodes);
        searchPath(rootNodes, stack);
    }

    protected void generateNavigationNodes(List<SiteNode> siteNodes,
                                           List<NavigationNode> navigationNodes) {
        for (SiteNode siteNode : siteNodes) {
            //se non sono autorizzato a vedere il nodo continuo
            //TODO riabilitare per i permessi
            /*User currentUser = context.getCurrentUser();
            List<String> groups = UserUtils.manageGroups(context);

            if (!siteNode.isAllowed(groups)){
                continue;
            }*/

            NavigationNode navigationNode;
            if (siteNode instanceof DocumentNode
                    || siteNode instanceof PortletNode
                    || siteNode instanceof UseCaseNode
                    || siteNode instanceof CustomNode) {
                navigationNode = new SimpleNavigationNode(siteNode);
                generateNavigationNodes(siteNode.getChildNodes(),
                        navigationNode.getChildNodes());
            }else if (siteNode instanceof FolderNode) {
                navigationNode = new FolderNavigationNode(siteNode);
                generateNavigationNodes(siteNode.getChildNodes(),
                        navigationNode.getChildNodes());
            } else if (siteNode instanceof CustomFolderNode) {
                CustomFolderNode node = (CustomFolderNode) siteNode;
                if("table-data".equals(node.getType())) {
                    navigationNode =
                        new TableDataNavigationNode(context, siteNode);
                } else if("table-design".equals(node.getType())) {
                    navigationNode =
                        new TableDesignNavigationNode(context, siteNode);
                } else {
                LogUtil.warningMF(logger,
                        "Unrecognized site node type: {0}", siteNode.getClass().getName());
                continue;
                }
            } else {
                LogUtil.warningMF(logger,
                        "Unrecognized site node type: {0}", siteNode.getClass().getName());
                continue;
            }
            navigationNodes.add(navigationNode);
        }
    }

    protected boolean searchPath(List<NavigationNode> nodes,
                                 Stack<NavigationNode> stack) {
        if (nodes == null || nodes.isEmpty()) {
            return false;
        }
        for (NavigationNode current : nodes) {
            boolean found;
            stack.push(current);
            String nodeUrl = current.getUrl();
            if (normalizedRequestUrl.equals(nodeUrl)) {
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
        print(rootNodes, xb);
    }

    private void print(List<NavigationNode> nodes, XhtmlBuffer xb) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        xb.openElement("ul");
        List<NavigationNode> expand = null;
        for (NavigationNode current : nodes) {
            xb.openElement("li");
            String nodeUrl = current.getUrl();
            if (normalizedRequestUrl.equals(nodeUrl)) {
                xb.addAttribute("class", "selected");
                expand = current.getChildNodes();
            } else if (foundPath != null && foundPath.contains(current)) {
                xb.addAttribute("class", "path");
                expand = current.getChildNodes();
            }
            xb.writeAnchor(nodeUrl, current.getTitle(), null, current.getDescription());
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

    public List<NavigationNode> getFoundPath() {
        return foundPath;
    }

    public NavigationNode getSelectedNavigationNode() {
        if (foundPath == null || foundPath.size() == 0) {
            return null;
        }
        return foundPath.get(foundPath.size() - 1);
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public List<NavigationNode> getRootNodes() {
        return rootNodes;
    }
}
