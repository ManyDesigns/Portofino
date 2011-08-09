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

import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.Dispatcher;
import com.manydesigns.portofino.dispatcher.SiteNodeInstance;
import com.manydesigns.portofino.model.site.SiteNode;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding("/pages/{path}")
public class PagesAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public String path;
    public final MultiMap portlets = new MultiHashMap();

    public Dispatcher dispatcher;
    public Dispatch dispatch;

    public final static Logger logger =
            LoggerFactory.getLogger(PagesAction.class);


    @DefaultHandler
    public Resolution execute() throws Exception {
        logger.debug("Invoking the dispatcher to create a dispatch");
        String orginalPath = "/" + path;
        ServletContext servletContext = context.getServletContext();
        dispatcher = (Dispatcher) servletContext.getAttribute(
                        ApplicationAttributes.DISPATCHER);
        dispatch = dispatcher.createDispatch(orginalPath);
        if (dispatch == null) {
            throw new Exception("Page not mapped: " + path);
        }

        setupPortlets();
        return new ForwardResolution("/layouts/portlet-page.jsp");
    }

    protected void setupPortlets() {
        SiteNodeInstance siteNodeInstance = dispatch.getLastSiteNodeInstance();
        String url = "/portlets/" + path;
        PortletInstance myPortletInstance = new PortletInstance("p", siteNodeInstance.getLayoutOrder(), url);
        portlets.put(siteNodeInstance.getLayoutContainer(), myPortletInstance);
        for(SiteNode node : siteNodeInstance.getChildNodes()) {
            if(node.getLayoutContainerInParent() != null) {
                String subUrl = "/portlets/" + path + node.getId();
                PortletInstance portletInstance =
                        new PortletInstance(
                                "c" + node.getId(),
                                node.getActualLayoutOrderInParent(),
                                subUrl);
                portlets.put(node.getLayoutContainerInParent(), portletInstance);
            }
        }
        for(Object entryObj : portlets.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            List portletContainer = (List) entry.getValue();
            Collections.sort(portletContainer);
        }
    }



    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public MultiMap getPortlets() {
        return portlets;
    }
}
