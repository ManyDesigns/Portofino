/*
 * Copyright (C) 2005-2022 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.modules;

import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.resourceactions.crud.CrudAction;
import com.manydesigns.portofino.resourceactions.crud.configuration.CrudConfiguration;
import com.manydesigns.portofino.resourceactions.crud.export.CrudExporterRegistry;
import com.manydesigns.portofino.resourceactions.crud.export.JSONExporter;
import com.manydesigns.portofino.resourceactions.m2m.ManyToManyAction;
import com.manydesigns.portofino.resourceactions.m2m.configuration.ManyToManyConfiguration;
import com.manydesigns.portofino.resourceactions.registry.ActionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PreDestroy;
import java.beans.IntrospectionException;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class CrudModule extends ManagedModule {
    public static final String copyright =
            "Copyright (C) 2005-2022 ManyDesigns srl";
    @Autowired
    public ActionRegistry actionRegistry;

    @Autowired
    public ModelService modelService;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(CrudModule.class);

    @Override
    public String getModuleVersion() {
        return Module.getPortofinoVersion();
    }

    @Override
    public String getName() {
        return "CRUD";
    }

    @Bean
    public CrudExporterRegistry getCrudExporterRegistry() {
        CrudExporterRegistry registry = new CrudExporterRegistry();
        registry.register(new JSONExporter());
        return registry;
    }

    @Override
    protected void addRequiredClasses() throws IntrospectionException {
        super.addRequiredClasses();
        modelService.addBuiltInClass(CrudConfiguration.class);
        modelService.addBuiltInClass(
                com.manydesigns.portofino.resourceactions.crud.configuration.database.CrudConfiguration.class);
        modelService.addBuiltInClass(ManyToManyConfiguration.class);
    }

    @Override
    protected void start(ApplicationContext applicationContext) {
        actionRegistry.register(CrudAction.class);
        actionRegistry.register(ManyToManyAction.class);
    }

    @PreDestroy
    public void destroy() {
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
