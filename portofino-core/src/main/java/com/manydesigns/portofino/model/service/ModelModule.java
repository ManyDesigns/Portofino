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

package com.manydesigns.portofino.model.service;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.modules.ModuleStatus;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.vfs2.FileObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Order(ModelModule.MODEL_LOAD)
public class ModelModule implements Module, ApplicationListener<ContextRefreshedEvent> {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public static final String PORTOFINO_DOMAIN =
            "com.manydesigns.portofino.model.service.ModelModule.portofinoDomain";

    public static final int MODEL_LOAD = 100;

    public @Autowired ModelService modelService;

    protected ModuleStatus status = ModuleStatus.CREATED;

    public static final Logger logger = LoggerFactory.getLogger(ModelModule.class);

    @Override
    public String getModuleVersion() {
        return Module.getPortofinoVersion();
    }

    @Override
    public String getName() {
        return "Model";
    }

    @PostConstruct
    public void init() {
        status = ModuleStatus.ACTIVE;
    }

    @Bean(name = PORTOFINO_DOMAIN)
    public Domain getPortofinoDomain(@Autowired ModelService modelService) {
        return modelService.getPortofinoDomain();
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        try {
            modelService.loadModel();
        } catch (IOException e) {
            logger.error("Could not load model", e);
        }
    }

}
