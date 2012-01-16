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

package com.manydesigns.portofino.logic;

import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.portofino.actions.crud.configuration.CrudPage;
import com.manydesigns.portofino.model.pages.Page;
import com.manydesigns.portofino.model.pages.RootPage;
import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PageLogic {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static SelectionProvider createPagesSelectionProvider(RootPage rootPage, Page... excludes) {
        return createPagesSelectionProvider(rootPage, false, false, excludes);
    }

    public static SelectionProvider createPagesSelectionProvider
            (RootPage rootPage, boolean includeRoot, boolean includeDetailChildren, Page... excludes) {
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider("pages");
        if(includeRoot) {
            selectionProvider.appendRow(rootPage.getId(), rootPage.getTitle() + " (top level)", true);
        }
        for (Page page : rootPage.getChildPages()) {
            appendToPagesSelectionProvider(page, null, selectionProvider, includeDetailChildren, excludes);
        }

        return selectionProvider;
    }

    private static void appendToPagesSelectionProvider(Page page, String breadcrumb,
                                                       DefaultSelectionProvider selectionProvider,
                                                       boolean includeDetailChildren, Page... excludes) {
        if(ArrayUtils.contains(excludes, page)) {
            return;
        }
        String pageBreadcrumb;
        if (breadcrumb == null) {
            pageBreadcrumb = page.getTitle();
        } else {
            pageBreadcrumb = String.format("%s > %s", breadcrumb, page.getTitle());
        }
        selectionProvider.appendRow(page.getId(), pageBreadcrumb, true);
        List<Page> children = page.getChildPages();
        for (Page subPage : children) {
            appendToPagesSelectionProvider(subPage, pageBreadcrumb, selectionProvider, includeDetailChildren, excludes);
        }
        if(page instanceof CrudPage && includeDetailChildren) {
            pageBreadcrumb += " (detail)";
            selectionProvider.appendRow(page.getId() + "-detail", pageBreadcrumb, true);
            for (Page subPage : ((CrudPage) page).getDetailChildPages()) {
               appendToPagesSelectionProvider(subPage, pageBreadcrumb, selectionProvider, includeDetailChildren, excludes);
            }
        }
    }

    public static String getPagePath(Page page) {
        if(page instanceof RootPage) {
            return "";
        } else {
            return getPagePath(page.getParent()) + "/" + page.getFragment();
        }
    }

    public static Page getLandingPage(RootPage rootPage) {
        String landingPageId = rootPage.getLandingPage();
        if (landingPageId == null) {
            return rootPage.getChildPages().get(0);
        } else {
            return rootPage.findDescendantPageById(landingPageId);
        }
    }

    public static boolean isLandingPage(RootPage root, Page page) {
        return page.equals(getLandingPage(root));
    }
}
