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

package com.manydesigns.elements.servlet;

import com.manydesigns.elements.ElementsThreadLocals;
import ognl.OgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ElementsFilter implements Filter {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    public static final String REQUEST_OGNL_ATTRIBUTE = "request";
    public static final String SESSION_OGNL_ATTRIBUTE = "session";
    public static final String SERVLET_CONTEXT_OGNL_ATTRIBUTE = "servletContext";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected FilterConfig filterConfig;
    protected ServletContext servletContext;


    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger =
            LoggerFactory.getLogger(ElementsFilter.class);

    //--------------------------------------------------------------------------
    // Filter implementation
    //--------------------------------------------------------------------------

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        this.servletContext = filterConfig.getServletContext();
        logger.info("ElementsFilter initialized");
    }

    public void doFilter(ServletRequest req,
                         ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {

        if (req instanceof HttpServletRequest
                && res instanceof HttpServletResponse) {
            doHttpFilter((HttpServletRequest) req,
                    (HttpServletResponse)res,
                    filterChain);
        } else {
            filterChain.doFilter(req, res);
        }
    }

    public void destroy() {
        logger.info("ElementsFilter destroyed");
    }

    //--------------------------------------------------------------------------
    // methods
    //--------------------------------------------------------------------------

    protected void doHttpFilter(HttpServletRequest req,
                                HttpServletResponse res,
                                FilterChain filterChain)
            throws IOException, ServletException {
        ServletContext context = filterConfig.getServletContext();

        try {
            WebFramework webFramework = WebFramework.getWebFramework();
            req = webFramework.wrapRequest(req);

            HttpSession session = req.getSession(false);

            logger.debug("Setting up default OGNL context");
            ElementsThreadLocals.setupDefaultElementsContext();
            OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();

            logger.debug("Creating request attribute mapper");
            AttributeMap requestAttributeMap =
                    AttributeMap.createAttributeMap(req);
            ognlContext.put(REQUEST_OGNL_ATTRIBUTE, requestAttributeMap);

            logger.debug("Creating session attribute mapper");
            AttributeMap sessionAttributeMap =
                    AttributeMap.createAttributeMap(session);
            ognlContext.put(SESSION_OGNL_ATTRIBUTE, sessionAttributeMap);

            logger.debug("Creating servlet context attribute mapper");
            AttributeMap servletContextAttributeMap =
                    AttributeMap.createAttributeMap(servletContext);
            ognlContext.put(SERVLET_CONTEXT_OGNL_ATTRIBUTE,
                    servletContextAttributeMap);


            ElementsThreadLocals.setHttpServletRequest(req);
            ElementsThreadLocals.setHttpServletResponse(res);
            ElementsThreadLocals.setServletContext(context);

            filterChain.doFilter(req, res);
        } finally {
            ElementsThreadLocals.removeElementsContext();
        }
    }

}
