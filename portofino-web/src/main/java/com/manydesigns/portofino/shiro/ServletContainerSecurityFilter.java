/*
* Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Add this to shiro.ini to manage user authentication with the servlet container.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ServletContainerSecurityFilter extends PathMatchingFilter {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(ServletContainerSecurityFilter.class);

    public ServletContainerSecurityFilter() {
        processPathConfig("/**", null);
    }

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        HttpServletRequest req = (HttpServletRequest) request;
        boolean shiroAuthenticated = subject.isAuthenticated();
        boolean containerAuthenticated = req.getUserPrincipal() != null;
        logger.debug("User authenticated by Shiro? {} User authenticated by the container? {}", shiroAuthenticated, containerAuthenticated);
        if (!shiroAuthenticated && containerAuthenticated) {
            logger.debug("User is known to the servlet container, but not to Shiro, attempting programmatic login");
            try {
                subject.login(new ServletContainerToken(req));
                logger.info("User {} login", req.getUserPrincipal().getName());
            } catch (AuthenticationException e) {
                logger.warn("Programmatic login failed", e);
            }
        } else if(shiroAuthenticated && !containerAuthenticated) {
            logger.debug("User is authenticated to Shiro, but not to the servlet container; logging out of Shiro.");
            String userName = ShiroUtils.getPrimaryPrincipal(SecurityUtils.getSubject()) + "";
            subject.logout();
            logger.info("User {} logout", userName);
            //TODO valutare effetti del distruggere o meno la sessione
            /*HttpSession session = req.getSession(false);
            if(session != null) {
                session.invalidate();
            }*/
        }
        return true;
    }
}
