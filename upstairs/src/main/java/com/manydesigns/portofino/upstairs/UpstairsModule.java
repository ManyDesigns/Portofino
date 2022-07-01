/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.upstairs;

import com.manydesigns.portofino.ResourceActionsModule;
import com.manydesigns.portofino.dispatcher.DispatcherInitializer;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.modules.ModuleStatus;
import com.manydesigns.portofino.rest.PortofinoRoot;
import com.manydesigns.portofino.upstairs.actions.UpstairsAction;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

/**
 * Root resource for the "upstairs" operations that work "one level above" the application, i.e. on the application's
 * model (also known as the "metamodel" if we consider the application's database as the "model").
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class UpstairsModule implements Module, ApplicationContextAware {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    @Autowired
    public ServletContext servletContext;
    @Autowired
    @Qualifier(ResourceActionsModule.ACTIONS_DIRECTORY)
    public FileObject actionsDirectory;
    @Autowired
    public ModelService modelService;
    @Autowired
    public DispatcherInitializer dispatcherInitializer;

    protected ApplicationContext applicationContext;
    protected ModuleStatus status = ModuleStatus.CREATED;

    public static final Logger logger = LoggerFactory.getLogger(UpstairsModule.class);

    @Override
    public String getModuleVersion() {
        return Module.getPortofinoVersion();
    }

    @Override
    public String getName() {
        return "\"Upstairs\" (configure, add, remove pages)";
    }

    @PostConstruct
    public void init() {
        modelService.modelEvents.filter(evt -> evt == ModelService.EventType.LOADED).take(1).subscribe(evt -> {
            try {
                PortofinoRoot root = ResourceActionsModule.getRootResource(
                        actionsDirectory, dispatcherInitializer.getResourceResolver(),
                        servletContext, applicationContext, modelService);
                root.mountPackage("portofino-upstairs", UpstairsAction.class.getPackage());
            } catch (Exception e) {
                logger.error("Could not install upstairs actions", e);
            }
        });
        status = ModuleStatus.STARTED;
    }

    @PreDestroy
    public void destroy() {
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
