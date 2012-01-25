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

package com.manydesigns.portofino.breadcrumbs;

import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.pages.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Breadcrumbs {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected final Dispatch dispatch;
    protected final List<BreadcrumbItem> items;

    public Breadcrumbs(Dispatch dispatch) {
        this(dispatch, dispatch.getPageInstancePath().length);
    }

    public Breadcrumbs(Dispatch dispatch, int upto) {
        this.dispatch = dispatch;
        items = new ArrayList<BreadcrumbItem>();

        StringBuilder sb = new StringBuilder();
        int start = dispatch.getClosestSubtreeRootIndex();
        sb.append(dispatch.getContextPath()).append(dispatch.getPathUrl(start));
        if(dispatch.getPageInstance(start).getParent() == null) {
            start++;
        }
        for (int i = start; i < upto; i++) {
            PageInstance current = dispatch.getPageInstancePath()[i];
            //Resolve references to treat Crud pages differently below
            //TODO current = current.dereference();
            sb.append("/");
            Page page = current.getPage();
            sb.append(current.getName());
            BreadcrumbItem item = new BreadcrumbItem(
                    sb.toString(), page.getTitle(),
                    page.getDescription());
            items.add(item);
            if(!current.getParameters().isEmpty()) {
                for(String param : current.getParameters()) {
                    sb.append("/").append(param);
                }
                String description = current.getDescription();
                String title = String.format("%s (%s)", description, page.getTitle());
                BreadcrumbItem item2 = new BreadcrumbItem(sb.toString(), description, title);
                items.add(item2);
            }
        }
    }

    public List<BreadcrumbItem> getItems() {
        return items;
    }

}
