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

import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.model.pages.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla - alessio.stalla@manydesigns.com
*/
public class PageInstance {

    protected final Application application;
    protected final Page page;
    protected final String mode;
    protected final List<PageInstance> childPageInstances;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(PageInstance.class);

    public PageInstance(Application application, Page page, String mode) {
        this.application = application;
        this.page = page;
        this.mode = mode;
        childPageInstances = new ArrayList<PageInstance>();
    }

    public Page getPage() {
        return page;
    }

    public String getMode() {
        return mode;
    }

    public Application getApplication() {
        return application;
    }

    public boolean realize() {
        return true;
    }

    public PageInstance dereference() {
        return this;
    }

    //**************************************************************************
    // Utility Methods
    //**************************************************************************

    public PageInstance findChildPageByFragment(String fragment) {
        for(PageInstance page : getChildPageInstances()) {
            if(fragment.equals(page.getPage().getFragment())) {
                return page;
            }
        }
        logger.debug("Child page not found: {}", fragment);
        return null;
    }

    public String getUrlFragment() {
        return page.getFragment();
    }

    public List<PageInstance> getChildPageInstances() {
        return childPageInstances;
    }

    public List<Page> getChildPages() {
        return page.getChildPages();
    }

    public String getLayoutContainer() {
        return page.getLayoutContainer();
    }

    public void setLayoutContainer(String layoutContainer) {
        page.setLayoutContainer(layoutContainer);
    }

    public int getLayoutOrder() {
        return page.getActualLayoutOrder();
    }

    public void setLayoutOrder(int order) {
        page.setLayoutOrder(Integer.toString(order));
    }

    public void addChild(Page page) {
        this.page.addChild(page);
    }

    public boolean removeChild(Page page) {
        return this.page.removeChild(page);
    }
}
