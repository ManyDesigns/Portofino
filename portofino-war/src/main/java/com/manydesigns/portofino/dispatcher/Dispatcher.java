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

import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.site.DocumentNode;
import com.manydesigns.portofino.model.site.PortletNode;
import com.manydesigns.portofino.model.site.SiteNode;
import com.manydesigns.portofino.model.site.UseCaseNode;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Dispatcher {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static String KEY = Dispatcher.class.getName();


    protected final Context context;

    public Dispatcher(Context context) {
        this.context = context;
    }

    public Dispatch createDispatch(HttpServletRequest request) {
        String originalPath = request.getServletPath();

        List<SiteNode> siteNodePath = new ArrayList<SiteNode>();

        Model model = context.getModel();
        SiteNode rootNode = model.getRootNode();
        List<SiteNode> nodeList = rootNode.getChildNodes();
        String[] fragments = StringUtils.split(originalPath, '/');
        for (String fragment : fragments) {
            SiteNode foundNode = null;
            for (SiteNode node : nodeList) {
                if (fragment.equals(node.getId())) {
                    foundNode = node;
                    break;
                }
            }
            if (foundNode == null) {
                return null;
            } else {
                siteNodePath.add(foundNode);
                nodeList = foundNode.getChildNodes();
            }
        }

        if (siteNodePath.isEmpty()) {
            return null;
        }

        SiteNode siteNode = siteNodePath.get(siteNodePath.size()-1);
        String rewrittenPath;
        if (siteNode instanceof DocumentNode) {
            rewrittenPath = "/Document.action";
        } else if (siteNode instanceof PortletNode) {
            rewrittenPath = "/Portlet.action";
        } else if (siteNode instanceof UseCaseNode) {
            rewrittenPath = "/UseCase.action";
        } else {
            throw new Error("Unrecognized node type");
        }

        SiteNode[] siteNodeArray = new SiteNode[siteNodePath.size()];
        siteNodePath.toArray(siteNodeArray);

        return new Dispatch(originalPath, rewrittenPath, siteNodeArray);
    }
}
