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

package com.manydesigns.portofino.servlets;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.configuration.BeanLookup;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.context.ServerInfo;
import com.manydesigns.portofino.context.hibernate.HibernateApplicationImpl;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.dispatcher.Dispatcher;
import com.manydesigns.portofino.email.EmailTask;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.File;
import java.util.Timer;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PortofinoListener
        implements ServletContextListener, HttpSessionListener {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String SEPARATOR =
            "----------------------------------------" +
            "----------------------------------------";
    public static final int PERIOD = 10000;
    public static final int DELAY = 5000;
    public static final int DELAY2 = 5300;

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Configuration elementsConfiguration;
    protected CompositeConfiguration portofinoConfiguration;

    protected ServletContext servletContext;
    protected ServerInfo serverInfo;

    protected DatabasePlatformsManager databasePlatformsManager;
    protected Application application;
    protected Dispatcher dispatcher;
    protected Timer scheduler;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(PortofinoListener.class);

    //**************************************************************************
    // ServletContextListener implementation
    //**************************************************************************

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        // clear the Mapping Diagnostic Context for logging
        MDC.clear();
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        servletContext = servletContextEvent.getServletContext();

        serverInfo = new ServerInfo(servletContext);
        servletContext.setAttribute(ApplicationAttributes.SERVER_INFO, serverInfo);

        boolean success = setupCommonsConfiguration();

        elementsConfiguration = ElementsProperties.getConfiguration();
        servletContext.setAttribute(
                ApplicationAttributes.ELEMENTS_CONFIGURATION, elementsConfiguration);

        portofinoConfiguration = new CompositeConfiguration();
        addConfiguration(PortofinoProperties.CUSTOM_PROPERTIES_RESOURCE);
        addConfiguration(PortofinoProperties.PROPERTIES_RESOURCE);
        servletContext.setAttribute(
                ApplicationAttributes.PORTOFINO_CONFIGURATION, portofinoConfiguration);

        logger.info("\n" + SEPARATOR +
                "\n--- ManyDesigns Portofino {} starting..." +
                "\n--- Context path: {}" +
                "\n--- Real path: {}" +
                "\n" + SEPARATOR,
                new String[] {
                        portofinoConfiguration.getString(
                                PortofinoProperties.PORTOFINO_VERSION),
                        serverInfo.getContextPath(),
                        serverInfo.getRealPath()
                }
        );

        // check servlet API version
        if (serverInfo.getServletApiMajor() < 2 ||
                (serverInfo.getServletApiMajor() == 2 &&
                        serverInfo.getServletApiMinor() < 3)) {
            logger.error("Servlet API version must be >= 2.3. Found: {}.",
                    serverInfo.getServletApiVersion());
            success = false;
        }

        if (success) {
            success = setupDirectories();
        }

        if (success) {
            success = setupDatabasePlatformsManager();
        }

        if (success) {
            success = setupApplication();
            servletContext.setAttribute(
                    ApplicationAttributes.APPLICATION, application);
        }

        if (success) {
            success = setupDispatcher();
            servletContext.setAttribute(
                    ApplicationAttributes.DISPATCHER, dispatcher);
        }

        if (success) {
            success = setupEmailScheduler();
        }

        stopWatch.stop();
        if (success) {
            logger.info("ManyDesigns Portofino successfully started in {} ms.",
                    stopWatch.getTime());
        } else {
            logger.error("Failed to start ManyDesigns Portofino.");
        }
    }

    public void addConfiguration(String resource) {
        try {
            portofinoConfiguration.addConfiguration(
                    new PropertiesConfiguration(resource));
        } catch (Throwable e) {
            String errorMessage = ExceptionUtils.getRootCauseMessage(e);
            logger.warn(errorMessage);
            logger.debug("Error loading configuration", e);
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // clear the Mapping Diagnostic Context for logging
        MDC.clear();

        logger.info("ManyDesigns Portofino stopping...");

        if (scheduler!=null) {
            logger.info("Terminating the scheduler...");
            scheduler.cancel();
            EmailTask.stop();
        }

        ElementsThreadLocals.destroy();

        logger.info("ManyDesigns Portofino stopped.");
    }

    //**************************************************************************
    // HttpSessionListener implementation
    //**************************************************************************

    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        logger.info("Session created: id={}", session.getId());
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        logger.info("Session destroyed: id={}", session.getId());
    }

    //**************************************************************************
    // Overridable setups
    //**************************************************************************

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
            ElementsThreadLocals.setupDefaultElementsContext();


            String connectionsLocation =
                    portofinoConfiguration.getString(
                            PortofinoProperties.CONNECTIONS_LOCATION);
            String modelLocation =
                    portofinoConfiguration.getString(
                            PortofinoProperties.MODEL_LOCATION);

            File connectionsFile = new File(connectionsLocation);
            File modelFile = new File(modelLocation);

            application.loadConnections(connectionsFile);
            application.loadXmlModel(modelFile);
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getRootCauseMessage(e), e);
            return false;
        } finally {
            ElementsThreadLocals.removeElementsContext();
        }
        return true;
    }

    public boolean setupDispatcher() {
        dispatcher = new Dispatcher(application);
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

    public boolean setupCommonsConfiguration() {
        logger.info("Setting up commons-configuration lookups...");
        BeanLookup serverInfoLookup = new BeanLookup(serverInfo);
        ConfigurationInterpolator.registerGlobalLookup(
                ApplicationAttributes.SERVER_INFO,
                serverInfoLookup);
        return true;
    }

    public boolean setupDirectories() {
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
}
