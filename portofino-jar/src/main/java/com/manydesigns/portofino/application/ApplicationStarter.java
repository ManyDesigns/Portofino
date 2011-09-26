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

package com.manydesigns.portofino.application;

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.connections.ConnectionProvider;
import com.manydesigns.portofino.application.hibernate.HibernateApplicationImpl;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.email.EmailTask;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.util.List;
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
    private Application application;
    private Timer scheduler;

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
                if (initializeApplication()) {
                    status = Status.INITIALIZED;
                } else {
                    status = Status.UNINITIALIZED;
                    throw new UninitializedApplicationException();
                }
            } catch (Throwable e) {
                status = Status.UNINITIALIZED;
                logger.error("Failed to initilize application", e);
                throw new UninitializedApplicationException();
            }
        }
        if (status == Status.INITIALIZED) {
            return application;
        } else {
            logger.error("Illegal state (probably due to concurrency issues): {}", status);
            throw new InternalError("Status: " + status);
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


    private boolean initializeApplication() {
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
            logger.info("Application initialized successfully");
        }
        return success;
    }

    private boolean updateSystemDatabase() {
        ConnectionProvider connectionProvider = application.getConnectionProvider("portofino");
        Connection connection = null;
        try {
            connection = connectionProvider.acquireConnection();
            JdbcConnection jdbcConnection = new JdbcConnection(connection);
            Liquibase lq = new Liquibase(
                    "databases/portofino.default.changelog.xml",
                    new ClassLoaderResourceAccessor(),
                    jdbcConnection);
            lq.update(null);
            DatabaseFactory df = DatabaseFactory.getInstance();
            List<Database> implemented = df.getImplementedDatabases();
            Database database = df.findCorrectDatabaseImplementation(jdbcConnection);
            DatabaseSnapshotGeneratorFactory dsgf =
                    DatabaseSnapshotGeneratorFactory.getInstance();
            DatabaseSnapshot snapshot = dsgf.createSnapshot(database, "public", null);
            return true;
        } catch (Exception e) {
            logger.error("Couldn't update system database", e);
            return false;
        } finally {
            connectionProvider.releaseConnection(connection);
        }
    }

    private boolean setupDirectories() {
        String storageDirectory =
                portofinoConfiguration.getString(
                        PortofinoProperties.STORAGE_DIRECTORY);
        File file = new File(storageDirectory);
        if (file.exists()) {
            if (file.isDirectory()) {
                logger.info("Storage directory: {}", file);
            } else {
                logger.error("Storage location is not a directory: {}", file);
                return false;
            }
        } else {
            if (file.mkdirs()) {
                logger.info("Storage directory created successfully: {}", file);
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
        application = new HibernateApplicationImpl(
        portofinoConfiguration, databasePlatformsManager);

        try {
            String connectionsLocation =
                    portofinoConfiguration.getString(
                            PortofinoProperties.CONNECTIONS_LOCATION);
            String modelLocation =
                    portofinoConfiguration.getString(
                            PortofinoProperties.MODEL_LOCATION);

            File connectionsFile = new File(connectionsLocation);
            File modelFile = new File(modelLocation);

            application.loadConnections(connectionsFile);
            boolean success = updateSystemDatabase();
            if(!success) {
                return false;
            }
            application.loadXmlModel(modelFile);
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
                    scheduler.schedule(new EmailTask(application),
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
