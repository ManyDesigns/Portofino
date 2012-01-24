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
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.pages.ChildPage;
import com.manydesigns.portofino.pages.Layout;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.security.AccessLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Navigation {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Application application;
    protected final Dispatch dispatch;
    protected final List<String> groups;
    protected final boolean skipPermissions;
    protected NavigationItem rootNavigationItem;

    public static final Logger logger =
            LoggerFactory.getLogger(Navigation.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Navigation(
            Application application, Dispatch dispatch, List<String> groups,
            boolean skipPermissions) {
        this.application = application;
        this.dispatch = dispatch;
        this.groups = groups;
        this.skipPermissions = skipPermissions;

        //TODO gestire deploy sotto ROOT

        String contextPath = dispatch.getContextPath();
        int rootPageIndex = dispatch.getClosestSubtreeRootIndex();
        PageInstance[] pageInstances = dispatch.getPageInstancePath(rootPageIndex);
        String prefix = contextPath + dispatch.getPathUrl(rootPageIndex);
        PageInstance rootPageInstance = pageInstances[0];
        boolean selected = pageInstances.length == 1;
        boolean ghost = true;
        rootNavigationItem = new NavigationItem(
                rootPageInstance.getPage(), prefix, true, selected, ghost);
        print(pageInstances);
    }

    private void print(PageInstance[] pageInstances) {
        if (pageInstances == null || pageInstances.length == 0) {
            return;
        }
        boolean first = true;
        List<ChildPage> childPages = new ArrayList<ChildPage>();
        PageInstance last = null;
        NavigationItem currentNavigationItem = rootNavigationItem;
        for (int i = 0, pageInstancesLength = pageInstances.length; i < pageInstancesLength; i++) {
            PageInstance current = pageInstances[i];
            PageInstance next;
            if (i < pageInstancesLength -1) {
                next = pageInstances[i+1];
            } else {
                next = null;
            }

            if (!skipPermissions && !SecurityLogic.hasPermissions(current, groups, AccessLevel.VIEW)) {
                break;
            }

//            if(current.getParent() != null) {
//                //Root doesn't print anything for itself
//                printNavigationElement(path, xb, current, childPages, first);
//                path = path + "/" + current.getUrlFragment();
//                first = false;
//            }

            Layout layout = current.getLayout();
            if (layout != null) {
                childPages = layout.getChildPages();
            } else {
                childPages = new ArrayList<ChildPage>();
            }

            List<NavigationItem> currentChildNavigationItems =
                    currentNavigationItem.getChildNavigationItems();
            String prefix = currentNavigationItem.getPath();
            currentNavigationItem = null;
            for (ChildPage childPage : childPages) {
                File pageDir = current.getChildPageDirectory(childPage.getName());
                Page page = DispatcherLogic.getPage(pageDir);
                String path = prefix + "/" + childPage.getName();
                boolean inPath = false;
                boolean selected = false;
                if (next != null) {
                    if (next.getName().equals(childPage.getName())) {
                        inPath = true;
                        selected = (i == pageInstancesLength - 2);
                    }
                }
                NavigationItem childNavigationItem =
                        new NavigationItem(page, path, inPath, selected, false);
                currentChildNavigationItems.add(childNavigationItem);
                if (inPath) {
                    currentNavigationItem = childNavigationItem;
                }
            }
        }
/*
        boolean firstChild = true;
        if(!childPages.isEmpty()) {
            for (ChildPage p : childPages) {
                if(!p.isShowInNavigation()) {
                    continue;
                }
                assert last != null;
                File pageDir = last.getChildPageDirectory(p.getName());
                Page page = DispatcherLogic.getPage(pageDir);
                PageInstance pageInstance = new PageInstance(last, pageDir, application, page);
                if (!skipPermissions && !SecurityLogic.hasPermissions(pageInstance, groups, AccessLevel.VIEW)) {
                    continue;
                }

                if(firstChild) {
                    if(!first) { xb.writeHr(); }
                    xb.openElement("ul");
                    firstChild = false;
                }
                xb.openElement("li");
                String url = path + "/" + p.getName();
                xb.writeAnchor(url, page.getTitle(), null, page.getDescription());
                xb.closeElement("li");
            }
        }
        if(!firstChild) {
            xb.closeElement("ul");
        }
        */
    }

    private void printNavigationElement
            (String path, XhtmlBuffer xb, PageInstance current,
             List<ChildPage> siblings, boolean first) {
        boolean firstChild = true;
        if(!siblings.isEmpty()) {
            for (ChildPage p : siblings) {
                if(!p.isShowInNavigation()) {
                    continue;
                }
                Page page;
                PageInstance pageInstance;
                if(p.getName().equals(current.getDirectory().getName())) {
                    page = current.getPage();
                    pageInstance = current;
                } else {
                    File pageDir = current.getParent().getChildPageDirectory(p.getName());
                    page = DispatcherLogic.getPage(pageDir);
                    pageInstance = new PageInstance(current, pageDir, application, page);
                }
                if (!skipPermissions && !SecurityLogic.hasPermissions(pageInstance, groups, AccessLevel.VIEW)) {
                    continue;
                }

                if(firstChild) {
                    if(!first) { xb.writeHr(); }
                    xb.openElement("ul");
                    firstChild = false;
                }
                xb.openElement("li");
                String url;
                if(page == current.getPage()) {
                    if (isSelected(current)) {
                        xb.addAttribute("class", "selected");
                    } else {
                        xb.addAttribute("class", "path");
                    }
                    url = path + "/" + current.getDirectory().getName();
                } else {
                    url = path + "/" + p.getName();
                }
                xb.writeAnchor(url, page.getTitle(), null, page.getDescription());
                xb.closeElement("li");
            }
        }
        if(!firstChild) {
            xb.closeElement("ul");
        }
    }

    protected boolean isSelected(PageInstance pageInstance) {
        return pageInstance == dispatch.getLastPageInstance();
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

    public boolean isSkipPermissions() {
        return skipPermissions;
    }

    public NavigationItem getRootNavigationItem() {
        return rootNavigationItem;
    }
}
