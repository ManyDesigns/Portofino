/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.servlets;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.blobs.SimpleBlobManager;
import com.manydesigns.elements.blobs.HierarchicalBlobManager;
import com.manydesigns.elements.configuration.BeanLookup;
import com.manydesigns.elements.servlet.AttributeMap;
import com.manydesigns.elements.servlet.ElementsFilter;
import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.i18n.ResourceBundleManager;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.modules.ModuleRegistry;
import com.manydesigns.portofino.stripes.ResolverUtil;
import groovy.util.GroovyScriptEngine;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Set;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PortofinoListener
        implements ServletContextListener, HttpSessionListener, ServletContextAttributeListener {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String SEPARATOR =
            "----------------------------------------" +
                    "----------------------------------------";
    
    public static final String PORTOFINO_MESSAGES_FILE_NAME = "portofino-messages.properties";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Configuration elementsConfiguration;
    protected Configuration configuration;

    protected File applicationDirectory;

    protected ServletContext servletContext;
    protected ServerInfo serverInfo;

    protected ModuleRegistry moduleRegistry;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(PortofinoListener.class);

    //**************************************************************************
    // ServletContextListener implementation
    //**************************************************************************

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            ElementsThreadLocals.setupDefaultElementsContext();
            ServletContext servletContext = servletContextEvent.getServletContext();
            ElementsThreadLocals.setServletContext(servletContext);
            AttributeMap servletContextAttributeMap = AttributeMap.createAttributeMap(servletContext);
            ElementsThreadLocals.getOgnlContext().put(
                    ElementsFilter.SERVLET_CONTEXT_OGNL_ATTRIBUTE,
                    servletContextAttributeMap);
            init(servletContextEvent);
        } catch (Throwable e) {
            logger.error("Could not start ManyDesigns Portofino", e);
            throw new Error(e);
        } finally {
            ElementsThreadLocals.removeElementsContext();
        }
    }

    private void init(ServletContextEvent servletContextEvent) {
        // clear the Mapping Diagnostic Context for logging
        MDC.clear();

        servletContext = servletContextEvent.getServletContext();

        serverInfo = new ServerInfo(servletContext);
        servletContext.setAttribute(BaseModule.SERVLET_CONTEXT, servletContext);
        servletContext.setAttribute(BaseModule.SERVER_INFO, serverInfo);

        setupCommonsConfiguration();

        elementsConfiguration = ElementsProperties.getConfiguration();
        servletContext.setAttribute(
                BaseModule.ELEMENTS_CONFIGURATION, elementsConfiguration);

        try {
            loadConfiguration();
        } catch (ConfigurationException e) {
            logger.error("Could not load configuration", e);
            throw new Error(e);
        }
        servletContext.setAttribute(BaseModule.APPLICATION_DIRECTORY, applicationDirectory);
        servletContext.setAttribute(BaseModule.PORTOFINO_CONFIGURATION, configuration);

        logger.debug("Setting blobs directory");
        File appBlobsDir;
        if(configuration.containsKey(PortofinoProperties.BLOBS_DIR_PATH)) {
            appBlobsDir = new File(configuration.getString(PortofinoProperties.BLOBS_DIR_PATH));
        } else {
            File appDir = (File) servletContext.getAttribute(BaseModule.APPLICATION_DIRECTORY);
            appBlobsDir = new File(appDir, "blobs");
        }
        logger.info("Blobs directory: " + appBlobsDir.getAbsolutePath());
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File tempBlobsDir = new File(tmpDir, "portofino-blobs" + servletContext.getContextPath().replace("/", "-"));
        logger.info("Temporary blobs directory: " + tempBlobsDir.getAbsolutePath());

        String metaFilenamePattern = "blob-{0}.properties";
        String dataFilenamePattern = "blob-{0}.data";
        BlobManager tempBlobManager = new HierarchicalBlobManager(tempBlobsDir, metaFilenamePattern, dataFilenamePattern);
        BlobManager defaultBlobManager;
        File[] blobs = appBlobsDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("blob-") && name.endsWith(".properties");
            }
        });
        if(blobs == null || blobs.length == 0) { //Null if the directory does not exist yet
            logger.info("Using new style (4.1.1+) hierarchical blob manager");
            defaultBlobManager = new HierarchicalBlobManager(appBlobsDir, metaFilenamePattern, dataFilenamePattern);
        } else {
            logger.info("Blobs found directly under the blobs directory; using old style (pre-4.1.1) flat file blob manager");
            defaultBlobManager = new SimpleBlobManager(appBlobsDir, metaFilenamePattern, dataFilenamePattern);
        }
        servletContext.setAttribute(BaseModule.TEMPORARY_BLOB_MANAGER, tempBlobManager);
        servletContext.setAttribute(BaseModule.DEFAULT_BLOB_MANAGER, defaultBlobManager);

        File groovyClasspath = new File(applicationDirectory, "groovy");
        logger.info("Initializing Groovy script engine with classpath: " + groovyClasspath.getAbsolutePath());
        ElementsFileUtils.ensureDirectoryExistsAndWarnIfNotWritable(groovyClasspath);

        logger.debug("Registering Groovy class loader");
        GroovyScriptEngine groovyScriptEngine = createScriptEngine(groovyClasspath);
        ClassLoader classLoader = groovyScriptEngine.getGroovyClassLoader();
        servletContext.setAttribute(BaseModule.GROOVY_CLASS_PATH, groovyClasspath);
        servletContext.setAttribute(BaseModule.CLASS_LOADER, classLoader);
        servletContext.setAttribute(BaseModule.GROOVY_SCRIPT_ENGINE, groovyScriptEngine);

        logger.debug("Installing I18n ResourceBundleManager");
        ResourceBundleManager resourceBundleManager = new ResourceBundleManager();
        try {
            Enumeration<URL> messagesSearchPaths = classLoader.getResources(PORTOFINO_MESSAGES_FILE_NAME);
            while (messagesSearchPaths.hasMoreElements()) {
                resourceBundleManager.addSearchPath(messagesSearchPaths.nextElement().toString());
            }
            File appMessages = new File(applicationDirectory, PORTOFINO_MESSAGES_FILE_NAME);
            resourceBundleManager.addSearchPath(appMessages.getAbsolutePath());
        } catch (IOException e) {
            logger.warn("Could not initialize resource bundle manager", e);
        }
        servletContext.setAttribute(BaseModule.RESOURCE_BUNDLE_MANAGER, resourceBundleManager);

        logger.info("Servlet API version is " + serverInfo.getServletApiVersion());
        if (serverInfo.getServletApiMajor() < 3) {
            String msg = "Servlet API version should be >= 3.0.";
            logger.warn(msg);
        }

        logger.info("Loading modules...");
        moduleRegistry = new ModuleRegistry(configuration);
        discoverModules(moduleRegistry, classLoader);
        servletContext.setAttribute(BaseModule.MODULE_REGISTRY, moduleRegistry);
        moduleRegistry.migrateAndInit(servletContext);
        logger.info("Starting modules...");
        moduleRegistry.start();
        logger.info("Modules initialization terminated.");

        String encoding = configuration.getString(
                PortofinoProperties.URL_ENCODING, PortofinoProperties.URL_ENCODING_DEFAULT);
        logger.info("URL character encoding is set to " + encoding + ". Make sure the web server uses the same encoding to parse URLs.");
        if(!Charset.isSupported(encoding)) {
            logger.error("The encoding is not supported by the JVM!");
        }
        if(!"UTF-8".equals(encoding)) {
            logger.warn("URL encoding is not UTF-8, but the Stripes framework always generates UTF-8 encoded URLs. URLs with non-ASCII characters may not work.");
        }

        String lineSeparator = System.getProperty("line.separator", "\n");
        logger.info(lineSeparator + SEPARATOR +
                lineSeparator + "--- ManyDesigns Portofino started successfully" +
                lineSeparator + "--- Context path: {}" +
                lineSeparator + "--- Real path: {}" +
                lineSeparator + SEPARATOR,
                new String[]{
                        serverInfo.getContextPath(),
                        serverInfo.getRealPath()
                }
        );
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
                                           getClass().getClassLoader());
        } catch (IOException e) {
            throw new Error(e);
        }
        scriptEngine.setConfig(cc);
        scriptEngine.getGroovyClassLoader().setShouldRecompile(true);
        return scriptEngine;
    }

    protected void discoverModules(ModuleRegistry moduleRegistry, ClassLoader classLoader) {
        ResolverUtil<Module> resolver = new ResolverUtil<Module>();
        resolver.setExtensions(".class", ".groovy");
        resolver.setClassLoader(classLoader);
        resolver.findImplementations(Module.class, Module.class.getPackage().getName());
        Set<Class<? extends Module>> classes = resolver.getClasses();
        classes.remove(Module.class);
        for(Class<? extends Module> moduleClass : classes) {
            try {
                logger.debug("Adding discovered module " + moduleClass);
                Module module = moduleClass.newInstance();
                moduleRegistry.getModules().add(module);
            } catch (Throwable e) {
                logger.error("Could not register module " + moduleClass, e);
            }
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        MDC.clear();
        logger.info("ManyDesigns Portofino stopping...");
        logger.info("Stopping modules...");
        moduleRegistry.stop();
        logger.info("Destroying modules...");
        moduleRegistry.destroy();
        logger.info("ManyDesigns Portofino stopped.");
    }

    //**************************************************************************
    // HttpSessionListener implementation
    //**************************************************************************

    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        logger.debug("Session created: id={}", session.getId());
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        logger.debug("Session destroyed: id={}", session.getId());
    }

    //**************************************************************************
    // Setup
    //**************************************************************************

    protected void loadConfiguration() throws ConfigurationException {
        applicationDirectory = new File(serverInfo.getRealPath(), "WEB-INF");
        logger.info("Application directory: {}", applicationDirectory.getAbsolutePath());

        File configurationFile = new File(applicationDirectory, "portofino.properties");
        configuration =  new PropertiesConfiguration(configurationFile);

        File localConfigurationFile =
                new File(applicationDirectory, "portofino-local.properties");
        if (localConfigurationFile.exists()) {
            logger.info("Local configuration found: {}", localConfigurationFile);
            PropertiesConfiguration localConfiguration =
                    new PropertiesConfiguration(localConfigurationFile);
            CompositeConfiguration compositeConfiguration =
                    new CompositeConfiguration();
            compositeConfiguration.addConfiguration(localConfiguration, true);
            compositeConfiguration.addConfiguration(configuration);
            configuration = compositeConfiguration;
        }
    }

    public void setupCommonsConfiguration() {
        logger.debug("Setting up commons-configuration lookups...");
        BeanLookup serverInfoLookup = new BeanLookup(serverInfo);
        ConfigurationInterpolator.registerGlobalLookup(
                "serverInfo",
                serverInfoLookup);
    }

    @Override
    public void attributeAdded(ServletContextAttributeEvent servletContextAttributeEvent) {
        logger.debug("Servlet context attribute added: " + servletContextAttributeEvent.getName() + " = " + servletContextAttributeEvent.getValue());
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent servletContextAttributeEvent) {
        logger.debug("Servlet context attribute removed: " + servletContextAttributeEvent.getName() + " = " + servletContextAttributeEvent.getValue());
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent servletContextAttributeEvent) {
        logger.debug("Servlet context attribute replaced: " + servletContextAttributeEvent.getName() + " = " + servletContextAttributeEvent.getValue());
    }
}
