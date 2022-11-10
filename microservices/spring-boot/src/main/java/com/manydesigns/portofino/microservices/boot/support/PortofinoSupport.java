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

package com.manydesigns.portofino.microservices.boot.support;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.spring.SpringBootResourceFileProvider;
import com.manydesigns.portofino.spring.SpringEnvironmentConfiguration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.io.IOException;

import static com.manydesigns.portofino.spring.PortofinoSpringConfiguration.*;
import static com.manydesigns.portofino.spring.PortofinoSpringConfiguration.CONFIGURATION_SOURCE;

/** Import this configuration in a Spring Boot application to support configuring Portofino modules
 * such as persistence.
 * Note that this doesn't enable Portofino's dispatcher. You need {@link PortofinoDispatcherSupport} for that.
 */
@Import({ PortofinoSpringConfiguration.class })
public class PortofinoSupport implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(PortofinoSupport.class);

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean(name = CONFIGURATION_SOURCE)
    public ConfigurationSource getConfiguration(@Autowired @Qualifier(APPLICATION_DIRECTORY) FileObject appDir)
            throws FileSystemException, ConfigurationException {
        CompositeConfiguration configuration = new CompositeConfiguration();

        FileObject configurationFile = appDir.resolveFile("portofino.properties");
        PropertiesBuilderParameters parameters =
                new Parameters()
                        .properties()
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        FileBasedConfigurationBuilder<PropertiesConfiguration> writableConfiguration = null;
        if (!configurationFile.exists()) {
            logger.debug(configurationFile.getPublicURIString() + " does not exist, ignoring");
        } else if (configurationFile.isWriteable()) {
            String path = configurationFile.getName().getURI();
            parameters.setFileName(path);
            writableConfiguration = new Configurations().propertiesBuilder(path).configure(parameters);
            configuration.addConfiguration(writableConfiguration.getConfiguration());
        } else {
            configuration.addConfiguration(new Configurations().propertiesBuilder(parameters).getConfiguration());
        }

        configuration.addConfiguration(new SpringEnvironmentConfiguration(applicationContext.getEnvironment()));
        return new ConfigurationSource(configuration, writableConfiguration);
    }

    @Bean(name = PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION)
    public Configuration getPortofinoConfiguration(@Autowired @Qualifier(CONFIGURATION_SOURCE) ConfigurationSource c) {
        return c.getProperties();
    }

    @Bean(name = APPLICATION_DIRECTORY)
    public FileObject getApplicationDirectory() throws FileSystemException {
        installCommonsVfsBootSupport();
        FileObject fileObject = VFS.getManager().resolveFile("res:portofino");
        if (fileObject.getType() != FileType.FOLDER) {
            throw new FileNotFolderException(fileObject);
        }
        return fileObject;
    }

    @Bean(name = MODEL_SERVICE)
    public ModelService getModelService(
            @Autowired @Qualifier(APPLICATION_DIRECTORY) FileObject appDir,
            @Autowired @Qualifier(CONFIGURATION_SOURCE) ConfigurationSource configuration,
            @Autowired CodeBase codeBase) {
        return new ModelService(appDir, configuration, codeBase);
    }

    @Bean
    public CodeBase getCodeBase(@Autowired @Qualifier(APPLICATION_DIRECTORY) FileObject appDir) throws IOException {
        return new JavaCodeBase(appDir.getParent());
    }

    public static void installCommonsVfsBootSupport() throws FileSystemException {
        ((DefaultFileSystemManager) VFS.getManager()).removeProvider("res");
        ((DefaultFileSystemManager) VFS.getManager()).addProvider("res", new SpringBootResourceFileProvider());
    }
}
