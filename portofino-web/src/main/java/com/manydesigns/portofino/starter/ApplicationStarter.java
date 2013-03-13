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

package com.manydesigns.portofino.starter;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.application.AppProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.DefaultApplication;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.starter.migration.text.MigrateTo408;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ApplicationStarter {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public enum Status {
        UNINITIALIZED,
        INITIALIZING,
        INITIALIZED,
        DESTROYING,
        DESTROYED
    }

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected final Configuration portofinoConfiguration;
    protected CompositeConfiguration appConfiguration;

    protected Status status;

    protected DatabasePlatformsManager databasePlatformsManager;
    protected Application tmpApplication;
    protected Application application;
    protected String appId;

    protected File appDir;

    protected final ServletContext servletContext;
    protected ApplicationListener applicationListener;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    private final static Logger logger =
            LoggerFactory.getLogger(ApplicationStarter.class);

    //--------------------------------------------------------------------------
    // Contructor
    //--------------------------------------------------------------------------

    public ApplicationStarter(ServletContext servletContext, Configuration portofinoConfiguration) {
        this(servletContext, portofinoConfiguration, portofinoConfiguration.getString(PortofinoProperties.APP_ID));
    }

    public ApplicationStarter(ServletContext servletContext, Configuration portofinoConfiguration, String appId) {
        this.appId = appId;
        this.portofinoConfiguration = portofinoConfiguration;
        this.servletContext = servletContext;
        this.status = Status.UNINITIALIZED;

        String appsDirPath =
            portofinoConfiguration.getString(
                PortofinoProperties.APPS_DIR_PATH);
        File appsDir = new File(appsDirPath);
        logger.info("Apps dir: {}", appsDir.getAbsolutePath());
        logger.info("App id: {}", appId);
        appDir = new File(appsDir, appId);
        try {
            loadAppConfiguration();
            boolean initNow = appConfiguration.getBoolean(AppProperties.INIT_AT_STARTUP, false);
            if(initNow) {
                logger.info("Trying to initialize the application (init-at-startup = true)");
                logger.debug("Setting up default OGNL context");
                ElementsThreadLocals.setupDefaultElementsContext();
                initializeApplication();
                ElementsThreadLocals.removeElementsContext();
            }
        } catch (ConfigurationException e) {
            logger.error("Couldn't load app configuration", e);
        } catch (FileNotFoundException e) {
            logger.warn("Application configuration file not found", e);
        }
    }

    protected void loadAppConfiguration() throws ConfigurationException, FileNotFoundException {
        File appConfigurationFile =
                new File(appDir, AppProperties.PROPERTIES_RESOURCE);
        if(appConfigurationFile.exists()) {
            PropertiesConfiguration config = new PropertiesConfiguration(appConfigurationFile);
            appConfiguration = new CompositeConfiguration(config);
            appConfiguration.addConfiguration(config);
            appConfiguration.addConfiguration(new PropertiesConfiguration(getClass().getResource(AppProperties.PROPERTIES_DEFAULT_RESOURCE)));
        } else {
            throw new FileNotFoundException(appConfigurationFile.getAbsolutePath());
        }
    }

    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    public synchronized Application getApplication()
            throws UninitializedApplicationException, DestroyedApplicationException {
        if (status == Status.DESTROYED) {
            throw new DestroyedApplicationException();
        }
        if (status == Status.UNINITIALIZED) {
            logger.info("Trying to initilize application");
            try {
                status = Status.INITIALIZING;
                initializeApplication();
            } catch (Throwable e) {
                status = Status.UNINITIALIZED;
                logger.error("Failed to initilize application", e);
                throw new UninitializedApplicationException();
            }
        }
        if (status == Status.INITIALIZED) {
            return application;
        } else {
            throw new UninitializedApplicationException();
        }
    }

    public synchronized void destroy() {
        if (status != Status.INITIALIZED) {
            logger.info("Application not initialized. Nothing to destroy.");
            return;
        }

        status = Status.DESTROYING;

        if (application != null) {
            destroyApplication();
            application.shutdown();
        }

        status = Status.DESTROYED;
    }

    protected void destroyApplication() {
        if(applicationListener != null) {
            try {
                applicationListener.applicationDestroying(application, servletContext);
            } catch (Throwable t) {
                logger.error("Application listener threw an exception during shutdown", t);
            }
        }
        logger.info("Removing base classloader for application {}", application.getAppId());
        ScriptingUtil.removeBaseClassLoader(application);
    }

    public Status getStatus() {
        return status;
    }

    public Configuration getPortofinoConfiguration() {
        return portofinoConfiguration;
    }

    //--------------------------------------------------------------------------
    // Private methods
    //--------------------------------------------------------------------------


    public boolean initializeApplication() {
        tmpApplication = null;

        String appsDirPath =
        portofinoConfiguration.getString(PortofinoProperties.APPS_DIR_PATH);
        File appsDir = new File(appsDirPath);
        boolean success = ElementsFileUtils.ensureDirectoryExistsAndWarnIfNotWritable(appsDir);

        appDir = new File(appsDir, appId);
        logger.info("Application dir: {}", appDir.getAbsolutePath());
        success &= ElementsFileUtils.ensureDirectoryExistsAndWarnIfNotWritable(appDir);

        if(success) {
            try {
                loadAppConfiguration();
            } catch (Exception e) {
                success = false;
                logger.error("Couldn't load app configuration", e);
            }
        }

        if (success) {
            success = setupDatabasePlatformsManager();
        }

        if(success) {
            success = checkBlobsDirectory();
        }

        if (success) {
            success = setupApplication();
        }

        if (success) {
            application = tmpApplication;
            status = Status.INITIALIZED;
            logger.info("Application initialized successfully");
        } else {
            status = Status.UNINITIALIZED;
        }
        return success;
    }

    protected boolean checkBlobsDirectory() {
        File appBlobsDir;
        if(portofinoConfiguration.containsKey(PortofinoProperties.BLOBS_DIR_PATH)) {
            appBlobsDir = new File(portofinoConfiguration.getString(PortofinoProperties.BLOBS_DIR_PATH));
        } else {
            appBlobsDir = new File(appDir, "blobs");
        }
        logger.info("Application blobs dir: {}", appBlobsDir.getAbsolutePath());
        return ElementsFileUtils.ensureDirectoryExistsAndWarnIfNotWritable(appBlobsDir);
    }

    public boolean setupDatabasePlatformsManager() {
        logger.info("Creating database platform...");
        databasePlatformsManager =
                new DatabasePlatformsManager(portofinoConfiguration);
        return true;
    }

    public boolean setupApplication() {
        logger.info("Creating application instance...");
        try {
            tmpApplication = new DefaultApplication(appId,
                    portofinoConfiguration, appConfiguration, databasePlatformsManager,
                    appDir);
            tmpApplication.loadXmlModel();
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getRootCauseMessage(e), e);
            return false;
        }

        runMigrations(tmpApplication);

        logger.info("Initializing base classloader for application {}", tmpApplication.getAppId());
        ScriptingUtil.initBaseClassLoader(tmpApplication);
        File appListenerFile = new File(tmpApplication.getAppScriptsDir(), "AppListener.groovy");
        boolean success = true;
        try {
            Object o = ScriptingUtil.getGroovyObject(appListenerFile);
            if(o != null) {
                if(o instanceof ApplicationListener) {
                    applicationListener = (ApplicationListener) o;
                    logger.info("Invoking application listener defined in {}", appListenerFile.getAbsolutePath());
                    success = applicationListener.applicationStarting(tmpApplication, servletContext);
                } else {
                    logger.error("Candidate app listener " + o +
                                 " is not an instance of " + ApplicationListener.class);
                    success = false;
                }
            } else {
                logger.debug("No app listener present");
            }
        } catch (Throwable e) {
            logger.error("Could not invoke app listener", e);
            success = false;
        }
        if(!success) {
            //Clean up
            logger.info("Removing base classloader for application {}", tmpApplication.getAppId());
            ScriptingUtil.removeBaseClassLoader(tmpApplication);
        }
        return success;
    }

    protected void runMigrations(Application tmpApplication) {
        MigrateTo408.migrate(tmpApplication);
    }


}
