/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.portofino.starter;

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.DefaultApplication;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.email.EmailTask;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Timer;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ApplicationStarter {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String APP_BLOBS_DIR = "blobs";
    public static final String APP_CONNECTIONS_FILE = "portofino-connections.xml";
    public static final String APP_DBS_DIR = "dbs";
    public static final String APP_MODEL_FILE = "portofino-model.xml";
    public static final String APP_SCRIPTS_DIR = "scripts";
    public static final String APP_TEXT_DIR = "text";
    public static final String APP_STORAGE_DIR = "storage";
    public static final String APP_WEB_DIR = "web";

    public static final int PERIOD = 10000;
    public static final int DELAY2 = 5300;

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

    private final Configuration portofinoConfiguration;

    private Status status;

    private DatabasePlatformsManager databasePlatformsManager;
    private Application tmpApplication;
    private Application application;
    private Timer scheduler;
    private String appId;

    private File appDir;
    private File appBlobsDir;
    private File appConnectionsFile;
    private File appDbsDir;
    private File appModelFile;
    private File appScriptsDir;
    private File appTextDir;
    private File appStorageDir;
    private File appWebDir;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger =
            LoggerFactory.getLogger(ApplicationStarter.class);

    //--------------------------------------------------------------------------
    // Contructor
    //--------------------------------------------------------------------------

    public ApplicationStarter(Configuration portofinoConfiguration) {
        this.portofinoConfiguration = portofinoConfiguration;
        status = Status.UNINITIALIZED;
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
            application.shutdown();
        }

        if (scheduler!=null) {
            logger.info("Terminating the scheduler...");
            scheduler.cancel();
            EmailTask.stop();
        }

        status = Status.DESTROYED;
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
        return initializeApplication(
                portofinoConfiguration.getString(PortofinoProperties.APP_ID));
    }

    public boolean initializeApplication(String appId) {
        tmpApplication = null;

        this.appId = appId;
        logger.info("Application id: {}", appId);

        boolean success = setupDirectories();

        if (success) {
            success = setupDatabasePlatformsManager();
        }

        if (success) {
            success = setupApplication();
        }

        if (success) {
            success = setupEmailScheduler();
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

    private boolean setupDirectories() {
        String appsDirPath =
        portofinoConfiguration.getString(
                PortofinoProperties.APPS_DIR_PATH);
        File appsDir = new File(appsDirPath);
        logger.info("Apps dir: {}", appsDir.getAbsolutePath());
        boolean result = ensureDirectoryExistsAndWritable(appsDir);

        appDir = new File(appsDir, appId);
        logger.info("Application dir: {}", appDir.getAbsolutePath());
        result = result && ensureDirectoryExistsAndWritable(appsDir);

        appBlobsDir = new File(appDir, APP_BLOBS_DIR);
        logger.info("Application blobs dir: {}", appBlobsDir.getAbsolutePath());
        result = result && ensureDirectoryExistsAndWritable(appBlobsDir);

        appConnectionsFile = new File(appDir, APP_CONNECTIONS_FILE);
        logger.info("Application connections file: {}", appConnectionsFile.getAbsolutePath());

        appDbsDir = new File(appDir, APP_DBS_DIR);
        logger.info("Application dbs dir: {}", appDbsDir.getAbsolutePath());
        result = result && ensureDirectoryExistsAndWritable(appDbsDir);

        appModelFile = new File(appDir, APP_MODEL_FILE);
        logger.info("Application model file: {}", appModelFile.getAbsolutePath());

        appScriptsDir = new File(appDir, APP_SCRIPTS_DIR);
        logger.info("Application scripts dir: {}", appScriptsDir.getAbsolutePath());
        result = result && ensureDirectoryExistsAndWritable(appScriptsDir);

        appTextDir = new File(appDir, APP_TEXT_DIR);
        logger.info("Application text dir: {}", appTextDir.getAbsolutePath());
        result = result && ensureDirectoryExistsAndWritable(appTextDir);

        appStorageDir = new File(appDir, APP_STORAGE_DIR);
        logger.info("Application storage dir: {}", appStorageDir.getAbsolutePath());
        result = result && ensureDirectoryExistsAndWritable(appStorageDir);

        appWebDir = new File(appDir, APP_WEB_DIR);
        logger.info("Application web dir: {}", appWebDir.getAbsolutePath());
        result = result && ensureDirectoryExistsAndWritable(appWebDir);

        return result;
    }

    private boolean ensureDirectoryExistsAndWritable(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                logger.debug("Storage directory: {}", file);
            } else {
                logger.error("Storage location is not a directory: {}", file);
                return false;
            }
        } else {
            if (file.mkdirs()) {
                logger.info("Directory created successfully: {}", file);
            } else {
                logger.error("Cannot create storage directory: {}", file);
                return false;
            }
        }
        if (!file.canWrite()) {
            logger.warn("Cannot write to storage directory: {}", file);
        }
        return true;
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
                    portofinoConfiguration, databasePlatformsManager,
                    appDir, appBlobsDir, appConnectionsFile, appDbsDir,
                    appModelFile, appScriptsDir, appTextDir, appStorageDir, appWebDir);
            tmpApplication.loadXmlModel();
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getRootCauseMessage(e), e);
            return false;
        }
        return true;
    }

    public boolean setupEmailScheduler() {
        String securityType = portofinoConfiguration
                .getString(PortofinoProperties.SECURITY_TYPE, "application");
        boolean mailEnabled = portofinoConfiguration.getBoolean(
                PortofinoProperties.MAIL_ENABLED, false);
        if ("application".equals(securityType) && mailEnabled) {
            String mailHost = portofinoConfiguration
                    .getString(PortofinoProperties.MAIL_SMTP_HOST);
            String mailSender = portofinoConfiguration
                    .getString(PortofinoProperties.MAIL_SMTP_SENDER);
            if (null == mailSender || null == mailHost ) {
                logger.info("User admin email or smtp server not set in" +
                        " portofino-custom.properties");
            } else {
                scheduler = new Timer(true);
                try {

                    //Invio mail
                    scheduler.schedule(new EmailTask(tmpApplication),
                            DELAY2, PERIOD);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Problems in starting schedulers", e);
                }
            }
        }
        return true;
    }


}
