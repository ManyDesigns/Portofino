/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino;

import com.manydesigns.portofino.actions.ActionLogic;
import com.manydesigns.portofino.cache.CacheResetEvent;
import com.manydesigns.portofino.cache.CacheResetListener;
import com.manydesigns.portofino.cache.CacheResetListenerRegistry;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.modules.ModuleStatus;
import com.manydesigns.portofino.resourceactions.custom.CustomAction;
import com.manydesigns.portofino.resourceactions.form.FormAction;
import com.manydesigns.portofino.resourceactions.form.TableFormAction;
import com.manydesigns.portofino.resourceactions.registry.ActionRegistry;
import com.manydesigns.portofino.rest.PortofinoApplicationRoot;
import com.manydesigns.portofino.security.SecurityLogic;
import com.manydesigns.portofino.security.noop.login.NoOpLoginAction;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import java.io.IOException;

import static com.manydesigns.portofino.model.service.ModelModule.PORTOFINO_DOMAIN;
import static com.manydesigns.portofino.spring.PortofinoSpringConfiguration.APPLICATION_DIRECTORY;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ResourceActionsModule implements Module {
    public static final String copyright =
            "Copyright (C) 2005-2021 ManyDesigns srl";

    public static final String ACTIONS_DIRECTORY = "actionsDirectory";
    public static final String ACTIONS_DOMAIN = "actionsDomain";

    @Autowired
    public ServletContext servletContext;

    @Autowired
    public ConfigurationSource configuration;

    @Autowired
    @Qualifier(APPLICATION_DIRECTORY)
    public FileObject applicationDirectory;

    @Autowired
    public CodeBase codeBase;

    @Autowired
    public CacheResetListenerRegistry cacheResetListenerRegistry;

    @Autowired
    public ModelService modelService;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(ResourceActionsModule.class);

    @Override
    public String getModuleVersion() {
        return Module.getPortofinoVersion();
    }

    @Override
    public String getName() {
        return "ResourceActions";
    }

    @PostConstruct
    public void init() throws Exception {
        logger.debug("Initializing dispatcher");
        ActionLogic.init(configuration.getProperties());

        //noinspection SpringConfigurationProxyMethods - @PostConstruct init() is a lifecycle method, it cannot have arguments
        FileObject actionsDirectory = getActionsDirectory(configuration, applicationDirectory);
        logger.info("Actions directory: " + actionsDirectory);
        //TODO ElementsFileUtils.ensureDirectoryExistsAndWarnIfNotWritable(actionsDirectory);

        if(configuration.getProperties().getBoolean(PortofinoProperties.PRELOAD_ACTIONS, false)) {
            logger.info("Preloading actions");
            try {
                ResourceResolver resourceResolver =
                        PortofinoApplicationRoot.getRootFactory().createRoot().getResourceResolver();
                preloadResourceActions(actionsDirectory, resourceResolver);
            } catch (Exception e) {
                logger.warn("Could not preload actions", e);
            }
        }
        if(configuration.getProperties().getBoolean(PortofinoProperties.PRELOAD_CLASSES, false)) {
            logger.info("Preloading Groovy classes");
            preloadClasses(codeBase.getRoot());
        }

        cacheResetListenerRegistry.getCacheResetListeners().add(new ConfigurationCacheResetListener());

        SecurityLogic.installLogin(actionsDirectory, configuration.getProperties(), NoOpLoginAction.class);
        status = ModuleStatus.STARTED;
    }

    @Bean
    public ActionRegistry getResourceActionRegistry() {
        logger.debug("Creating actions registry");
        ActionRegistry actionRegistry = new ActionRegistry();
        actionRegistry.register(CustomAction.class);
        actionRegistry.register(FormAction.class);
        actionRegistry.register(TableFormAction.class);
        return actionRegistry;
    }

    @Bean(name = ACTIONS_DIRECTORY)
    public FileObject getActionsDirectory(
            @Autowired ConfigurationSource configuration,
            @Autowired @Qualifier(APPLICATION_DIRECTORY) FileObject applicationDirectory) throws FileSystemException {
        String actionsDirectory = configuration.getProperties().getString("portofino.actions.path", "actions");
        return applicationDirectory.resolveFile(actionsDirectory);
    }

    @Bean(name = ACTIONS_DOMAIN)
    @Scope("prototype")
    public Domain getActionsDomain() {
        return modelService.ensureTopLevelDomain("resourceactions", true);
    }

    protected void preloadResourceActions(FileObject directory, ResourceResolver resourceResolver) throws FileSystemException {
        for(FileObject child : directory.getChildren()) {
            logger.debug("Preload resource action {}", child);
            if(child.getType() == FileType.FOLDER) {
                if(!child.equals(directory) && !child.equals(directory.getParent())) {
                    try {
                        resourceResolver.resolve(child, Class.class).getConstructor().newInstance();
                    } catch(Throwable t) {
                        logger.warn("ResourceAction preload failed for actionDescriptor " + child.getName().getPath(), t);
                    }
                    preloadResourceActions(child, resourceResolver);
                }
            }
        }
    }

    protected void preloadClasses(FileObject directory) {
        try {
            for(FileObject file : directory.getChildren()) {
                logger.debug("visit {}", file);
                if(file.getType() == FileType.FOLDER) {
                    if(!file.equals(directory) && !file.equals(directory.getParent())) {
                        preloadClasses(file);
                    }
                } else {
                    String extension = file.getName().getExtension();
                    String className = codeBase.getRoot().getName().getRelativeName(file.getName());
                    if(!StringUtils.isEmpty(extension)) {
                        className = className.substring(0, className.length() - extension.length() - 1);
                    }
                    logger.debug("Preloading " + className);
                    try {
                        codeBase.loadClass(className);
                    } catch(Throwable t) {
                        logger.warn("Class preload failed for " + className, t);
                    }
                }
            }
        } catch (FileSystemException e) {
            logger.warn("Could not preload classes under " + directory, e);
        }
    }

    @PreDestroy
    public void destroy() {
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }

    private static class ConfigurationCacheResetListener implements CacheResetListener {
        @Override
        public void handleReset(CacheResetEvent e) {
            ActionLogic.clearConfigurationCache();
        }
    }
}
