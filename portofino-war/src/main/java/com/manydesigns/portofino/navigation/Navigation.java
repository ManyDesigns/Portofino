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

package com.manydesigns.portofino.navigation;

import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.SiteNodeInstance;
import com.manydesigns.portofino.model.site.SiteNode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Navigation implements XhtmlFragment {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Application application;
    protected final Dispatch dispatch;
    protected final List<String> groups;

    public static final Logger logger =
            LoggerFactory.getLogger(Navigation.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Navigation(Application application, Dispatch dispatch, List<String> groups) {
        this.application = application;
        this.dispatch = dispatch;
        this.groups = groups;
        //TODO gestire deploy sotto ROOT
    }

    //**************************************************************************
    // XhtmlFragment implementation
    //**************************************************************************

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        print("", dispatch.getNavigationNodeInstances(), xb, false);
    }

    private void print(String path, List<SiteNodeInstance> nodes, XhtmlBuffer xb, boolean recursive) {
        if (nodes == null) {
            return;
        }
        boolean first = true;
        SiteNodeInstance expand = null;
        for (SiteNodeInstance current : nodes) {
            // gestire permessi
            if(first) {
                if(recursive) { xb.writeHr(); }
                xb.openElement("ul");
                first = false;
            }
            xb.openElement("li");
            if (isSelected(current)) {
                xb.addAttribute("class", "selected");
                expand = current;
            } else if (isInPath(current)) {
                xb.addAttribute("class", "path");
                expand = current;
            }
            SiteNode siteNode = current.getSiteNode();
            String url = path + "/" + siteNode.getId();
            xb.writeAnchor(url, siteNode.getTitle(), null, siteNode.getDescription());
            xb.closeElement("li");
        }
        if(!first) {
            xb.closeElement("ul");
        }
        if (expand != null) {
            path = path + "/" + expand.getUrlFragment();
            print(path, expand.getChildNodeInstances(), xb, true);
        }
    }

    protected boolean isSelected(SiteNodeInstance siteNodeInstance) {
        return siteNodeInstance == dispatch.getLastSiteNodeInstance();
    }

    protected boolean isInPath(SiteNodeInstance siteNodeInstance) {
        return findInPath(siteNodeInstance) != null;
    }

    protected SiteNodeInstance findInPath(SiteNodeInstance siteNodeInstance) {
        for (SiteNodeInstance current : dispatch.getSiteNodeInstancePath()) {
            if (siteNodeInstance == current) {
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

    public Dispatch getDispatch() {
        return dispatch;
    }

    public List<String> getGroups() {
        return groups;
    }
}
