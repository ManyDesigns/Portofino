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

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Dispatch {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected final HttpServletRequest request;
    protected final String originalPath;
    protected final String rewrittenPath;
    protected final PageInstance[] pageInstancePath;
    protected final List<PageInstance> navigationPageInstances;

    public Dispatch(HttpServletRequest request,
                    String originalPath,
                    String rewrittenPath,
                    PageInstance[] pageInstancePath,
                    List<PageInstance> navigationPageInstances) {
        this.request = request;
        this.originalPath = originalPath;
        this.rewrittenPath = rewrittenPath;
        this.pageInstancePath = pageInstancePath;
        this.navigationPageInstances = navigationPageInstances;

        String pathUrl = getPathUrl();
        assert pathUrl.equals(originalPath);

    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public String getRewrittenPath() {
        return rewrittenPath;
    }

    public PageInstance[] getPageInstancePath() {
        return pageInstancePath;
    }

    public List<PageInstance> getNavigationPageInstances() {
        return navigationPageInstances;
    }

    public PageInstance getLastPageInstance() {
        return pageInstancePath[pageInstancePath.length - 1];
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public String getAbsoluteOriginalPath() {
        String contextPath = request.getContextPath();
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
        sb.append("/");
        boolean first = true;
        for (int i = 0; i < length; i++) {
            PageInstance current = pageInstancePath[i];
            String fragment = current.getUrlFragment();
            if (first) {
                first = false;
            } else {
                sb.append("/");
            }
            sb.append(fragment);
        }
        return sb.toString();
    }
}
