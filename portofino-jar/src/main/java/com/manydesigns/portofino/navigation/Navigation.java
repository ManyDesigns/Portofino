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
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.pages.AccessLevel;
import com.manydesigns.portofino.model.pages.ChildPage;
import com.manydesigns.portofino.model.pages.Layout;
import com.manydesigns.portofino.model.pages.Page;
import org.jetbrains.annotations.NotNull;
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
public class Navigation implements XhtmlFragment {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Application application;
    protected final Dispatch dispatch;
    protected final List<String> groups;
    protected final boolean skipPermissions;

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
    }

    //**************************************************************************
    // XhtmlFragment implementation
    //**************************************************************************

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        String contextPath = dispatch.getContextPath();
        int rootPageIndex = dispatch.getClosestSubtreeRootIndex();
        //PageInstance rootPageInstance = dispatch.getPageInstance(rootPageIndex);
        PageInstance[] pageInstances = dispatch.getPageInstancePath(rootPageIndex);
        /*if(rootPageInstance.getParent() == null) {
            pageInstances = new ArrayList<PageInstance>(); //TODO rootPageInstance.getChildPageInstances();
        } else {
            pageInstances = Collections.singletonList(rootPageInstance);
        }*/
        String prefix = contextPath + dispatch.getPathUrl(rootPageIndex);
        print(prefix, pageInstances, xb);
    }

    private void print(String path, PageInstance[] pageInstances, XhtmlBuffer xb) {
        if (pageInstances == null || pageInstances.length == 0) {
            return;
        }
        boolean first = true;
        List<ChildPage> siblings = new ArrayList<ChildPage>();
        PageInstance last = null;
        for (PageInstance current : pageInstances) {
            Page page = current.getPage();
            if (!skipPermissions && !SecurityLogic.hasPermissions(page.getPermissions(), groups, AccessLevel.VIEW)) {
                break;
            }

            /*if(!page.isShowInNavigation()) {
                continue;
            }*/

            if(current.getParent() != null) {
                //Root doesn't print anything for itself
                printNavigationElement(path, xb, current, siblings, first);
                path = path + "/" + current.getUrlFragment();
                first = false;
            }

            Layout layout = current.getLayout();
            if(layout != null) {
                siblings = layout.getChildPages();
            } else {
                siblings = new ArrayList<ChildPage>();
            }
            last = current;
        }

        boolean firstChild = true;
        for (ChildPage p : siblings) {
            if(!p.isShowInNavigation()) {
                continue;
            }
            if(firstChild) {
                if(!first) { xb.writeHr(); }
                xb.openElement("ul");
                firstChild = false;
            }
            xb.openElement("li");
            String url = path + "/" + p.getName();
            File pageDir = new File(last.getDirectory(), p.getName());
            Page page = application.getPage(pageDir);
            xb.writeAnchor(url, page.getTitle(), null, page.getDescription());
            xb.closeElement("li");
        }
        if(!firstChild) {
            xb.closeElement("ul");
        }
    }

    private void printNavigationElement
            (String path, XhtmlBuffer xb, PageInstance current, List<ChildPage> siblings, boolean first) {
        boolean firstChild = true;
        if(!siblings.isEmpty()) {
            for (ChildPage p : siblings) {
                if(!p.isShowInNavigation()) {
                    continue;
                }
                Page page;
                if(p.getName().equals(current.getDirectory().getName())) {
                    page = current.getPage();
                } else {
                    File pageDir = new File(current.getParent().getDirectory(), p.getName());
                    page = application.getPage(pageDir);
                }
                if (!skipPermissions && !SecurityLogic.hasPermissions(page.getPermissions(), groups, AccessLevel.VIEW)) {
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

    protected boolean isInPath(PageInstance pageInstance) {
        return findInPath(pageInstance) != null;
    }

    protected PageInstance findInPath(PageInstance pageInstance) {
        for (PageInstance current : dispatch.getPageInstancePath()) {
            if (pageInstance == current) {
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

    public boolean isSkipPermissions() {
        return skipPermissions;
    }
}
