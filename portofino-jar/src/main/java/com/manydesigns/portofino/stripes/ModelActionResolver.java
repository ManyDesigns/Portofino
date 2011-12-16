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

package com.manydesigns.portofino.stripes;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.ApplicationStarter;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.Dispatcher;
import groovy.lang.GroovyObject;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.NameBasedActionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ModelActionResolver extends NameBasedActionResolver {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(ModelActionResolver.class);

    protected Dispatcher dispatcher;
    protected ServletContext servletContext;

    @Override
    public void init(Configuration configuration) throws Exception {
        super.init(configuration);
        servletContext = configuration.getServletContext();
        logger.debug("Retrieving application starter");
        ApplicationStarter applicationStarter =
                (ApplicationStarter) servletContext.getAttribute(
                        ApplicationAttributes.APPLICATION_STARTER);

        logger.debug("Retrieving application");
        Application application;
        try {
            application = applicationStarter.getApplication();
            dispatcher = new Dispatcher(application);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public Class<? extends ActionBean> getActionBeanType(String path) {
        Dispatch dispatch = getDispatch(path);
        if(dispatch != null) {
            Class<? extends ActionBean> actionBeanClass = dispatch.getActionBeanClass();
            if(GroovyObject.class.isAssignableFrom(actionBeanClass)) {
                synchronized (this) { //TODO aggiungere solo se necessario?
                    addActionBean(actionBeanClass);
                }
            }
            return actionBeanClass;
        } else {
            return super.getActionBeanType(path);
        }
    }

    @Override
    public String getUrlBindingFromPath(String path) {
        Dispatch dispatch = getDispatch(path);
        if(dispatch != null) {
            return path;
        } else {
            return super.getUrlBindingFromPath(path);
        }
    }

    protected Dispatch getDispatch(String path) {
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        return dispatcher.createDispatch(request.getContextPath(), path);
    }

    @Override
    public synchronized void removeActionBean(Class<? extends ActionBean> clazz) {
        super.removeActionBean(clazz);
    }

    @Override
    public String getUrlBinding(Class<? extends ActionBean> clazz) {
        if(GroovyObject.class.isAssignableFrom(clazz)) {
            //Meglio evitare conflitti con action configurate normalmente
            return super.getUrlBinding(clazz) + "__groovy__";
        } else {
            return super.getUrlBinding(clazz);
        }
    }
}
