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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.SiteNodeInstance;
import com.manydesigns.portofino.dispatcher.UseCaseNodeInstance;
import com.manydesigns.portofino.model.site.SiteNode;
import com.manydesigns.portofino.model.site.UseCaseNode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

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

    protected final Application application;
    protected final Dispatch dispatch;
    protected final SiteNodeInstance[] siteNodeInstancePath;
    protected final List<NavigationNode> rootNodes;
    protected final List<String> groups;

    public static final Logger logger =
            LoggerFactory.getLogger(Navigation.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Navigation(Application application, Dispatch dispatch, List<String> groups) {
        this.application = application;
        this.dispatch = dispatch;
        if(dispatch != null) {
            siteNodeInstancePath = dispatch.getSiteNodeInstancePath();
        } else {
            siteNodeInstancePath = null;
        }
        this.groups = groups;
        rootNodes = new ArrayList<NavigationNode>();
        final List<SiteNode> rootChildNodes =
                application.getModel().getRootNode().getChildNodes();
        HttpServletRequest req =
                ElementsThreadLocals.getHttpServletRequest();
        //TODO gestire deploy sotto ROOT
        generateNavigationNodes(rootChildNodes, rootNodes, req.getContextPath(), true);
    }

    protected void generateNavigationNodes(List<SiteNode> siteNodes,
                                           List<NavigationNode> navigationNodes,
                                           String prefixUrl, boolean useCaseEnabled) {
        for (SiteNode siteNode : siteNodes) {
            String url = String.format("%s/%s", prefixUrl, siteNode.getId());
            String title = siteNode.getTitle();
            String description = siteNode.getDescription();
            boolean allowed = siteNode.isAllowed(groups);

            boolean ownEnabled;
            boolean childUseCaseEnabled;
            String childUrl;

            if (siteNode instanceof UseCaseNode) {
                ownEnabled = useCaseEnabled;
                SiteNodeInstance siteNodeInstance = findInPath(siteNode);
                if (siteNodeInstance instanceof UseCaseNodeInstance) {
                    String mode = siteNodeInstance.getMode();
                    if (UseCaseNode.MODE_DETAIL.equals(mode)) {
                        childUseCaseEnabled = useCaseEnabled;
                        childUrl = url + "/" + ((UseCaseNodeInstance) siteNodeInstance).getPk();
                    } else {
                        childUseCaseEnabled = false;
                        childUrl = url;
                    }
                } else {
                    childUseCaseEnabled = false;
                    childUrl = url;
                }
            } else {
                ownEnabled = true;
                childUseCaseEnabled = useCaseEnabled;
                childUrl = url;
            }

            NavigationNode navigationNode =
                    new NavigationNode(siteNode, url, title, description,
                            allowed, ownEnabled);
            generateNavigationNodes(siteNode.getChildNodes(),
                    navigationNode.getChildNodes(), childUrl, childUseCaseEnabled);
            navigationNodes.add(navigationNode);
        }
    }

    //**************************************************************************
    // XhtmlFragment implementation
    //**************************************************************************

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        print(rootNodes, xb, false);
    }

    private void print(List<NavigationNode> nodes, XhtmlBuffer xb, boolean recursive) {
        if (nodes == null) {
            return;
        }
        boolean first = true;
        List<NavigationNode> expand = null;
        for (NavigationNode current : nodes) {
            if(!current.isAllowed() || !current.isEnabled()){
                continue;
            }
            if(first) {
                if(recursive) { xb.writeHr(); }
                xb.openElement("ul");
                first = false;
            }
            xb.openElement("li");
            SiteNode siteNode = current.getSiteNode();
            if (isSelected(siteNode)) {
                xb.addAttribute("class", "selected");
                expand = current.getChildNodes();
            } else if (isInPath(siteNode)) {
                xb.addAttribute("class", "path");
                expand = current.getChildNodes();
            }
            xb.writeAnchor(current.getUrl(), current.getTitle(), null, current.getDescription());
            xb.closeElement("li");
        }
        if(!first) {
            xb.closeElement("ul");
        }
        if (expand != null) {
            print(expand, xb, true);
        }
    }

    protected boolean isSelected(SiteNode siteNode) {
        if(siteNodeInstancePath == null) { return false; }
        SiteNodeInstance last =
                siteNodeInstancePath[siteNodeInstancePath.length - 1];
        return siteNode == last.getSiteNode();
    }

    protected boolean isInPath(SiteNode siteNode) {
        return findInPath(siteNode) != null;
    }

    protected SiteNodeInstance findInPath(SiteNode siteNode) {
        if(siteNodeInstancePath == null) { return null; }
        for (SiteNodeInstance current : siteNodeInstancePath) {
            if (siteNode == current.getSiteNode()) {
                return current;
            }
        }
        return null;
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public Application getApplication() {
        return application;
    }

    public List<NavigationNode> getRootNodes() {
        return rootNodes;
    }
}
