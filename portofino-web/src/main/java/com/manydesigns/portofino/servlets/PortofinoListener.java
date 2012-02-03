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
import com.manydesigns.elements.configuration.BeanLookup;
import com.manydesigns.mail.queue.FileSystemMailQueue;
import com.manydesigns.mail.queue.LockingMailQueue;
import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.sender.DefaultMailSender;
import com.manydesigns.mail.sender.MailSender;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.liquibase.LiquibaseUtils;
import com.manydesigns.portofino.starter.ApplicationStarter;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.exception.ExceptionUtils;
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

    /**
     * How often do we reload portofino-custom.properties
     */
    public static final int CONFIGURATION_REFRESH_DELAY = 2000;

    public static final String SEPARATOR =
            "----------------------------------------" +
                    "----------------------------------------";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Configuration elementsConfiguration;
    protected CompositeConfiguration portofinoConfiguration;

    protected ServletContext servletContext;
    protected ServerInfo serverInfo;

    protected ApplicationStarter applicationStarter;

    protected MailSender mailSender;
    protected Thread mailSenderThread;

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
        
        servletContext = servletContextEvent.getServletContext();

        serverInfo = new ServerInfo(servletContext);
        servletContext.setAttribute(ApplicationAttributes.SERVER_INFO, serverInfo);

        setupCommonsConfiguration();

        LiquibaseUtils.setupDatabaseFactory();
        LiquibaseUtils.setupSqlGeneratorFactory();

        elementsConfiguration = ElementsProperties.getConfiguration();
        servletContext.setAttribute(
                ApplicationAttributes.ELEMENTS_CONFIGURATION, elementsConfiguration);

        portofinoConfiguration = new CompositeConfiguration();
        addConfiguration(PortofinoProperties.CUSTOM_PROPERTIES_RESOURCE, true);
        addConfiguration(PortofinoProperties.PROPERTIES_RESOURCE, false);
        servletContext.setAttribute(
                ApplicationAttributes.PORTOFINO_CONFIGURATION, portofinoConfiguration);

        logger.info("Checking servlet API version...");
        if (serverInfo.getServletApiMajor() < 2 ||
                (serverInfo.getServletApiMajor() == 2 &&
                        serverInfo.getServletApiMinor() < 3)) {
            String msg = String.format(
                    "Servlet API version must be >= 2.3. Found: %s.",
                    serverInfo.getServletApiVersion());
            logger.error(msg);
            throw new InternalError(msg);
        }

        logger.info("Creating the application starter...");
        applicationStarter = new ApplicationStarter(portofinoConfiguration);
        servletContext.setAttribute(
                ApplicationAttributes.APPLICATION_STARTER, applicationStarter);

        setupEmailScheduler();

        String lineSeparator = System.getProperty("line.separator", "\n");
        logger.info(lineSeparator + SEPARATOR +
                lineSeparator + "--- ManyDesigns Portofino {} started successfully" +
                lineSeparator + "--- Context path: {}" +
                lineSeparator + "--- Real path: {}" +
                lineSeparator + SEPARATOR,
                new String[] {
                        portofinoConfiguration.getString(
                                PortofinoProperties.PORTOFINO_VERSION),
                        serverInfo.getContextPath(),
                        serverInfo.getRealPath()
                }
        );
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        MDC.clear();
        logger.info("ManyDesigns Portofino stopping...");
        applicationStarter.destroy();
        if (mailSender != null) {
            logger.info("Terminating the mail sender...");
            mailSender.stop();
            try {
                mailSenderThread.join();
            } catch (InterruptedException e) {
                logger.debug("Mail sender thread interrupted, not waiting", e);
            }
            logger.info("Mail sender terminated");
        }
        logger.info("ManyDesigns Portofino stopped.");
    }

    //**************************************************************************
    // Mail
    //**************************************************************************

    public void setupEmailScheduler() {
        String securityType = portofinoConfiguration
                .getString(PortofinoProperties.SECURITY_TYPE, "application");
        boolean mailEnabled = portofinoConfiguration.getBoolean(
                PortofinoProperties.MAIL_ENABLED, false);
        if ("application".equals(securityType) && mailEnabled) {
            String mailHost = portofinoConfiguration
                    .getString(PortofinoProperties.MAIL_SMTP_HOST);
            if (null == mailHost) {
                logger.error("Mail is enabled but smtp server not set in portofino-custom.properties");
            } else {
                logger.info("Mail is enabled, starting sender");
                int port = portofinoConfiguration.getInt(
                        PortofinoProperties.MAIL_SMTP_PORT, 25);
                boolean ssl = portofinoConfiguration.getBoolean(
                        PortofinoProperties.MAIL_SMTP_SSL_ENABLED, false);
                String login = portofinoConfiguration.getString(
                        PortofinoProperties.MAIL_SMTP_LOGIN);
                String password = portofinoConfiguration.getString(
                        PortofinoProperties.MAIL_SMTP_PASSWORD);
                boolean keepSent = portofinoConfiguration.getBoolean(
                        PortofinoProperties.KEEP_SENT, false);

                String mailQueueLocation =
                        portofinoConfiguration.getString(PortofinoProperties.MAIL_QUEUE_LOCATION);
                MailQueue mailQueue =
                        new LockingMailQueue(new FileSystemMailQueue(new File(mailQueueLocation)));
                logger.info("Mail queue location: {}", mailQueueLocation);
                mailQueue.setKeepSent(keepSent);
                mailSender = new DefaultMailSender(mailQueue);
                mailSender.setServer(mailHost);
                mailSender.setLogin(login);
                mailSender.setPassword(password);
                mailSender.setPort(port);
                mailSender.setSsl(ssl);
                mailSenderThread = new Thread(mailSender);
                mailSenderThread.setDaemon(true);
                mailSenderThread.start();
                servletContext.setAttribute(
                    ApplicationAttributes.MAIL_QUEUE, mailQueue);
                logger.info("Mail sender started");
            }
        }
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
    // Setup
    //**************************************************************************

    public void addConfiguration(String resource, boolean reloadable) {
        try {
            PropertiesConfiguration propertiesConfiguration =
                    new PropertiesConfiguration(resource);
            if (reloadable) {
                FileChangedReloadingStrategy reloadingStrategy =
                        new FileChangedReloadingStrategy();
                reloadingStrategy.setRefreshDelay(CONFIGURATION_REFRESH_DELAY);
                propertiesConfiguration.setReloadingStrategy(reloadingStrategy);
            }
            portofinoConfiguration.addConfiguration(propertiesConfiguration);
        } catch (Throwable e) {
            String errorMessage = ExceptionUtils.getRootCauseMessage(e);
            logger.warn(errorMessage);
            logger.debug("Error loading configuration", e);
        }
    }

    public void setupCommonsConfiguration() {
        logger.debug("Setting up commons-configuration lookups...");
        BeanLookup serverInfoLookup = new BeanLookup(serverInfo);
        ConfigurationInterpolator.registerGlobalLookup(
        ApplicationAttributes.SERVER_INFO,
        serverInfoLookup);
}
}
