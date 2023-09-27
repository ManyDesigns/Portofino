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

package com.manydesigns.portofino.servlets;

import com.manydesigns.elements.configuration.BeanLookup;
import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.portofino.code.AggregateCodeBase;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.CachingResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.ResourceResolvers;
import com.manydesigns.portofino.dispatcher.web.WebDispatcherInitializer;
import com.manydesigns.portofino.rest.PortofinoApplicationRoot;
import com.manydesigns.portofino.rest.PortofinoRoot;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PortofinoDispatcherInitializer extends WebDispatcherInitializer {
    public static final String copyright =
            "Copyright (C) 2005-2021 ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String PORTOFINO_CONFIGURATION_FILE_PROPERTY = "portofino.configuration.file";

    protected FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile;
    protected ServerInfo serverInfo;
    protected final Set<Class<? extends CodeBase>> codeBaseClasses;
    protected final Set<Class<? extends ResourceResolver>> resourceResolverClasses;

    public static final Logger logger =
            LoggerFactory.getLogger(PortofinoDispatcherInitializer.class);

    public PortofinoDispatcherInitializer(
            Set<Class<? extends CodeBase>> codeBaseClasses,
            Set<Class<? extends ResourceResolver>> resourceResolverClasses) {
        this.codeBaseClasses = codeBaseClasses;
        this.resourceResolverClasses = resourceResolverClasses;
    }

    //**************************************************************************
    // ServletContextListener implementation
    //**************************************************************************

    @Override
    public void initWithServletContext(ServletContext servletContext) {
        // clear the Mapping Diagnostic Context for logging
        MDC.clear();
        serverInfo = new ServerInfo(servletContext);
        super.initWithServletContext(servletContext);
        servletContext.setAttribute(PortofinoSpringConfiguration.APPLICATION_DIRECTORY, applicationRoot);
        servletContext.setAttribute(PortofinoSpringConfiguration.CONFIGURATION_SOURCE,
                new ConfigurationSource(configuration, configurationFile));

        logger.info("Servlet API version is " + serverInfo.getServletApiVersion());
        if (serverInfo.getServletApiMajor() < 3) {
            String msg = "Servlet API version should be >= 3.0.";
            logger.warn(msg);
        }
    }

    @Override
    protected String getApplicationDirectoryPath() {
        String applicationDirectoryPath = super.getApplicationDirectoryPath();
        if(applicationDirectoryPath != null) {
            ConfigurationInterpolator interpolator = new BaseConfiguration().getInterpolator();
            interpolator.registerLookups(getConfigurationLookups());
            return (String) interpolator.interpolate(applicationDirectoryPath);
        } else {
            return null;
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
    protected CodeBase createCodeBase() throws IOException {
        CodeBase codeBase = super.createCodeBase();
        for (Class<? extends CodeBase> c : codeBaseClasses) {
            if (c == JavaCodeBase.class || c == AggregateCodeBase.class) {
                continue;
            }
            try {
                Constructor<?> constructor = c.getConstructor(FileObject.class, CodeBase.class);
                codeBase = (CodeBase) constructor.newInstance(codeBase.getRoot(), codeBase);
                logger.info("Installed codebase " + c);
            } catch (Exception e) {
                logger.error("Could not install codebase " + c, e);
            }
        }
        return codeBase;
    }

    @Override
    protected void configureResourceResolvers(ResourceResolvers resourceResolver, CodeBase codeBase) {
        for (Class<?> c : resourceResolverClasses) {
            if (c == ResourceResolvers.class || c == CachingResourceResolver.class) {
                continue;
            }
            try {
                addResourceResolver(resourceResolver, c, codeBase, false);
            } catch (Exception e) {
                logger.error("Could not add resource resolver " + c);
            }
        }
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

    protected void loadConfiguration() throws ConfigurationException, FileSystemException {
        FileObject configurationFile = applicationRoot.resolveFile("portofino.properties");
        PropertiesBuilderParameters parameters =
                new Parameters()
                        .properties()
                        .setPrefixLookups(getConfigurationLookups())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        Configuration baseConf;
        if (configurationFile.exists()) {
            String path = configurationFile.getName().getURI();
            parameters.setFileName(path);
            this.configurationFile = new Configurations().propertiesBuilder(path).configure(parameters);
            baseConf = this.configurationFile.getConfiguration();
        } else {
            baseConf = new Configurations().propertiesBuilder(parameters).getConfiguration();
        }

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
            localConfigurationPath = baseConf.getString(
                    "portofino-local.properties",
                    applicationRoot.resolveFile("portofino-local.properties").getName().getURI());
        }

        CompositeConfiguration configuration = new CompositeConfiguration();

        FileObject localConfigurationFile = VFS.getManager().resolveFile(localConfigurationPath);
        if (localConfigurationFile.exists()) {
            logger.info("Local configuration file: {}", localConfigurationFile);
            parameters.setFileName(localConfigurationPath);
            this.configurationFile =
                    new Configurations().propertiesBuilder(localConfigurationPath).configure(parameters);
            PropertiesConfiguration localConfiguration = this.configurationFile.getConfiguration();

            configuration.setPrefixLookups(getConfigurationLookups());
            configuration.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
            //Note: order is important. The localConfiguration must be added here, and not in the constructor,
            //otherwise it is consulted last and not first.
            configuration.addConfiguration(localConfiguration, true);
        } else {
            logger.info("No local configuration found at {}", localConfigurationPath);
        }
        configuration.addConfiguration(baseConf);
        this.configuration = configuration;
    }

    public Map<String, Lookup> getConfigurationLookups() {
        Map<String, Lookup> lookupMap = new HashMap<>();
        lookupMap.put("serverInfo", new BeanLookup(serverInfo));
        lookupMap.put("applicationInfo", new BeanLookup(new ApplicationInfo()));
        return lookupMap;
    }

    public FileBasedConfigurationBuilder<PropertiesConfiguration> getConfigurationFile() {
        return configurationFile;
    }

    public class ApplicationInfo {
        public String getApplicationDirectory() {
            return applicationRoot.getName().getPath();
        }
    }

}
