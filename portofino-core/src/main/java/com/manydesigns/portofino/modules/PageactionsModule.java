/*
 * Copyright (C) 2005-2017 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.cache.CacheResetEvent;
import com.manydesigns.portofino.cache.CacheResetListener;
import com.manydesigns.portofino.cache.CacheResetListenerRegistry;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.pageactions.custom.CustomAction;
import com.manydesigns.portofino.pageactions.form.FormAction;
import com.manydesigns.portofino.pageactions.form.TableFormAction;
import com.manydesigns.portofino.pageactions.registry.ActionRegistry;
import com.manydesigns.portofino.pages.PageLogic;
import com.manydesigns.portofino.shiro.SecurityClassRealm;
import com.manydesigns.portofino.shiro.SelfRegisteringShiroFilter;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import io.jsonwebtoken.io.Encoders;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.WebEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import java.io.File;
import java.util.UUID;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PageactionsModule implements Module, ApplicationContextAware {
    public static final String copyright =
            "Copyright (C) 2005-2017 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Autowired
    public ServletContext servletContext;

    @Autowired
    public Configuration configuration;

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.APPLICATION_DIRECTORY)
    public File applicationDirectory;

    @Autowired
    public CodeBase codeBase;

    @Autowired
    public CacheResetListenerRegistry cacheResetListenerRegistry;

    protected EnvironmentLoader environmentLoader = new EnvironmentLoader();
    protected ApplicationContext applicationContext;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(PageactionsModule.class);

    @Override
    public String getModuleVersion() {
        return PortofinoProperties.getPortofinoVersion();
    }

    @Override
    public String getName() {
        return "Pageactions";
    }

    @PostConstruct
    public void init() {
        logger.debug("Initializing dispatcher");
        PageLogic.init(configuration);

        File actionsDirectory = new File(applicationDirectory, "actions");
        logger.info("Pages directory: " + actionsDirectory);
        ElementsFileUtils.ensureDirectoryExistsAndWarnIfNotWritable(actionsDirectory);

        if(configuration.getBoolean(PortofinoProperties.GROOVY_PRELOAD_PAGES, false)) {
            logger.info("Preloading pages");
            preloadPageActions(actionsDirectory);
        }
        if(configuration.getBoolean(PortofinoProperties.GROOVY_PRELOAD_CLASSES, false)) {
            logger.info("Preloading Groovy classes");
            preloadClasses(codeBase.getRoot());
        }

        cacheResetListenerRegistry.getCacheResetListeners().add(new ConfigurationCacheResetListener());

        if(!configuration.containsKey("jwt.secret")) {
            String jwtSecret = Encoders.BASE64.encode((UUID.randomUUID() + UUID.randomUUID().toString()).getBytes());
            logger.warn("No jwt.secret property was set, so we generated one: {}. It will only be valid until the application stops.", jwtSecret);
            configuration.setProperty("jwt.secret", jwtSecret);
        }

        logger.info("Initializing Shiro environment");
        WebEnvironment environment = environmentLoader.initEnvironment(servletContext);
        RealmSecurityManager rsm = (RealmSecurityManager) environment.getWebSecurityManager();
        SelfRegisteringShiroFilter shiroFilter = SelfRegisteringShiroFilter.get(servletContext);
        if(shiroFilter != null) {
            try {
                //when reloading the Spring context, this overwrites the filter's stale security manager.
                shiroFilter.init();
            } catch (Exception e) {
                logger.error("Could not initialize the Shiro filter", e);
                status = ModuleStatus.FAILED;
                return;
            }
        }
        logger.debug("Creating SecurityClassRealm");
        try {
            SecurityClassRealm realm = new SecurityClassRealm(codeBase, "Security", applicationContext);
            LifecycleUtils.init(realm);
            rsm.setRealm(realm);
            status = ModuleStatus.STARTED;
        } catch (Exception  e) {
            logger.error("Security class not found or invalid; installing dummy realm", e);
            SimpleAccountRealm realm = new SimpleAccountRealm();
            LifecycleUtils.init(realm);
            rsm.setRealm(realm);
            status = ModuleStatus.FAILED;
        }
    }

    @Bean
    public ActionRegistry getPageActionRegistry() {
        logger.debug("Creating pageactions registry");
        ActionRegistry actionRegistry = new ActionRegistry();
        actionRegistry.register(CustomAction.class);
        actionRegistry.register(FormAction.class);
        actionRegistry.register(TableFormAction.class);
        return actionRegistry;
    }

    protected void preloadPageActions(File directory) {
        /*for(File file : directory.listFiles()) {
            logger.debug("visit {}", file);
            if(file.isDirectory()) {
                if(!file.equals(directory) && !file.equals(directory.getParentFile())) {
                    preloadPageActions(file);
                }
            } else if("action.groovy".equals(file.getName())) {
                logger.debug("Preloading page: {}", file);
                try {
                    Class<?> clazz = PageLogic.getActionClass(configuration, directory);
                    clazz.newInstance();
                } catch(Throwable t) {
                    logger.warn("PageAction preload failed for page " + file.getAbsolutePath(), t);
                }
            }
        }*/
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
                    String className = file.getName().getRelativeName(codeBase.getRoot().getName());
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
        logger.info("Destroying Shiro environment...");
        environmentLoader.destroyEnvironment(servletContext);
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }

    private static class ConfigurationCacheResetListener implements CacheResetListener {
        @Override
        public void handleReset(CacheResetEvent e) {
            PageLogic.clearConfigurationCache();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
