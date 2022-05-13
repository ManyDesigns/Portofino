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
import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.modules.ModuleStatus;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ModelModule implements Module {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public static final String PORTOFINO_DOMAIN =
            "com.manydesigns.portofino.model.service.ModelModule.portofinoDomain";

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION_FILE)
    public FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile;

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.APPLICATION_DIRECTORY)
    public FileObject applicationDirectory;

    @Autowired
    public CodeBase codeBase;

    protected ModuleStatus status = ModuleStatus.CREATED;
    protected ModelService modelService;

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

    @Bean
    public ModelService getModelService() throws IOException {
        ModelService modelService = new ModelService(applicationDirectory, configuration, configurationFile, codeBase);
        modelService.loadModel();
        return modelService;
    }

    @Bean(name = PORTOFINO_DOMAIN)
    public Domain getPortofinoDomain(@Autowired ModelService modelService) {
        return modelService.getPortofinoDomain();
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }

}
