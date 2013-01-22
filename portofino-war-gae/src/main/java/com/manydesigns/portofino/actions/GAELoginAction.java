/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.shiro.ShiroUtils;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding("/actions/user/login.gae")
public class GAELoginAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(GAELoginAction.class);

    protected String returnUrl;

    public Resolution login() {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            logger.debug("User not authenticated, redirecting to GAE login URL");
            UserService userService = UserServiceFactory.getUserService();
            String loginUrl = userService.createLoginURL(returnUrl);
            return new RedirectResolution(loginUrl);
        } else {
            return new RedirectResolution(returnUrl);
        }
    }

    public Resolution logout() {
        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated()) {
            logger.debug("User not authenticated, redirecting to GAE logout URL");
            UserService userService = UserServiceFactory.getUserService();
            String userName = ShiroUtils.getPrimaryPrincipal(SecurityUtils.getSubject()) + "";
            SecurityUtils.getSubject().logout();
            HttpSession session = context.getRequest().getSession(false);
            if (session != null) {
                session.invalidate();
            }

            Locale locale = context.getLocale();
            Application application = (Application) context.getRequest().getAttribute(RequestAttributes.APPLICATION);
            ResourceBundle bundle = application.getBundle(locale);
            String msg = bundle.getString("user.logout");
            SessionMessages.addInfoMessage(msg);
            logger.info("User {} logout", userName);
            String logoutUrl = userService.createLogoutURL(context.getRequest().getContextPath() + "/");
            return new RedirectResolution(logoutUrl);
        } else {
            return new RedirectResolution("/");
        }
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}
