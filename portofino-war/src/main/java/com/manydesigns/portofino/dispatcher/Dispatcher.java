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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.site.*;
import net.sourceforge.stripes.controller.StripesConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Dispatcher {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static String KEY = Dispatcher.class.getName();

    public static final Logger logger =
            LoggerFactory.getLogger(Dispatcher.class);

    protected final Application application;

    public Dispatcher(Application application) {
        this.application = application;
    }

    public Dispatch createDispatch(HttpServletRequest request) {
        String originalPath = (String) request.getAttribute(
                StripesConstants.REQ_ATTR_INCLUDE_PATH);
        if (originalPath == null) {
            originalPath = request.getServletPath();
        }

        List<SiteNodeInstance> path = new ArrayList<SiteNodeInstance>();
        List<SiteNodeInstance> tree = new ArrayList<SiteNodeInstance>();

        Model model = application.getModel();

        if (model == null) {
            logger.error("Model is null");
            throw new Error("Model is null");
        }

        SiteNode rootNode = model.getRootNode();
        List<SiteNode> nodeList = rootNode.getChildNodes();
        String[] fragments = StringUtils.split(originalPath, '/');

        visitNodesInPath(path, tree, nodeList, fragments, 0);

        if (path.isEmpty()) {
            return null;
        }

        SiteNodeInstance siteNodeInstance =
                path.get(path.size() - 1);
        SiteNode siteNode = siteNodeInstance.getSiteNode();
        String rewrittenPath = siteNode.getUrl();
        if (rewrittenPath == null) {
            if (siteNode instanceof DocumentNode) {
                rewrittenPath = "/Document.action";
            } else if (siteNode instanceof PortletNode) {
                rewrittenPath = "/Portlet.action";
            } else if (siteNode instanceof FolderNode) {
                rewrittenPath = "/Index.action";
            } else if (siteNode instanceof CrudNode) {
                rewrittenPath = "/Crud.action";
//                rewrittenPath = "/Crud/" + originalPath + ".action";
            } else {
                throw new Error("Unrecognized node type");
            }
        }

        SiteNodeInstance[] siteNodeArray =
                new SiteNodeInstance[path.size()];
        path.toArray(siteNodeArray);

        return new Dispatch(request, originalPath, rewrittenPath, siteNodeArray, tree);
    }

    private void visitNodesInPath(List<SiteNodeInstance> path,
                                  List<SiteNodeInstance> tree,
                                  List<SiteNode> siteNodes,
                                  String[] fragments,
                                  int fragmentIndex) {
        if (fragmentIndex >= fragments.length) {
            logger.debug("Stopping recursion.");
            return;
        }

        String fragment = fragments[fragmentIndex];

        for (SiteNode siteNode : siteNodes) {
            // Wrap SiteNode in SiteNodeInstance
            SiteNodeInstance siteNodeInstance;
            if (fragment.equals(siteNode.getId())) {
                siteNodeInstance = visitNodeInPath(path, fragments, fragmentIndex, siteNode);
            } else {
                siteNodeInstance = visitNodeOutsidePath(siteNode);
            }
            tree.add(siteNodeInstance);
        }
    }

    private SiteNodeInstance visitNodeInPath(List<SiteNodeInstance> path,
                                 String[] fragments,
                                 int fragmentIndex,
                                 SiteNode siteNode) {
        SiteNodeInstance siteNodeInstance;
        int recursiveFragmentIndex = fragmentIndex + 1;
        if (siteNode instanceof CrudNode) {
            CrudNode crudNode = (CrudNode) siteNode;
            String mode;
            String param;
            if (fragmentIndex < fragments.length -1) {
                String peek = fragments[fragmentIndex + 1];
                if (CrudNode.MODE_NEW.equals(peek)) {
                    mode = CrudNode.MODE_NEW;
                    param = null;
                } else {
                    mode = CrudNode.MODE_DETAIL;
                    param = peek;
                    recursiveFragmentIndex = fragmentIndex + 2;
                }
            } else {
                mode = CrudNode.MODE_SEARCH;
                param = null;
            }
            siteNodeInstance = new CrudNodeInstance(
                    application, crudNode, mode, param);
        } else {
            siteNodeInstance =
                    new SiteNodeInstance(application, siteNode, null);
        }

        // add to path
        path.add(siteNodeInstance);

        // visit recursively
        visitNodesInPath(path, siteNodeInstance.getChildNodeInstances(),
                siteNodeInstance.getChildNodes(), fragments,
                recursiveFragmentIndex);

        return siteNodeInstance;
    }


    private SiteNodeInstance visitNodeOutsidePath(SiteNode siteNode) {
        SiteNodeInstance siteNodeInstance;
        if (siteNode instanceof CrudNode) {
            CrudNode crudNode = (CrudNode) siteNode;
            siteNodeInstance = new CrudNodeInstance(
                    application, crudNode, CrudNode.MODE_SEARCH, null);
        } else {
            siteNodeInstance =
                    new SiteNodeInstance(application, siteNode, null);
        }

        // visit recursively
        visitNodesOutsidePath(siteNodeInstance.getChildNodeInstances(),
                siteNodeInstance.getChildNodes());

        return siteNodeInstance;
    }

    private void visitNodesOutsidePath(List<SiteNodeInstance> tree,
                                       List<SiteNode> siteNodes) {
        for (SiteNode siteNode : siteNodes) {
            // Wrap SiteNode in SiteNodeInstance
            SiteNodeInstance siteNodeInstance = visitNodeOutsidePath(siteNode);
            tree.add(siteNodeInstance);
        }
    }
}
