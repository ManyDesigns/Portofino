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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.model.pages.Page;

import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PageReferenceInstance extends PageInstance {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected final PageInstance wrappedPageInstance;

    public PageReferenceInstance(Application application, Page page, String mode, PageInstance wrappedPageInstance) {
        super(application, page, mode);
        this.wrappedPageInstance = wrappedPageInstance;
    }

    public PageInstance getWrappedPageInstance() {
        return wrappedPageInstance;
    }

    //**************************************************************************
    // Delegate Methods (to wrappedPageInstance)
    //**************************************************************************

    @Override
    public PageInstance dereference() {
        return wrappedPageInstance.dereference();
    }

    @Override
    public void realize() {
        wrappedPageInstance.realize();
    }

    @Override
    public PageInstance findChildPageByFragment(String fragment) {
        return wrappedPageInstance.findChildPageByFragment(fragment);
    }

    @Override
    public List<PageInstance> getChildPageInstances() {
        return wrappedPageInstance.getChildPageInstances();
    }

    @Override
    public List<Page> getChildPages() {
        return wrappedPageInstance.getChildPages();
    }

    @Override
    public String getLayoutContainer() {
        return wrappedPageInstance.getLayoutContainer();
    }

    @Override
    public void setLayoutContainer(String layoutContainer) {
        wrappedPageInstance.setLayoutContainer(layoutContainer);
    }

    @Override
    public int getLayoutOrder() {
        return wrappedPageInstance.getLayoutOrder();
    }

    @Override
    public void setLayoutOrder(int order) {
        wrappedPageInstance.setLayoutOrder(order);
    }

    @Override
    public void addChild(Page page) {
        wrappedPageInstance.addChild(page);
    }

    @Override
    public boolean removeChild(Page page) {
        return wrappedPageInstance.removeChild(page);
    }

    @Override
    public String getUrlFragment() {
        return wrappedPageInstance.getUrlFragment();
    }

    @Override
    public Application getApplication() {
        return wrappedPageInstance.getApplication();
    }

    @Override
    public String getMode() {
        return wrappedPageInstance.getMode();
    }
}
