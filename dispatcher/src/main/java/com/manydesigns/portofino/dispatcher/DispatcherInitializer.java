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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.dispatcher.resolvers.CachingResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.JavaResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.ResourceResolvers;
import com.manydesigns.portofino.dispatcher.swagger.DocumentedApiRoot;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class DispatcherInitializer {
    protected FileObject applicationRoot;
    protected CompositeConfiguration configuration;
    protected CodeBase codeBase;
    protected ResourceResolver resourceResolver;

    private static final Logger logger = LoggerFactory.getLogger(DispatcherInitializer.class);

    public FileObject getApplicationRoot() {
        return applicationRoot;
    }

    public CompositeConfiguration getConfiguration() {
        return configuration;
    }

    public CodeBase getCodeBase() {
        return codeBase;
    }

    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    public void initialize() {
        String applicationDirectoryPath = getApplicationDirectoryPath();
        if(applicationDirectoryPath != null) try {
            //TODO allow placeholders?
            applicationRoot = VFS.getManager().resolveFile(applicationDirectoryPath);
            if(applicationRoot.getType() != FileType.FOLDER) {
                logger.error("Configured application directory " + applicationDirectoryPath + " is not a directory");
                applicationRoot = null;
            }
        } catch (FileSystemException e) {
            logger.error("Configured application directory " + applicationDirectoryPath + " is not valid", e);
        }
        if(applicationRoot == null) {
            try {
                applicationRoot = getDefaultApplicationRoot();
            } catch (FileSystemException e) {
                initializationFailed(e);
            }
        }
        logger.info("Application directory: {}", applicationRoot);
        try {
            loadConfiguration();
        } catch (Exception e) {
            initializationFailed(e);
        }

        String actionsDirectory = getConfiguration().getString("portofino.actions.path", "actions");
        initApplicationRoot(actionsDirectory);
        logger.info("Application initialized.");
    }

    protected abstract FileObject getDefaultApplicationRoot() throws FileSystemException;

    protected abstract String getApplicationDirectoryPath();

    protected void loadConfiguration()
            throws FileSystemException, ConfigurationException {
        FileObject configurationFile = applicationRoot.getChild("portofino.properties");
        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
        if(configurationFile != null) {
            Configurations configurations = new Configurations();
            PropertiesConfiguration configuration = configurations.properties(configurationFile.getURL());
    
            FileObject localConfigurationFile = applicationRoot.getChild("portofino-local.properties");
            if (localConfigurationFile != null && localConfigurationFile.exists() && localConfigurationFile.getType() == FileType.FILE) {
                logger.info("Local configuration found: {}", localConfigurationFile);
                PropertiesConfiguration localConfiguration = configurations.properties(localConfigurationFile.getURL());
                compositeConfiguration.addConfiguration(localConfiguration);
            }
            compositeConfiguration.addConfiguration(configuration);
        } else {
            compositeConfiguration.addConfiguration(new PropertiesConfiguration());
            logger.debug("portofino.properties file not found in " + applicationRoot);
        }
        this.configuration = compositeConfiguration;
    }

    protected void initApplicationRoot(String actionsDirectoryName) {
        try {
            FileObject actionsDirectory = applicationRoot.getChild(actionsDirectoryName);
            if(actionsDirectory == null || actionsDirectory.getType() != FileType.FOLDER) {
                initializationFailed(new Exception("Not a directory: " + actionsDirectoryName));
            }
            CodeBase codeBase = createCodeBase();
            ResourceResolvers resourceResolver = new ResourceResolvers();
            configureResourceResolvers(resourceResolver, codeBase);
            DocumentedApiRoot.setRootFactory(() -> getRoot(actionsDirectory, resourceResolver));
            this.codeBase = codeBase;
            this.resourceResolver = resourceResolver;
        } catch (Exception e) {
            initializationFailed(e);
        }
    }

    protected CodeBase createCodeBase() throws IOException {
        FileObject codeBaseRoot = getCodeBaseRoot();
        return new JavaCodeBase(codeBaseRoot, null, getClass().getClassLoader());
    }

    protected FileObject getCodeBaseRoot() throws FileSystemException {
        return VFS.getManager().resolveFile("res:com").getParent();
    }

    protected void configureResourceResolvers(ResourceResolvers resourceResolver, CodeBase codeBase) {
        resourceResolver.resourceResolvers.add(new JavaResourceResolver(codeBase));
        addResourceResolver(resourceResolver, "com.manydesigns.portofino.dispatcher.resolvers.JacksonResourceResolver", codeBase, true);
    }

    protected Resource getRoot(FileObject actionsDirectory, ResourceResolvers resourceResolver) throws Exception {
        return Root.get(actionsDirectory, resourceResolver);
    }

    protected void addResourceResolver(ResourceResolvers resourceResolver, String className, CodeBase codeBase, boolean caching) {
        try {
            Class<?> resClass = Class.forName(className);
            addResourceResolver(resourceResolver, resClass, codeBase, caching);
        } catch (Exception e) {
            logger.debug(className + " not available", e);
        }
    }

    protected void addResourceResolver(
            ResourceResolvers resourceResolver, Class<?> resClass, CodeBase codeBase, boolean caching
    ) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ResourceResolver resolver;
        try {
            Constructor<?> resClassConstructor = resClass.getConstructor(CodeBase.class);
            resolver = (ResourceResolver) resClassConstructor.newInstance(codeBase);
        } catch (Exception e) {
            logger.debug("Constructor from CodeBase not available", e);
            resolver = (ResourceResolver) resClass.getConstructor().newInstance();
        }
        if(caching) {
            resolver = new CachingResourceResolver(resolver);
        }
        resourceResolver.resourceResolvers.add(resolver);
    }

    protected void initializationFailed(Exception e) {
        logger.error("Could not initialize application", e);
        throw new RuntimeException(e);
    }
}
