/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import com.manydesigns.portofino.quartz.MailSenderJob;
import com.manydesigns.portofino.shiro.UsersGroupsDAO;
import com.manydesigns.portofino.starter.ApplicationStarter;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.WebEnvironment;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
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
import java.nio.charset.Charset;
import java.util.List;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PortofinoListener
        implements ServletContextListener, HttpSessionListener {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

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

    protected EnvironmentLoader environmentLoader = new EnvironmentLoader();

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

        LiquibaseUtils.setup();

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
        applicationStarter = new ApplicationStarter(servletContext, portofinoConfiguration);
        servletContext.setAttribute(
                ApplicationAttributes.APPLICATION_STARTER, applicationStarter);

        String encoding = portofinoConfiguration.getString(PortofinoProperties.URL_ENCODING);
        logger.info("URL character encoding is set to " + encoding + ". Make sure the web server uses the same encoding to parse URLs.");
        if(!Charset.isSupported(encoding)) {
            logger.error("The encoding is not supported by the JVM!");
        }
        if(!"UTF-8".equals(encoding)) {
            logger.warn("URL encoding is not UTF-8, but the Stripes framework always generates UTF-8 encoded URLs. URLs with non-ASCII characters may not work.");
        }

        logger.info("Initializing Shiro environment");
        WebEnvironment environment = environmentLoader.initEnvironment(servletContext);
        logger.debug("Publishing the Application Realm in the servlet context");
        RealmSecurityManager rsm = (RealmSecurityManager) environment.getWebSecurityManager();
        List classNames = portofinoConfiguration.getList(PortofinoProperties.SECURITY_REALM_CLASSES);
        for(Object className : classNames) {
            try {
                Class c = Class.forName(className.toString());
                Realm realm = (Realm) c.newInstance();
                LifecycleUtils.init(realm);
                rsm.setRealm(realm);
                if(realm instanceof UsersGroupsDAO) {
                    servletContext.setAttribute(ApplicationAttributes.USERS_GROUPS_DAO, realm);
                }
            } catch (Throwable t) {
                logger.error("Couldn't create security realm " + className, t);
            }
        }

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
        logger.info("Destroying Shiro environment");
        environmentLoader.destroyEnvironment(servletContext);
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
                boolean tls = portofinoConfiguration.getBoolean(
                        PortofinoProperties.MAIL_SMTP_TLS_ENABLED, false);
                String login = portofinoConfiguration.getString(
                        PortofinoProperties.MAIL_SMTP_LOGIN);
                String password = portofinoConfiguration.getString(
                        PortofinoProperties.MAIL_SMTP_PASSWORD);
                boolean keepSent = portofinoConfiguration.getBoolean(
                        PortofinoProperties.MAIL_KEEP_SENT, false);

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
                mailSender.setTls(tls);

                try {
                    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                    JobDetail job = JobBuilder
                            .newJob(MailSenderJob.class)
                            .withIdentity("mail.sender", "portofino")
                            .build();

                    int pollInterval = portofinoConfiguration.getInt(PortofinoProperties.MAIL_SENDER_POLL_INTERVAL);

                    Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("mail.sender.trigger", "portofino")
                        .startNow()
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMilliseconds(pollInterval)
                            .repeatForever())
                        .build();

                    scheduler.getContext().put(MailSenderJob.MAIL_SENDER_KEY, mailSender);
                    scheduler.scheduleJob(job, trigger);
                    servletContext.setAttribute(
                        ApplicationAttributes.MAIL_QUEUE, mailQueue);
                    logger.info("Mail sender started");
                } catch (SchedulerException e) {
                    logger.error("Couldn't start email task", e);
                }
            }
        }
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
