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
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.model.pages.Page;
import com.manydesigns.portofino.util.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.FileFilter;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PageLogic {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static SelectionProvider createPagesSelectionProvider
            (Application application, File baseDir, File... excludes) {
        return createPagesSelectionProvider(application, baseDir, false, false, excludes);
    }

    public static SelectionProvider createPagesSelectionProvider
            (Application application, File baseDir, boolean includeRoot, boolean includeDetailChildren,
             File... excludes) {
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider("pages");
        if(includeRoot) {
            Page rootPage = application.getPage(baseDir);
            selectionProvider.appendRow("/" + baseDir.getName(), rootPage.getTitle() + " (top level)", true);
        }
        appendToPagesSelectionProvider(application, baseDir, null, selectionProvider, includeDetailChildren, excludes);
        return selectionProvider;
    }

    protected static void appendToPagesSelectionProvider
            (Application application, File baseDir, String breadcrumb,
             DefaultSelectionProvider selectionProvider, boolean includeDetailChildren, File... excludes) {
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
        for (File dir : baseDir.listFiles(filter)) {
            appendToPagesSelectionProvider
                    (application, baseDir, dir, breadcrumb, selectionProvider, includeDetailChildren, excludes);
        }
    }

    private static void appendToPagesSelectionProvider
            (Application application, File baseDir, File file, String breadcrumb,
             DefaultSelectionProvider selectionProvider, boolean includeDetailChildren, File... excludes) {
        if(ArrayUtils.contains(excludes, file)) {
            return;
        }
        if(PageInstance.DETAIL.equals(file.getName())) {
            if(includeDetailChildren) {
                breadcrumb += " (detail)"; //TODO I18n
                selectionProvider.appendRow
                    ("/" + baseDir.getName() + "/" + FileUtils.getRelativePath(baseDir, file), breadcrumb, true);
                appendToPagesSelectionProvider
                        (application, file, breadcrumb, selectionProvider, includeDetailChildren, excludes);
            }
        } else {
            Page page = application.getPage(file);
            if (breadcrumb == null) {
                breadcrumb = page.getTitle();
            } else {
                breadcrumb = String.format("%s > %s", breadcrumb, page.getTitle());
            }
            selectionProvider.appendRow
                    ("/" + baseDir.getName() + "/" + FileUtils.getRelativePath(baseDir, file), breadcrumb, true);
            appendToPagesSelectionProvider
                    (application, file, breadcrumb, selectionProvider, includeDetailChildren, excludes);
        }
    }

    /*public static String getPagePath(Page page) {
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
    }*/
}
