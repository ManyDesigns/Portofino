package com.manydesigns.portofino.dispatcher.web;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import com.manydesigns.portofino.dispatcher.Root;
import com.manydesigns.portofino.dispatcher.configuration.WritableCompositeConfiguration;
import com.manydesigns.portofino.dispatcher.resolvers.*;
import com.manydesigns.portofino.dispatcher.swagger.DocumentedApiRoot;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * Created by alessio on 28/07/16.
 */
public class DispatcherInitializer implements ServletContextListener {
    
    protected FileObject applicationRoot;
    protected Configuration configuration;
    private static final Logger logger = LoggerFactory.getLogger(DispatcherInitializer.class);

    public static final String CODE_BASE_ATTRIBUTE = "portofino.codebase";
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        initialize(sce.getServletContext());
    }

    public void initialize(ServletContext servletContext) {
        String applicationDirectoryPath = getApplicationDirectoryPath(servletContext);
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
                applicationRoot = VFS.getManager().toFileObject(new File(servletContext.getRealPath(""), "WEB-INF"));
            } catch (FileSystemException e) {
                initializationFailed(e);
            }
        }
        logger.info("Application directory: {}", applicationRoot);
        try {
            loadConfiguration(servletContext, applicationRoot);
        } catch (Exception e) {
            initializationFailed(e);
        }

        String actionsDirectory = configuration.getString("portofino.actions.path", "actions");
        initApplicationRoot(servletContext, actionsDirectory);
        logger.info("Application initialized.");
    }

    protected String getApplicationDirectoryPath(ServletContext servletContext) {
        return servletContext.getInitParameter("portofino.application.directory");
    }

    protected void loadConfiguration(ServletContext servletContext, FileObject applicationRoot)
            throws FileSystemException, ConfigurationException {
        FileObject configurationFile = applicationRoot.getChild("portofino.properties");
        WritableCompositeConfiguration compositeConfiguration = new WritableCompositeConfiguration();
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
            this.configuration = compositeConfiguration;
        } else {
            this.configuration = new PropertiesConfiguration();
            logger.warn("portofino.properties file not found in " + applicationRoot);
        }
        servletContext.setAttribute("portofino.configuration", this.configuration);
    }

    protected void initApplicationRoot(ServletContext servletContext, String actionsDirectoryName) {
        try {
            FileObject actionsDirectory = applicationRoot.getChild(actionsDirectoryName);
            if(actionsDirectory == null || actionsDirectory.getType() != FileType.FOLDER) {
                initializationFailed(new Exception("Not a directory: " + actionsDirectoryName));
            }
            CodeBase codeBase = createAndStoreCodeBase(servletContext);
            ResourceResolvers resourceResolver = new ResourceResolvers();
            configureResourceResolvers(resourceResolver, codeBase);
            DocumentedApiRoot.setRootFactory(() -> getRoot(actionsDirectory, resourceResolver));
        } catch (Exception e) {
            initializationFailed(e);
        }
    }

    protected CodeBase createAndStoreCodeBase(ServletContext servletContext) throws IOException {
        //TODO auto discovery?
        FileObject codeBaseRoot = getCodeBaseRoot();
        JavaCodeBase javaCodeBase = new JavaCodeBase(codeBaseRoot, null, getClass().getClassLoader());
        CodeBase codeBase = javaCodeBase;
        try {
            Class<?> gcb = Class.forName("com.manydesigns.portofino.code.GroovyCodeBase");
            Constructor<?> gcbConstructor = gcb.getConstructor(FileObject.class, CodeBase.class);
            codeBase = (CodeBase) gcbConstructor.newInstance(codeBaseRoot, javaCodeBase);
            logger.info("Groovy is available");
        } catch (Exception e) {
            logger.debug("Groovy not available", e);
        }
        servletContext.setAttribute(CODE_BASE_ATTRIBUTE, codeBase);
        return codeBase;
    }

    protected FileObject getCodeBaseRoot() throws FileSystemException {
        return VFS.getManager().resolveFile("res:");
    }

    protected void configureResourceResolvers(ResourceResolvers resourceResolver, CodeBase codeBase) {
        resourceResolver.resourceResolvers.add(new JavaResourceResolver(codeBase));
        addResourceResolver(resourceResolver, "com.manydesigns.portofino.dispatcher.resolvers.GroovyResourceResolver", codeBase, false);
        addResourceResolver(resourceResolver, "com.manydesigns.portofino.dispatcher.resolvers.JacksonResourceResolver", codeBase, true);
    }

    protected Root getRoot(FileObject actionsDirectory, ResourceResolvers resourceResolver) throws Exception {
        return Root.get(actionsDirectory, resourceResolver);
    }

    protected void addResourceResolver(ResourceResolvers resourceResolver, String className, CodeBase codeBase, boolean caching) {
        try {
            Class<?> resClass = Class.forName(className);
            ResourceResolver resolver;
            try {
                Constructor<?> resClassConstructor = resClass.getConstructor(CodeBase.class);
                resolver = (ResourceResolver) resClassConstructor.newInstance(codeBase);
            } catch (Exception e) {
                logger.debug("Constructor from CodeBase not available", e);
                resolver = (ResourceResolver) resClass.newInstance();
            }
            if(caching) {
                resolver = new CachingResourceResolver(resolver);
            }
            resourceResolver.resourceResolvers.add(resolver);
        } catch (Exception e) {
            logger.debug(className + " not available", e);
        }
    }

    protected void initializationFailed(Exception e) {
        logger.error("Could not initialize application", e);
        throw new RuntimeException(e);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Application destroyed.");
    }
}
