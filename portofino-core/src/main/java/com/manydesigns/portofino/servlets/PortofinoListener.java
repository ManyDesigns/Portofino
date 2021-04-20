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

package com.manydesigns.portofino.servlets;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.configuration.BeanLookup;
import com.manydesigns.elements.servlet.AttributeMap;
import com.manydesigns.elements.servlet.ElementsFilter;
import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.dispatcher.resolvers.ResourceResolvers;
import com.manydesigns.portofino.dispatcher.web.DispatcherInitializer;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.rest.PortofinoApplicationRoot;
import com.manydesigns.portofino.rest.PortofinoRoot;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PortofinoListener extends DispatcherInitializer
        implements ServletContextListener, ServletContextAttributeListener {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String SEPARATOR =
            "----------------------------------------" +
            "----------------------------------------";
    public static final String PORTOFINO_CONFIGURATION_FILE_PROPERTY = "portofino.configuration.file";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile;

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

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            ElementsThreadLocals.setupDefaultElementsContext();
            ServletContext servletContext = servletContextEvent.getServletContext();
            initWithServletContext(servletContext);
        } catch (Throwable e) {
            logger.error("Could not start ManyDesigns Portofino", e);
            throw new Error(e);
        } finally {
            ElementsThreadLocals.removeElementsContext();
        }
    }

    public void initWithServletContext(ServletContext servletContext) {
        ElementsThreadLocals.setServletContext(servletContext);
        AttributeMap servletContextAttributeMap = AttributeMap.createAttributeMap(servletContext);
        ElementsThreadLocals.getOgnlContext().put(
                ElementsFilter.SERVLET_CONTEXT_OGNL_ATTRIBUTE,
                servletContextAttributeMap);
        ElementsThreadLocals.getOgnlContext().put(
                PORTOFINO_APPLICATION_DIRECTORY_PARAMETER,
                servletContext.getInitParameter(PORTOFINO_APPLICATION_DIRECTORY_PARAMETER));
        init(servletContext);
        String actionsDirectory = configuration.getString("portofino.actions.path", "actions");
        CodeBase codeBase = initApplicationRoot(actionsDirectory);
        servletContext.setAttribute(CODE_BASE_ATTRIBUTE, codeBase);

        String portofinoVersion = Module.getPortofinoVersion();
        String lineSeparator = System.getProperty("line.separator", "\n");
        logger.info(lineSeparator + SEPARATOR +
                        lineSeparator + "--- ManyDesigns Portofino " + portofinoVersion + " started successfully" +
                        lineSeparator + "--- Context path: {}" +
                        lineSeparator + "--- Real path: {}" +
                        lineSeparator + "--- Visit https://portofino.manydesigns.com for news, documentation, issue tracker, community forums, commercial support!" +
                        lineSeparator + SEPARATOR,
                serverInfo.getContextPath(), serverInfo.getRealPath());
    }

    private void init(ServletContext servletContext) {
        // clear the Mapping Diagnostic Context for logging
        MDC.clear();
        serverInfo = new ServerInfo(servletContext);

        String applicationDirectoryPath = servletContext.getInitParameter(PORTOFINO_APPLICATION_DIRECTORY_PARAMETER);
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
            if(!StringUtils.isEmpty(applicationDirectoryPath)) {
                applicationRoot = manager.resolveFile(applicationDirectoryPath);
                if (applicationRoot.getType() != FileType.FOLDER) {
                    logger.error("Configured application directory " + applicationDirectoryPath + " is not a directory");
                    applicationRoot = null;
                }
            }
        } catch (Exception e) {
            logger.error("Configured application directory " + applicationDirectoryPath + " is not valid", e);
        }
        if(applicationRoot == null) {
            try {
                logger.info("Using default application directory (WEB-INF)");
                applicationRoot = manager.resolveFile(serverInfo.getRealPath()).resolveFile("WEB-INF");
            } catch (FileSystemException e) {
                logger.error("Failed to obtain application real path", e);
                throw new RuntimeException(e);
            }
        }
        logger.info("Application directory: {}", applicationRoot.getName().getPath());

        try {
            loadConfiguration(servletContext);
        } catch (Exception e) {
            logger.error("Could not load configuration", e);
            throw new Error(e);
        }
        servletContext.setAttribute(PortofinoSpringConfiguration.APPLICATION_DIRECTORY, applicationRoot);
        servletContext.setAttribute(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION, configuration);
        servletContext.setAttribute(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION_FILE, configurationFile);

        logger.info("Servlet API version is " + serverInfo.getServletApiVersion());
        if (serverInfo.getServletApiMajor() < 3) {
            String msg = "Servlet API version should be >= 3.0.";
            logger.warn(msg);
        }
    }

    @Override
    protected FileObject getCodeBaseRoot() throws FileSystemException {
        FileObject codeBaseRoot = applicationRoot.resolveFile("classes");
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
    // Setup
    //**************************************************************************

    protected void loadConfiguration(ServletContext servletContext) throws ConfigurationException, FileSystemException {
        FileObject configurationFile = applicationRoot.resolveFile("portofino.properties");
        PropertiesBuilderParameters parameters =
                new Parameters()
                        .properties()
                        .setPrefixLookups(getConfigurationLookups())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        String path = configurationFile.getName().getURI();
        parameters.setFileName(path);
        this.configurationFile = new Configurations().propertiesBuilder(path).configure(parameters);
        configuration =  this.configurationFile.getConfiguration();
        servletContext.setAttribute(PortofinoApplicationRoot.PORTOFINO_CONFIGURATION_ATTRIBUTE, this.configuration);

        String localConfigurationPath = null;
        try {
            localConfigurationPath = System.getProperty(PORTOFINO_CONFIGURATION_FILE_PROPERTY);
        } catch (SecurityException e) {
            logger.warn("Reading system properties is forbidden. Will read configuration file from standard location.", e);
        }
        String localConfigurationFromDeploymentDescriptor = servletContext.getInitParameter(PORTOFINO_CONFIGURATION_FILE_PROPERTY);
        if(localConfigurationFromDeploymentDescriptor != null) {
            logger.debug("Read configuration file location from deployment descriptor");
            localConfigurationPath = localConfigurationFromDeploymentDescriptor;
        }
        if(localConfigurationPath == null) {
            localConfigurationPath = configuration.getString(
                    "portofino-local.properties",
                    applicationRoot.resolveFile("portofino-local.properties").getName().getURI());
        }
        FileObject localConfigurationFile = VFS.getManager().resolveFile(localConfigurationPath);
        if (localConfigurationFile.exists()) {
            logger.info("Local configuration file: {}", localConfigurationFile);
            parameters.setFileName(localConfigurationPath);
            this.configurationFile = new Configurations().propertiesBuilder(localConfigurationPath).configure(parameters);
            PropertiesConfiguration localConfiguration = this.configurationFile.getConfiguration();
            CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
            compositeConfiguration.setPrefixLookups(getConfigurationLookups());
            compositeConfiguration.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
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
