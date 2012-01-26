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

import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.pages.*;
import com.manydesigns.portofino.security.AccessLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
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

        buildTree();
    }

    private void buildTree() {
        String contextPath = dispatch.getContextPath();
        int rootPageIndex = dispatch.getClosestSubtreeRootIndex();
        PageInstance[] pageInstances = dispatch.getPageInstancePath(rootPageIndex);
        if (pageInstances == null || pageInstances.length == 0) {
            return;
        }
        PageInstance rootPageInstance = pageInstances[0];
        String prefix = contextPath;
        if(rootPageIndex > 0) {
            prefix += rootPageInstance.getParent().getPath() + "/" + rootPageInstance.getName();
        }
        boolean rootSelected = pageInstances.length == 1;
        Page rootPage = rootPageInstance.getPage();
        boolean rootGhost = rootPage.getActualNavigationRoot() == NavigationRoot.GHOST_ROOT;
        rootNavigationItem = new NavigationItem(
                rootPage, prefix, true, rootSelected, rootGhost);
        LinkedList<Page> pages = new LinkedList<Page>();
        for(PageInstance pageInstance : pageInstances) {
            pages.add(pageInstance.getPage());
        }
        List<ChildPage> childPages;
        NavigationItem currentNavigationItem = rootNavigationItem;
        for (int i = 0, pageInstancesLength = pageInstances.length; i < pageInstancesLength; i++) {
            PageInstance current = pageInstances[i];
            PageInstance next;
            if (i < pageInstancesLength -1) {
                next = pageInstances[i+1];
            } else {
                next = null;
            }

            Layout layout = current.getLayout();
            if (layout != null) {
                childPages = layout.getChildPages();
            } else {
                childPages = new ArrayList<ChildPage>();
            }

            List<NavigationItem> currentChildNavigationItems =
                    currentNavigationItem.getChildNavigationItems();
            prefix = currentNavigationItem.getPath() + "/";
            for(String param : current.getParameters()) {
                prefix += param + "/";
            }
            currentNavigationItem = null;
            for (ChildPage childPage : childPages) {
                File pageDir = current.getChildPageDirectory(childPage.getName());
                Page page;
                try {
                    page = DispatcherLogic.getPage(pageDir);
                } catch (Exception e) {
                    logger.warn("Nonexisting child page: " + pageDir, e);
                    continue;
                }
                String path = prefix + childPage.getName();
                boolean inPath = false;
                boolean selected = false;
                if (next != null) {
                    if (next.getName().equals(childPage.getName())) {
                        inPath = true;
                        selected = (i == pageInstancesLength - 2);
                    }
                }
                pages.add(page);
                if (!skipPermissions) {
                    Permissions permissions = SecurityLogic.calculateActualPermissions(pages);
                    if(!SecurityLogic.hasPermissions(permissions, groups, AccessLevel.VIEW)) {
                        pages.removeLast();
                        continue;
                    }
                }
                pages.removeLast();
                if(!childPage.isShowInNavigation() && !inPath) {
                    continue;
                }
                NavigationItem childNavigationItem =
                        new NavigationItem(page, path, inPath, selected, false);
                currentChildNavigationItems.add(childNavigationItem);
                if (inPath) {
                    currentNavigationItem = childNavigationItem;
                }
            }
            if(currentNavigationItem == null && next != null) {
                boolean selected = (i == pageInstancesLength - 2);
                String path = prefix + next.getName();
                currentNavigationItem =
                        new NavigationItem(next.getPage(), path, true, selected, false);
                currentChildNavigationItems.add(currentNavigationItem);
            }

            if(next != null) {
                pages.add(next.getPage());
            }
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
