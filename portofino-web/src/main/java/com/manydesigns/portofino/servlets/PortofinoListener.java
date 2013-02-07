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

package com.manydesigns.portofino.servlets;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.configuration.BeanLookup;
import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.setup.MailQueueSetup;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.files.TempFileService;
import com.manydesigns.portofino.liquibase.LiquibaseUtils;
import com.manydesigns.portofino.shiro.ApplicationRealm;
import com.manydesigns.portofino.starter.ApplicationStarter;
import net.sf.ehcache.CacheManager;
import ognl.OgnlRuntime;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.WebEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.nio.charset.Charset;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PortofinoListener
        implements ServletContextListener, HttpSessionListener {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

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

    protected EnvironmentLoader environmentLoader = new EnvironmentLoader();

    protected CacheManager cacheManager;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(PortofinoListener.class);

    //**************************************************************************
    // ServletContextListener implementation
    //**************************************************************************

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            init(servletContextEvent);
        } catch (Throwable e) {
            logger.error("Could not start ManyDesigns Portofino", e);
            throw new Error(e);
        }
    }

    private void init(ServletContextEvent servletContextEvent) {
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
        addConfiguration(PortofinoProperties.CUSTOM_PROPERTIES_RESOURCE);
        addConfiguration(PortofinoProperties.PROPERTIES_RESOURCE);
        servletContext.setAttribute(
                ApplicationAttributes.PORTOFINO_CONFIGURATION, portofinoConfiguration);

        logger.debug("Initializing dispatcher");
        DispatcherLogic.init(portofinoConfiguration);

        logger.debug("Setting up temporary file service");
        String tempFileServiceClass = portofinoConfiguration.getString(PortofinoProperties.TEMP_FILE_SERVICE_CLASS);
        try {
            TempFileService.setInstance((TempFileService) Class.forName(tempFileServiceClass).newInstance());
        } catch (Exception e) {
            logger.error("Could not set up temp file service", e);
            throw new Error(e);
        }

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
        
        Realm realm = new ApplicationRealm(applicationStarter);
        LifecycleUtils.init(realm);
        rsm.setRealm(realm);

        logger.info("Initializing ehcache service");
        cacheManager = CacheManager.newInstance();
        servletContext.setAttribute(ApplicationAttributes.EHCACHE_MANAGER, cacheManager);

        setupMailQueue();

        //Disabilitazione security manager per funzionare su GAE. Il security manager permette di valutare
        //in sicurezza espressioni OGNL provenienti da fonti non sicure, configurando i necessari permessi
        //(invoke.<declaring-class>.<method-name>). In Portofino non permettiamo agli utenti finali di valutare
        //espressioni OGNL arbitrarie, pertanto il security manager può essere disabilitato in sicurezza.
        logger.info("Disabling OGNL security manager");
        OgnlRuntime.setSecurityManager(null);

        String lineSeparator = System.getProperty("line.separator", "\n");
        logger.info(lineSeparator + SEPARATOR +
                lineSeparator + "--- ManyDesigns Portofino {} started successfully" +
                lineSeparator + "--- Context path: {}" +
                lineSeparator + "--- Real path: {}" +
                lineSeparator + SEPARATOR,
                new String[]{
                        portofinoConfiguration.getString(
                                PortofinoProperties.PORTOFINO_VERSION),
                        serverInfo.getContextPath(),
                        serverInfo.getRealPath()
                }
        );
    }

    protected void setupMailQueue() {
        MailQueueSetup mailQueueSetup = new MailQueueSetup();
        mailQueueSetup.setup();

        MailQueue mailQueue = mailQueueSetup.getMailQueue();
        if(mailQueue == null) {
            logger.info("Mail queue not enabled");
            return;
        }

        servletContext.setAttribute(ApplicationAttributes.MAIL_QUEUE, mailQueue);
        servletContext.setAttribute(ApplicationAttributes.MAIL_SENDER, mailQueueSetup.getMailSender());
        servletContext.setAttribute(ApplicationAttributes.MAIL_CONFIGURATION, mailQueueSetup.getMailConfiguration());

        try {
            //In classe separata per permettere al Listener di essere caricato anche in assenza di Quartz a runtime
            MailScheduler.setupMailScheduler(mailQueueSetup);
        } catch (NoClassDefFoundError e) {
            logger.debug(e.getMessage(), e);
            logger.info("Quartz is not available, mail scheduler not started");
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        MDC.clear();
        logger.info("ManyDesigns Portofino stopping...");
        applicationStarter.destroy();
        logger.info("Destroying Shiro environment...");
        environmentLoader.destroyEnvironment(servletContext);
        logger.info("Shutting down cache...");
        cacheManager.shutdown();
        logger.info("ManyDesigns Portofino stopped.");
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

    public void addConfiguration(String resource) {
        try {
            PropertiesConfiguration propertiesConfiguration =
                    new PropertiesConfiguration(resource);
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
