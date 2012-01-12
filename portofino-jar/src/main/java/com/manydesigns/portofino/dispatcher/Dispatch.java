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

import net.sourceforge.stripes.action.ActionBean;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Dispatch {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected final String contextPath;
    protected final String originalPath;
    protected final Class<? extends ActionBean> actionBeanClass;
    protected final PageInstance[] pageInstancePath;

    public Dispatch(String contextPath,
                    String originalPath,
                    Class<? extends ActionBean> actionBeanClass,
                    PageInstance... pageInstancePath) {
        this.contextPath = contextPath;
        this.originalPath = originalPath;
        this.actionBeanClass = actionBeanClass;
        this.pageInstancePath = pageInstancePath;
    }

    public String getContextPath() {
        return contextPath;
    }

    public Class<? extends ActionBean> getActionBeanClass() {
        return actionBeanClass;
    }

    public PageInstance[] getPageInstancePath() {
        return pageInstancePath;
    }

    public PageInstance getRootPageInstance() {
        return pageInstancePath[0];
    }

    public PageInstance getLastPageInstance() {
        return pageInstancePath[pageInstancePath.length - 1];
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public String getAbsoluteOriginalPath() {
        if ("/".equals(contextPath)) {
            return getOriginalPath();
        } else {
            return contextPath + getOriginalPath();
        }
    }

    public String getPathUrl() {
        return getPathUrl(pageInstancePath.length);
    }

    public String getParentPathUrl() {
        return getPathUrl(pageInstancePath.length - 1);
    }

    public String getPathUrl(int length) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < length; i++) {
            PageInstance current = pageInstancePath[i];
            String fragment = current.getUrlFragment();
            if (first) {
                first = false;
                // ignore fragment of root node
            } else {
                sb.append("/");
                sb.append(fragment);
            }
        }
        return sb.toString();
    }

    public PageInstance getPageInstance(int index) {
        if(index >= 0) {
            return getPageInstancePath()[index];
        } else {
            return getPageInstancePath()[getPageInstancePath().length + index];
        }
    }

    public PageInstance getParentPageInstance() {
        return getPageInstance(-2);
    }
}
