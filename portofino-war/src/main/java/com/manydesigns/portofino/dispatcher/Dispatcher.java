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
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Dispatcher {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static String KEY = Dispatcher.class.getName();

    public static final Logger logger =
            LoggerFactory.getLogger(Dispatcher.class);

    protected final Application application;

    public Dispatcher(Application application) {
        this.application = application;
    }

    public Dispatch createDispatch(HttpServletRequest request) {
        String originalPath =
                (String) request.getAttribute(StripesConstants.REQ_ATTR_INCLUDE_PATH);
        if(originalPath == null) { originalPath = request.getServletPath(); }

        List<SiteNodeInstance> siteNodePath = new ArrayList<SiteNodeInstance>();

        Model model = application.getModel();

        if (model == null) {
            logger.error("Model is null");
            throw new Error("Model is null");
        }

        SiteNode rootNode = model.getRootNode();
        List<SiteNode> nodeList = rootNode.getChildNodes();
        String[] fragments = StringUtils.split(originalPath, '/');
        List<String> fragmentList = Arrays.asList(fragments);
        ListIterator<String> iterator = fragmentList.listIterator();
        while (iterator.hasNext()) {
            String fragment = iterator.next();
            SiteNodeInstance foundNodeInstance = null;
            for (SiteNode node : nodeList) {
                if (fragment.equals(node.getId())) {
                    foundNodeInstance = createSiteNodeInstance(iterator, node);
                    break;
                }
            }
            if (foundNodeInstance == null) {
                return null;
            } else {
                siteNodePath.add(foundNodeInstance);
                nodeList = foundNodeInstance.getSiteNode().getChildNodes();
            }
        }

        if (siteNodePath.isEmpty()) {
            return null;
        }

        SiteNodeInstance siteNodeInstance =
                siteNodePath.get(siteNodePath.size() - 1);
        SiteNode siteNode = siteNodeInstance.getSiteNode();
        String rewrittenPath = siteNode.getUrl();
        if (rewrittenPath == null) {
            if (siteNode instanceof DocumentNode) {
                rewrittenPath = "/Document.action";
            } else if (siteNode instanceof PortletNode) {
                rewrittenPath = "/Portlet.action";
            } else if (siteNode instanceof FolderNode) {
                rewrittenPath = "/Index.action";
            } else if (siteNode instanceof UseCaseNode) {
                rewrittenPath = "/UseCase.action";
//                rewrittenPath = "/UseCase/" + originalPath + ".action";
            } else {
                throw new Error("Unrecognized node type");
            }
        }

        SiteNodeInstance[] siteNodeArray =
                new SiteNodeInstance[siteNodePath.size()];
        siteNodePath.toArray(siteNodeArray);

        return new Dispatch(request, originalPath, rewrittenPath, siteNodeArray);
    }

    private SiteNodeInstance createSiteNodeInstance(
            ListIterator<String> iterator, SiteNode foundNode) {
        SiteNodeInstance result;
        if (foundNode instanceof UseCaseNode) {
            String mode;
            String param;
            if (iterator.hasNext()) {
                String peek = iterator.next();
                if (UseCaseNode.MODE_NEW.equals(peek)) {
                    mode = UseCaseNode.MODE_NEW;
                    param = null;
                    if (iterator.hasNext()) {
                        return null;
                    }
//                } else if(UseCaseNode.MODE_EMBEDDED_SEARCH.equals(peek)) {
//                    mode = UseCaseNode.MODE_EMBEDDED_SEARCH;
//                    param = null;
//                    if (iterator.hasNext()) {
//                        return null;
//                    }
                } else {
                    mode = UseCaseNode.MODE_DETAIL;
                    param = peek;
                }
            } else {
                mode = UseCaseNode.MODE_SEARCH;
                param = null;
            }
            result = new UseCaseNodeInstance(application, (UseCaseNode) foundNode, mode, param);
        } else {
            result = new SiteNodeInstance(application, foundNode, null);
        }
        return result;
    }
}
