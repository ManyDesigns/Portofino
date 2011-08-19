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
import com.manydesigns.portofino.model.pages.Page;
import com.manydesigns.portofino.model.pages.RootPage;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
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
        return createPagesSelectionProvider(rootPage, false);
    }

    public static SelectionProvider createPagesSelectionProvider(RootPage rootPage, boolean includeRoot, Page... excludes) {
        List<String> valuesList = new ArrayList<String>();
        List<String> labelsList = new ArrayList<String>();

        if(includeRoot) {
            valuesList.add(rootPage.getId());
            labelsList.add(rootPage.getTitle() + " (top level)");
        }
        for (Page page : rootPage.getChildPages()) {
            recursive(page, null, valuesList, labelsList, excludes);
        }

        String[] values = new String[valuesList.size()];
        valuesList.toArray(values);
        String[] labels = new String[labelsList.size()];
        labelsList.toArray(labels);
        return DefaultSelectionProvider.create("pages", values, labels);
    }

    private static void recursive(Page page, String breadcrumb,
                           List<String> valuesList, List<String> labelsList,
                           Page... excludes) {
        if(ArrayUtils.contains(excludes, page)) {
            return;
        }
        String pageBreadcrumb;
        if (breadcrumb == null) {
            pageBreadcrumb = page.getTitle();
        } else {
            pageBreadcrumb = String.format("%s > %s", breadcrumb, page.getTitle());
        }
        valuesList.add(page.getId());
        labelsList.add(pageBreadcrumb);
        for (Page subPage : page.getChildPages()) {
            recursive(subPage, pageBreadcrumb, valuesList, labelsList, excludes);
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
