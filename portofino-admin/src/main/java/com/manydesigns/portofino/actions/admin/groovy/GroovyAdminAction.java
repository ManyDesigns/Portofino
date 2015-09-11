/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.admin.groovy;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import groovy.util.GroovyScriptEngine;
import net.sourceforge.stripes.action.*;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAuthentication
@RequiresAdministrator
@UrlBinding(GroovyAdminAction.URL_BINDING)
public class GroovyAdminAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String URL_BINDING = "/actions/admin/groovy";

    @Inject(BaseModule.GROOVY_CLASS_PATH)
    File groovyClasspath;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger = LoggerFactory.getLogger(GroovyAdminAction.class);

    //--------------------------------------------------------------------------
    // Action events
    //--------------------------------------------------------------------------

    @DefaultHandler
    public Resolution execute() {
        return new ForwardResolution("/m/admin/groovy.jsp");
    }

    @Button(list = "groovy", key = "reset.groovy.script.engine", order = 1, type = Button.TYPE_PRIMARY , icon = Button.ICON_RELOAD)
    public Resolution resetGroovyScriptEngine() {
        logger.info("Resetting Groovy script engine");
        ServletContext servletContext = context.getServletContext();
        GroovyScriptEngine groovyScriptEngine =
                ScriptingUtil.createScriptEngine(groovyClasspath, getClass().getClassLoader());
        ClassLoader classLoader = groovyScriptEngine.getGroovyClassLoader();
        servletContext.setAttribute(BaseModule.CLASS_LOADER, classLoader);
        servletContext.setAttribute(BaseModule.GROOVY_SCRIPT_ENGINE, groovyScriptEngine);
        SessionMessages.addInfoMessage(ElementsThreadLocals.getText("script.engine.successfully.reset"));

        logger.info("Clearing OGNL caches potentially holding Groovy objects");
        OgnlUtils.clearCache();
        logger.info("Groovy script engine reset.");
        return new ForwardResolution("/m/admin/groovy.jsp");
    }

    @Button(list = "groovy", key = "return.to.pages", order = 2  , icon = Button.ICON_HOME)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

}
