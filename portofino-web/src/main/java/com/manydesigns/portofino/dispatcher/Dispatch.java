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

import com.manydesigns.portofino.pages.NavigationRoot;
import net.sourceforge.stripes.action.ActionBean;

import java.util.Arrays;

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
    protected final PageInstance[] pageInstancePath;

    public Dispatch(String contextPath,
                    String originalPath,
                    PageInstance... pageInstancePath) {
        this.contextPath = contextPath;
        this.originalPath = originalPath;
        this.pageInstancePath = pageInstancePath;
    }

    public String getContextPath() {
        return contextPath;
    }

    public PageInstance[] getPageInstancePath() {
        return pageInstancePath;
    }

    public PageInstance[] getPageInstancePath(int startIndex) {
        return Arrays.copyOfRange(pageInstancePath, startIndex, pageInstancePath.length);
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
            if (!first) {
                sb.append("/");
                sb.append(fragment);
            } else {
                first = false;
                // ignore fragment of root node
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

    public int getClosestSubtreeRootIndex() {
        PageInstance[] path = getPageInstancePath();
        for(int i = path.length - 1; i > 0; i--) {
            if(path[i].getPage().getActualNavigationRoot() != NavigationRoot.INHERIT) {
                return i;
            }
        }
        return 0;
    }

    public Class<? extends ActionBean> getActionBeanClass() {
        return getLastPageInstance().getActionClass();
    }
}
