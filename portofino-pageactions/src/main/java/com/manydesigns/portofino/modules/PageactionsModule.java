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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.util.ElementsFileUtils;
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
import com.manydesigns.portofino.pageactions.login.OpenIdLoginAction;
import com.manydesigns.portofino.pageactions.registry.PageActionRegistry;
import com.manydesigns.portofino.pageactions.registry.TemplateRegistry;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.shiro.SecurityGroovyRealm;
import groovy.util.GroovyScriptEngine;
import net.sf.ehcache.CacheManager;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.WebEnvironment;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

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
    public ClassLoader originalClassLoader;

    @Inject(BaseModule.APP_LISTENERS)
    public List<ApplicationListener> applicationListeners;

    @Inject(BaseModule.CACHE_RESET_LISTENER_REGISTRY)
    public CacheResetListenerRegistry cacheResetListenerRegistry;

    protected EnvironmentLoader environmentLoader = new EnvironmentLoader();

    protected CacheManager cacheManager;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String GROOVY_SCRIPT_ENGINE = "GROOVY_SCRIPT_ENGINE";
    public static final String GROOVY_CLASS_PATH = "GROOVY_CLASS_PATH";
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
        return 1;
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

        logger.info("Initializing Groovy script engine");
        File groovyClasspath = new File(applicationDirectory, "groovy");
        ElementsFileUtils.ensureDirectoryExistsAndWarnIfNotWritable(groovyClasspath);

        servletContext.setAttribute(GROOVY_CLASS_PATH, groovyClasspath);
        GroovyScriptEngine groovyScriptEngine = createScriptEngine(groovyClasspath);
        servletContext.setAttribute(GROOVY_SCRIPT_ENGINE, groovyScriptEngine);

        File appListenerFile = new File(groovyClasspath, "AppListener.groovy");
        try {
            ElementsThreadLocals.setServletContext(servletContext); //Necessary for getGroovyObject
            Object listener = ScriptingUtil.getGroovyObject(appListenerFile);
            if(listener != null) {
                if(listener instanceof ApplicationListener) {
                    ApplicationListener applicationListener = (ApplicationListener) listener;
                    logger.info("Groovy application listener found at {}", appListenerFile.getAbsolutePath());
                    applicationListeners.add(applicationListener);
                } else {
                    logger.error(
                            "Candidate app listener " + listener + " found at " + appListenerFile.getAbsolutePath() +
                            " is not an instance of " + ApplicationListener.class);
                }
            } else {
                logger.debug("No Groovy app listener present");
            }
        } catch (Throwable e) {
            logger.error("Could not invoke app listener", e);
        }

        File pagesDirectory = new File(applicationDirectory, "pages");
        ElementsFileUtils.ensureDirectoryExistsAndWarnIfNotWritable(pagesDirectory);
        servletContext.setAttribute(PAGES_DIRECTORY, pagesDirectory);

        ClassLoader classLoader = groovyScriptEngine.getGroovyClassLoader();
        servletContext.setAttribute(BaseModule.CLASS_LOADER, classLoader);

        logger.debug("Creating pageactions registry");
        PageActionRegistry pageActionRegistry = new PageActionRegistry();
        pageActionRegistry.register(CustomAction.class);
        pageActionRegistry.register(DefaultLoginAction.class);
        pageActionRegistry.register(OpenIdLoginAction.class);
        servletContext.setAttribute(PAGE_ACTIONS_REGISTRY, pageActionRegistry);

        File scriptFile = new File(groovyClasspath, "Security.groovy");
        SecurityGroovyRealm realm = new SecurityGroovyRealm(groovyScriptEngine, scriptFile.toURI().toString());
        LifecycleUtils.init(realm);
        rsm.setRealm(realm);

        servletContext.setAttribute(TEMPLATES_REGISTRY, new TemplateRegistry());

        cacheResetListenerRegistry.getCacheResetListeners().add(new ConfigurationCacheResetListener());

        SimpleMenuAppender link = SimpleMenuAppender.link(
                "configuration", "settings", null, "Settings", SettingsAction.URL_BINDING, 0.5);
        adminMenu.menuAppenders.add(link);

        status = ModuleStatus.ACTIVE;
    }

    protected GroovyScriptEngine createScriptEngine(File classpathFile) {
        CompilerConfiguration cc = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        String classpath = classpathFile.getAbsolutePath();
        logger.info("Groovy classpath: " + classpath);
        cc.setClasspath(classpath);
        cc.setRecompileGroovySource(true);
        GroovyScriptEngine scriptEngine;
        try {
            scriptEngine =
                    new GroovyScriptEngine(new URL[] { classpathFile.toURI().toURL() },
                                           originalClassLoader);
        } catch (IOException e) {
            throw new Error(e);
        }
        scriptEngine.setConfig(cc);
        return scriptEngine;
    }

    @Override
    public void destroy() {
        logger.info("Destroying Shiro environment...");
        environmentLoader.destroyEnvironment(servletContext);
        logger.info("Shutting down cache...");
        cacheManager.shutdown();
        logger.info("Removing Groovy classloader...");
        servletContext.removeAttribute(GROOVY_SCRIPT_ENGINE);
        servletContext.removeAttribute(GROOVY_CLASS_PATH);
        servletContext.setAttribute(BaseModule.CLASS_LOADER, originalClassLoader);
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
