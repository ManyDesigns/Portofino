/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.actions;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.shiro.ShiroUtils;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.io.Serializable;

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
            Serializable userId = ShiroUtils.getUserId(SecurityUtils.getSubject());
            SecurityUtils.getSubject().logout();
            HttpSession session = context.getRequest().getSession(false);
            if (session != null) {
                session.invalidate();
            }

            String msg = ElementsThreadLocals.getText("user.disconnected");
            SessionMessages.addInfoMessage(msg);
            logger.info("User {} logout", userId);
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
