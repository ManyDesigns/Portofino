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

import com.manydesigns.portofino.cache.CacheResetListenerRegistry;
import com.manydesigns.portofino.code.AggregateCodeBase;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.database.model.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.hibernate.EntityMode;
import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyImplementationFactory;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import io.reactivex.disposables.Disposable;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.AllFileSelector;
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
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DatabaseModule implements Module, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";
    public static final String GENERATED_CLASSES_DIRECTORY_NAME = "classes-generated";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Autowired
    public ServletContext servletContext;

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.APPLICATION_DIRECTORY)
    public FileObject applicationDirectory;

    protected ApplicationContext applicationContext;

    protected ModuleStatus status = ModuleStatus.CREATED;

    protected final AggregateCodeBase persistenceCodeBase = new AggregateCodeBase(null, getClass().getClassLoader());
    protected Disposable subscription;

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
        return Module.getPortofinoVersion();
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
    public MultiTenancyImplementationFactory getApplicationContextMTImplFactory() {
        return implClass -> {
            try {
                return applicationContext.getBean(implClass);
            } catch (BeansException e) {
                logger.error("MultiTenancyImplementation is not a valid spring bean, trying default constructor");
                return MultiTenancyImplementationFactory.DEFAULT.make(implClass);
            }
        };
    }

    @Bean
    public Persistence getPersistence(
            @Autowired ModelService modelService,
            @Autowired DatabasePlatformsRegistry databasePlatformsRegistry,
            @Autowired CacheResetListenerRegistry cacheResetListenerRegistry) throws FileSystemException {
        Persistence persistence = new Persistence(modelService, configuration, databasePlatformsRegistry);
        persistence.cacheResetListenerRegistry = cacheResetListenerRegistry;
        if(applicationContext != null) { //We may want it to be null when testing
            applicationContext.getAutowireCapableBeanFactory().autowireBean(persistence);
        }

        FileObject generatedClassesRoot = applicationDirectory.resolveFile(GENERATED_CLASSES_DIRECTORY_NAME);
        generatedClassesRoot.createFolder();
        AllFileSelector allFileSelector = new AllFileSelector();
        //When the entity mode is POJO:
        // - make generated classes visible to shared classes and actions;
        // - write them in the application directory so the user's IDE and tools can know about them.
        subscription = persistence.databaseSetupEvents.subscribe(e -> {
            String databaseName = e.setup.getDatabase().getDatabaseName();
            FileObject inMemoryDatabaseDir = e.setup.getCodeBase().getRoot().resolveFile(databaseName);
            FileObject externalDatabaseDir = generatedClassesRoot.resolveFile(databaseName);
            externalDatabaseDir.deleteAll();
            switch (e.type) {
                case Persistence.DatabaseSetupEvent.ADDED:
                    persistenceCodeBase.add(e.setup.getCodeBase());
                    if(e.setup.getEntityMode() == EntityMode.POJO) {
                        externalDatabaseDir.copyFrom(inMemoryDatabaseDir, allFileSelector);
                    }
                    break;
                case Persistence.DatabaseSetupEvent.REMOVED:
                    persistenceCodeBase.remove(e.setup.getCodeBase());
                    externalDatabaseDir.deleteAll();
                    inMemoryDatabaseDir.deleteAll();
                    break;
                case Persistence.DatabaseSetupEvent.REPLACED:
                    persistenceCodeBase.replace(e.oldSetup.getCodeBase(), e.setup.getCodeBase());
                    externalDatabaseDir.deleteAll();
                    if(e.setup.getEntityMode() == EntityMode.POJO) {
                        externalDatabaseDir.copyFrom(inMemoryDatabaseDir, allFileSelector);
                    }
                    break;
            }
        });
        return persistence;
    }

    @PreDestroy
    public void destroy() {
        logger.info("ManyDesigns Portofino database module stopping...");
        applicationContext.getBean(Persistence.class).stop();
        if(subscription != null) {
            subscription.dispose();
            subscription = null;
        }
        logger.info("ManyDesigns Portofino database module stopped.");
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
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
