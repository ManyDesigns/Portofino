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

package com.manydesigns.portofino.modules;

import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.admin.SettingsAction;
import com.manydesigns.portofino.cache.CacheResetEvent;
import com.manydesigns.portofino.cache.CacheResetListener;
import com.manydesigns.portofino.cache.CacheResetListenerRegistry;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.menu.MenuBuilder;
import com.manydesigns.portofino.menu.SimpleMenuAppender;
import com.manydesigns.portofino.pageactions.custom.CustomAction;
import com.manydesigns.portofino.pageactions.login.DefaultLoginAction;
import com.manydesigns.portofino.pageactions.registry.PageActionRegistry;
import com.manydesigns.portofino.pageactions.registry.TemplateRegistry;
import com.manydesigns.portofino.shiro.SecurityGroovyRealm;
import groovy.lang.GroovyClassLoader;
import net.sf.ehcache.CacheManager;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.WebEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PageactionsModule implements Module {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Inject(BaseModule.SERVLET_CONTEXT)
    public ServletContext servletContext;

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Inject(BaseModule.APPLICATION_DIRECTORY)
    public File applicationDirectory;

    @Inject(BaseModule.ADMIN_MENU)
    public MenuBuilder adminMenu;

    @Inject(BaseModule.CLASS_LOADER)
    public GroovyClassLoader classLoader;

    @Inject(BaseModule.GROOVY_CLASS_PATH)
    public File groovyClasspath;

    @Inject(BaseModule.CACHE_RESET_LISTENER_REGISTRY)
    public CacheResetListenerRegistry cacheResetListenerRegistry;

    protected EnvironmentLoader environmentLoader = new EnvironmentLoader();

    protected CacheManager cacheManager;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String PAGES_DIRECTORY = "PAGES_DIRECTORY";
    public static final String EHCACHE_MANAGER = "portofino.ehcache.manager";
    public static final String PAGE_ACTIONS_REGISTRY =
            "com.manydesigns.portofino.pageactions.registry.PageActionRegistry";
    public static final String TEMPLATES_REGISTRY =
            "com.manydesigns.portofino.pageactions.templates.registry";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(PageactionsModule.class);

    @Override
    public String getModuleVersion() {
        return ModuleRegistry.getPortofinoVersion();
    }

    @Override
    public int getMigrationVersion() {
        return 1;
    }

    @Override
    public double getPriority() {
        return 10;
    }

    @Override
    public String getId() {
        return "pageactions";
    }

    @Override
    public String getName() {
        return "Pageactions";
    }

    @Override
    public int install() {
        return 1;
    }

    @Override
    public void init() {
        logger.debug("Initializing dispatcher");
        DispatcherLogic.init(configuration);

        logger.info("Initializing ehcache service");
        cacheManager = CacheManager.newInstance();
        servletContext.setAttribute(EHCACHE_MANAGER, cacheManager);

        logger.info("Initializing Shiro environment");
        WebEnvironment environment = environmentLoader.initEnvironment(servletContext);
        logger.debug("Publishing the Application Realm in the servlet context");
        RealmSecurityManager rsm = (RealmSecurityManager) environment.getWebSecurityManager();

        File pagesDirectory = new File(applicationDirectory, "pages");
        logger.info("Pages directory: " + pagesDirectory);
        ElementsFileUtils.ensureDirectoryExistsAndWarnIfNotWritable(pagesDirectory);

        if(configuration.getBoolean(PortofinoProperties.GROOVY_PRELOAD_PAGES, true)) {
            logger.info("Preloading pages");
            preloadPageActions(pagesDirectory);
        }
        if(configuration.getBoolean(PortofinoProperties.GROOVY_PRELOAD_CLASSES, true)) {
            logger.info("Preloading Groovy classes");
            preloadGroovyClasses(groovyClasspath);
        }
        servletContext.setAttribute(PAGES_DIRECTORY, pagesDirectory);

        logger.debug("Creating pageactions registry");
        PageActionRegistry pageActionRegistry = new PageActionRegistry();
        pageActionRegistry.register(CustomAction.class);
        pageActionRegistry.register(DefaultLoginAction.class);
        servletContext.setAttribute(PAGE_ACTIONS_REGISTRY, pageActionRegistry);

        servletContext.setAttribute(TEMPLATES_REGISTRY, new TemplateRegistry());

        cacheResetListenerRegistry.getCacheResetListeners().add(new ConfigurationCacheResetListener());

        SimpleMenuAppender link = SimpleMenuAppender.link(
                "configuration", "settings", null, "Settings", SettingsAction.URL_BINDING, 0.5);
        adminMenu.menuAppenders.add(link);

        logger.debug("Creating SecurityGroovyRealm");
        try {
            SecurityGroovyRealm realm = new SecurityGroovyRealm(classLoader, servletContext);
            LifecycleUtils.init(realm);
            rsm.setRealm(realm);
            status = ModuleStatus.ACTIVE;
        } catch (Exception  e) {
            logger.error("Security.groovy not found or invalid", e);
            status = ModuleStatus.FAILED;
        }
    }

    protected void preloadPageActions(File directory) {
        for(File file : directory.listFiles()) {
            logger.debug("visit {}", file);
            if(file.isDirectory()) {
                if(!file.equals(directory) && !file.equals(directory.getParentFile())) {
                    preloadPageActions(file);
                }
            } else if("action.groovy".equals(file.getName())) {
                logger.debug("Preloading page: {}", file);
                try {
                    Class<?> clazz = DispatcherLogic.getActionClass(configuration, directory);
                    clazz.newInstance();
                } catch(Throwable t) {
                    logger.warn("PageAction preload failed for page " + file.getAbsolutePath(), t);
                }
            }
        }
    }

    protected void preloadGroovyClasses(File directory) {
        preloadGroovyClasses(directory, "");
    }

    protected void preloadGroovyClasses(File directory, String pkg) {
        for(File file : directory.listFiles()) {
            logger.debug("visit {}", file);
            if(file.isDirectory()) {
                if(!file.equals(directory) && !file.equals(directory.getParentFile())) {
                    preloadGroovyClasses(file, pkg + file.getName() + ".");
                }
            } else {
                String name = file.getName();
                String className = pkg + name.substring(0, name.length() - ".groovy".length());
                logger.debug("Preloading " + className);
                try {
                    classLoader.loadClass(className, true, false, true);
                } catch(Throwable t) {
                    logger.warn("Groovy class preload failed for class " + className, t);
                }
            }
        }
    }

    @Override
    public void start() {
        status = ModuleStatus.STARTED;
    }

    @Override
    public void stop() {
        status = ModuleStatus.STOPPED;
    }

    @Override
    public void destroy() {
        logger.info("Destroying Shiro environment...");
        environmentLoader.destroyEnvironment(servletContext);
        logger.info("Shutting down cache...");
        cacheManager.shutdown();
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }

    private static class ConfigurationCacheResetListener implements CacheResetListener {
        @Override
        public void handleReset(CacheResetEvent e) {
            DispatcherLogic.clearConfigurationCache();
        }
    }
}
