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

package com.manydesigns.portofino.stripes;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.dispatcher.*;
import groovy.lang.GroovyObject;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.AnnotatedClassActionResolver;
import net.sourceforge.stripes.controller.NameBasedActionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ModelActionResolver extends NameBasedActionResolver {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(ModelActionResolver.class);

    protected ServletContext servletContext;

    protected Map<Class<? extends ActionBean>,Map<String,Method>> eventMappings;

    @Override
    public void init(Configuration configuration) throws Exception {
        super.init(configuration);
        servletContext = configuration.getServletContext();

        Field eventMappingsField = AnnotatedClassActionResolver.class.getDeclaredField("eventMappings");
        eventMappingsField.setAccessible(true);
        eventMappings = (Map<Class<? extends ActionBean>, Map<String, Method>>) eventMappingsField.get(this);
    }

    @Override
    public Class<? extends ActionBean> getActionBeanType(String path) {
        Dispatch dispatch = getDispatch(path);
        if(dispatch != null) {
            Class<? extends ActionBean> actionBeanClass = dispatch.getActionBeanClass();
            if(GroovyObject.class.isAssignableFrom(actionBeanClass) &&
               !eventMappings.containsKey(actionBeanClass)) {
                synchronized (this) {
                    addActionBean(actionBeanClass);
                }
            }
            return actionBeanClass;
        } else {
            return super.getActionBeanType(path);
        }
    }

    @Override
    protected ActionBean makeNewActionBean(
            Class<? extends ActionBean> type, ActionBeanContext context) throws Exception {
        Dispatch dispatch = DispatcherUtil.getDispatch(context.getRequest());
        if(dispatch != null) {
            PageInstance pageInstance = dispatch.getLastPageInstance();
            if(type.equals(pageInstance.getActionClass())) {
                if(pageInstance.getActionBean() != null) {
                    return pageInstance.getActionBean();
                } else {
                    if(DispatcherLogic.isValidActionClass(type)) {
                        ActionBean actionBean = super.makeNewActionBean(type, context);
                        pageInstance.setActionBean((PageAction) actionBean);
                        return actionBean;
                    } else {
                        throw new Exception("Invalid action bean type for dispatch: " + type); //TODO
                    }
                }
            }
        }
        return super.makeNewActionBean(type, context);
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
        Dispatcher dispatcher = DispatcherUtil.get(request);
        return dispatcher != null ?
               dispatcher.getDispatch(path) :
               null;
    }

    @Override
    public synchronized void removeActionBean(Class<? extends ActionBean> clazz) {
        super.removeActionBean(clazz);
    }

    @Override
    public String getUrlBinding(Class<? extends ActionBean> clazz) {
        String urlBinding = super.getUrlBinding(clazz);
        if(GroovyObject.class.isAssignableFrom(clazz) && !urlBinding.endsWith("__groovy__")) {
            //Meglio evitare conflitti con action configurate normalmente
            return urlBinding + clazz.hashCode() + "__groovy__";
        } else {
            return urlBinding;
        }
    }
}
