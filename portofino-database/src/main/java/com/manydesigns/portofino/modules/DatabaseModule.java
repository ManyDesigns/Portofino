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

package com.manydesigns.portofino.modules;

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.cache.CacheResetListenerRegistry;
import com.manydesigns.portofino.code.AggregateCodeBase;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.model.database.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DatabaseModule implements Module, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Autowired
    public ServletContext servletContext;

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION_FILE)
    public FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile;

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.APPLICATION_DIRECTORY)
    public FileObject applicationDirectory;

    protected ApplicationContext applicationContext;

    protected ModuleStatus status = ModuleStatus.CREATED;

    protected final AggregateCodeBase persistenceCodeBase = new AggregateCodeBase(null, getClass().getClassLoader());

    //**************************************************************************
    // Constants
    //**************************************************************************

    //Liquibase properties
    public static final String LIQUIBASE_ENABLED = "liquibase.enabled";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(DatabaseModule.class);

    @Override
    public String getModuleVersion() {
        return PortofinoProperties.getPortofinoVersion();
    }

    @Override
    public String getName() {
        return "Database";
    }

    @PostConstruct
    public void init() {
        status = ModuleStatus.ACTIVE;
    }

    @Autowired
    public void setCodeBase(CodeBase codeBase) throws Exception {
        codeBase.setParent(persistenceCodeBase);
    }

    @Bean
    public DatabasePlatformsRegistry getDatabasePlatformsRegistry() {
        return new DatabasePlatformsRegistry(configuration);
    }

    @Bean
    public Persistence getPersistence(
            @Autowired DatabasePlatformsRegistry databasePlatformsRegistry,
            @Autowired CacheResetListenerRegistry cacheResetListenerRegistry) throws FileSystemException {
        Persistence persistence = new Persistence(applicationDirectory, configuration, configurationFile, databasePlatformsRegistry);
        persistence.cacheResetListenerRegistry = cacheResetListenerRegistry;
        //Make generated classes visible to shared classes and actions
        persistence.databaseSetupEvents.subscribe(e -> {
            switch (e.type) {
                case Persistence.DatabaseSetupEvent.ADDED:
                    persistenceCodeBase.add(e.setup.getCodeBase());
                    break;
                case Persistence.DatabaseSetupEvent.REMOVED:
                    persistenceCodeBase.remove(e.setup.getCodeBase());
                    break;
                case Persistence.DatabaseSetupEvent.REPLACED:
                    persistenceCodeBase.replace(e.oldSetup.getCodeBase(), e.setup.getCodeBase());
                    break;
            }
        });
        return persistence;
    }

    @PreDestroy
    public void destroy() {
        logger.info("ManyDesigns Portofino database module stopping...");
        applicationContext.getBean(Persistence.class).stop();
        logger.info("ManyDesigns Portofino database module stopped.");
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

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        Persistence persistence = applicationContext.getBean(Persistence.class);
        Persistence.Status status = persistence.status.getValue();
        if(status == null || status == Persistence.Status.STOPPED) {
            logger.info("Starting persistence...");
            persistence.start();
            this.status = ModuleStatus.STARTED;
            logger.info("Persistence started.");
        }
    }
}
