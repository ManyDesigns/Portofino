/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class DispatcherFilter implements Filter {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(DispatcherFilter.class);

    protected FilterConfig filterConfig;
    protected ServletContext servletContext;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        servletContext = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain
    ) throws IOException, ServletException {
        // cast to http type
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // invoke the dispatcher to create a dispatch
        Dispatcher dispatcher =
                (Dispatcher) servletContext.getAttribute(Dispatcher.KEY);
        Dispatch dispatch = dispatcher.createDispatch(httpRequest);
        if (dispatch == null) {
            // we can't handle this path. Let's do the normal filter chain
            chain.doFilter(request, response);
        } else {
            request.setAttribute(Dispatch.KEY, dispatch);

            // forward
            String rewrittenPath = dispatch.getRewrittenPath();
            logger.debug("Forwarding '{}' to '{}'",
                    dispatch.getServletPath(),
                    rewrittenPath);
            RequestDispatcher requestDispatcher =
                    servletContext.getRequestDispatcher(rewrittenPath);
            requestDispatcher.forward(request, response);
        }
    }

    public void destroy() {
        filterConfig = null;
        servletContext = null;
    }
}