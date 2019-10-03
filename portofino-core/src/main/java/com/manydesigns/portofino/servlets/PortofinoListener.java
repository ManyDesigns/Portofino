/*
 * Copyright (C) 2005-2019 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.elements.configuration.BeanLookup;
import com.manydesigns.elements.crypto.KeyManager;
import com.manydesigns.elements.servlet.AttributeMap;
import com.manydesigns.elements.servlet.ElementsFilter;
import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.dispatcher.resolvers.ResourceResolvers;
import com.manydesigns.portofino.dispatcher.web.DispatcherInitializer;
import com.manydesigns.portofino.i18n.I18nUtils;
import com.manydesigns.portofino.rest.PortofinoRoot;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.vfs2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PortofinoListener extends DispatcherInitializer
        implements ServletContextListener, HttpSessionListener, ServletContextAttributeListener {
    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String SEPARATOR =
            "----------------------------------------" +
            "----------------------------------------";
    
    public final static String SERVER_INFO = "com.manydesigns.portofino.serverInfo";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Configuration elementsConfiguration;
    protected Configuration configuration;
    protected FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile;

    protected FileObject applicationDirectory;

    protected ServletContext servletContext;
    protected ServerInfo serverInfo;

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
            super.contextInitialized(servletContextEvent);

            String portofinoVersion = PortofinoProperties.getPortofinoVersion();
            String lineSeparator = System.getProperty("line.separator", "\n");
            logger.info(lineSeparator + SEPARATOR +
                            lineSeparator + "--- ManyDesigns Portofino " + portofinoVersion + " started successfully" +
                            lineSeparator + "--- Context path: {}" +
                            lineSeparator + "--- Real path: {}" +
                            lineSeparator + "--- Visit http://portofino.manydesigns.com for news, documentation, issue tracker, community forums, commercial support!" +
                            lineSeparator + SEPARATOR,
                    serverInfo.getContextPath(), serverInfo.getRealPath());

            String versionCheckUrl = configuration.getString(
                    "portofino.version.check.url",
                    "https://portofino.manydesigns.com/version-check.jsp");
            if(!"off".equalsIgnoreCase(versionCheckUrl)) {
                try {
                    checkForNewVersion(portofinoVersion, versionCheckUrl);
                } catch (Throwable t) {
                    logger.warn("Version check failed unexpectedly", t);
                }
            }
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
        servletContext.setAttribute(SERVER_INFO, serverInfo);

        elementsConfiguration = ElementsProperties.getConfiguration();

        String applicationDirectoryPath = servletContext.getInitParameter("portofino.application.directory");
        FileSystemManager manager;
        try {
            manager = VFS.getManager();
        } catch (FileSystemException e) {
            logger.error("Failed to obtain VFS manager", e);
            throw new RuntimeException(e);
        }
        if(applicationDirectoryPath != null) try {
            ConfigurationInterpolator interpolator = new BaseConfiguration().getInterpolator();
            interpolator.registerLookups(getConfigurationLookups());
            applicationDirectoryPath = (String) interpolator.interpolate(applicationDirectoryPath);
            applicationDirectory = manager.resolveFile(applicationDirectoryPath);
            if(applicationDirectory.getType() != FileType.FOLDER) {
                logger.error("Configured application directory " + applicationDirectoryPath + " is not a directory");
                applicationDirectory = null;
            }
        } catch (Exception e) {
            logger.error("Configured application directory " + applicationDirectoryPath + " is not valid", e);
        }
        if(applicationDirectory == null) {
            try {
                applicationDirectory = manager.resolveFile(serverInfo.getRealPath()).resolveFile("WEB-INF");
            } catch (FileSystemException e) {
                logger.error("Failed to obtain application real path", e);
                throw new RuntimeException(e);
            }
        }
        logger.info("Application directory: {}", applicationDirectory.getName().getPath());

        try {
            loadConfiguration();
        } catch (Exception e) {
            logger.error("Could not load configuration", e);
            throw new Error(e);
        }
        servletContext.setAttribute(PortofinoSpringConfiguration.APPLICATION_DIRECTORY, applicationDirectory);
        servletContext.setAttribute(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION, configuration);
        servletContext.setAttribute(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION_FILE, configurationFile);

        try {
            logger.info("Initializing KeyManager ");
            KeyManager.init(configuration);
        } catch (Exception e) {
            logger.error("Could not initialize KeyManager", e);
        }

        I18nUtils.setupResourceBundleManager(applicationDirectory, servletContext);

        logger.info("Servlet API version is " + serverInfo.getServletApiVersion());
        if (serverInfo.getServletApiMajor() < 3) {
            String msg = "Servlet API version should be >= 3.0.";
            logger.warn(msg);
        }

        String encoding = configuration.getString(
                PortofinoProperties.URL_ENCODING, PortofinoProperties.URL_ENCODING_DEFAULT);
        logger.info("URL character encoding is set to " + encoding + ". Make sure the web server uses the same encoding to parse URLs.");
        if(!Charset.isSupported(encoding)) {
            logger.error("The encoding is not supported by the JVM!");
        }
    }

    protected void checkForNewVersion(String portofinoVersion, String versionCheckUrl) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(versionCheckUrl)
                .queryParam("version", portofinoVersion);
        Future<Response> responseFuture = target.request().async().get();
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Response response = responseFuture.get();
                if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                    String latestVersion = response.readEntity(String.class).trim();
                    if (Objects.equals(portofinoVersion, latestVersion)) {
                        logger.info("Your installation of Portofino is up-to-date");
                    } else {
                        String lineSeparator = System.getProperty("line.separator", "\n");
                        logger.info(lineSeparator + SEPARATOR + lineSeparator +
                                "A new version of Portofino is available: " + latestVersion +
                                lineSeparator + SEPARATOR);
                    }
                } else {
                    logger.info("Version check failed: " + response.getStatus());
                }
                String message = response.getHeaderString("X-Message");
                if (message != null) {
                    logger.info(message);
                }
            } catch (Exception e) {
                logger.info("Could not check for new version: " + e.getMessage());
                logger.debug("Additional information", e);
            }
        });
    }

    @Override
    protected String getApplicationDirectoryPath(ServletContext servletContext) {
        return applicationDirectory.getName().getPath();
    }

    @Override
    protected FileObject getCodeBaseRoot() throws FileSystemException {
        FileObject codeBaseRoot = applicationDirectory.resolveFile("classes");
        String classpath = codeBaseRoot.getName().getPath();
        logger.info("Initializing codebase with classpath: " + classpath);
        ElementsFileUtils.ensureDirectoryExistsAndWarnIfNotWritable(new File(classpath));
        return codeBaseRoot;
    }

    @Override
    protected PortofinoRoot getRoot(FileObject actionsDirectory, ResourceResolvers resourceResolver) throws Exception {
        return PortofinoRoot.get(actionsDirectory, resourceResolver);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        MDC.clear();
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

    protected void loadConfiguration() throws ConfigurationException, FileSystemException {
        FileObject configurationFile = applicationDirectory.resolveFile("portofino.properties");
        PropertiesBuilderParameters parameters =
                new Parameters()
                        .properties()
                        .setPrefixLookups(getConfigurationLookups())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        String path = configurationFile.getName().getPath();
        parameters.setFileName(path);
        this.configurationFile = new Configurations().propertiesBuilder(path).configure(parameters);
        configuration =  this.configurationFile.getConfiguration();

        String localConfigurationPath = null;
        try {
            localConfigurationPath = System.getProperty("portofino.configuration.file");
        } catch (SecurityException e) {
            logger.warn("Reading system properties is forbidden. Will read configuration file from standard location.", e);
        }
        if(localConfigurationPath == null) {
            localConfigurationPath = configuration.getString(
                    "portofino-local.properties",
                    applicationDirectory.resolveFile("portofino-local.properties").getName().getPath());
        }
        FileObject localConfigurationFile = VFS.getManager().resolveFile(localConfigurationPath);
        if (localConfigurationFile.exists()) {
            logger.info("Local configuration file: {}", localConfigurationFile);
            parameters.setFileName(localConfigurationPath);
            this.configurationFile = new Configurations().propertiesBuilder(localConfigurationPath).configure(parameters);
            PropertiesConfiguration localConfiguration = this.configurationFile.getConfiguration();
            CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
            //Note: order is important. The localConfiguration must be added here, and not in the constructor,
            //otherwise it is consulted last and not first.
            compositeConfiguration.addConfiguration(localConfiguration, true);
            compositeConfiguration.addConfiguration(configuration);
            configuration = compositeConfiguration;
        } else {
            logger.info("No local configuration found at {}", localConfigurationPath);
        }
    }

    public Map<String, Lookup> getConfigurationLookups() {
        Map<String, Lookup> lookupMap = new HashMap<>();
        lookupMap.put("serverInfo", new BeanLookup(serverInfo));
        return lookupMap;
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
