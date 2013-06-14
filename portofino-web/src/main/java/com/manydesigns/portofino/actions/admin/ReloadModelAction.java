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

package com.manydesigns.portofino.actions.admin;

import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.servlets.ServerInfo;
import net.sourceforge.stripes.action.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAdministrator
@UrlBinding("/actions/admin/reload-model")
public class ReloadModelAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    @Inject(RequestAttributes.APPLICATION)
    Application application;

    @Inject(ApplicationAttributes.SERVER_INFO)
    ServerInfo serverInfo;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger =
            LoggerFactory.getLogger(ReloadModelAction.class);

    //--------------------------------------------------------------------------
    // Action events
    //--------------------------------------------------------------------------

    @DefaultHandler
    public Resolution execute() {
        return new ForwardResolution("/layouts/admin/reload-model.jsp");
    }

    @Button(list = "reload-model", key = "model.reload", order = 1, primary = true)
    @RequiresAdministrator
    public Resolution reloadModel() {
        synchronized (application) {
            application.loadXmlModel();
            DispatcherLogic.clearConfigurationCache();
            SessionMessages.addInfoMessage(getMessage("model.reloaded"));
            return new ForwardResolution("/layouts/admin/reload-model.jsp");
        }
    }

    private String getMessage(String key) {
        Locale locale = context.getLocale();
        ResourceBundle bundle = application.getBundle(locale);
        return bundle.getString(key);
    }

    @Button(list = "reload-model-bar", key = "commons.returnToPages", order = 1)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

}
