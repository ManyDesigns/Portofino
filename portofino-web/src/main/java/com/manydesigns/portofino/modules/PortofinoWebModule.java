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

package com.manydesigns.portofino.modules;

import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.setup.MailQueueSetup;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.files.TempFileService;
import com.manydesigns.portofino.liquibase.LiquibaseUtils;
import com.manydesigns.portofino.servlets.MailScheduler;
import com.manydesigns.portofino.shiro.ApplicationRealm;
import com.manydesigns.portofino.starter.ApplicationStarter;
import net.sf.ehcache.CacheManager;
import ognl.OgnlRuntime;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.WebEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PortofinoWebModule implements Module {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Inject(ApplicationAttributes.SERVLET_CONTEXT)
    public ServletContext servletContext;

    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    protected ApplicationStarter applicationStarter;

    protected EnvironmentLoader environmentLoader = new EnvironmentLoader();

    protected CacheManager cacheManager;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(PortofinoWebModule.class);

    //**************************************************************************
    // ServletContextListener implementation
    //**************************************************************************

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

    @Override
    public String getModuleVersion() {
        return configuration.getString(PortofinoProperties.PORTOFINO_VERSION);
    }

    @Override
    public int getMigrationVersion() {
        return 1;
    }

    @Override
    public double getPriority() {
        return 1;
    }

    @Override
    public String getId() {
        return "portofino-web";
    }

    @Override
    public String getName() {
        return "Portofino Web";
    }

    @Override
    public int install() {
        return 1;
    }

    @Override
    public void init() {
                Configuration configuration =
                (Configuration) servletContext.getAttribute(ApplicationAttributes.PORTOFINO_CONFIGURATION);

        logger.debug("Initializing dispatcher");
        DispatcherLogic.init(configuration);

        LiquibaseUtils.setup();

        logger.debug("Setting up temporary file service");
        String tempFileServiceClass = configuration.getString(PortofinoProperties.TEMP_FILE_SERVICE_CLASS);
        try {
            TempFileService.setInstance((TempFileService) Class.forName(tempFileServiceClass).newInstance());
        } catch (Exception e) {
            logger.error("Could not set up temp file service", e);
            throw new Error(e);
        }

        setupMailQueue();

        //Disabilitazione security manager per funzionare su GAE. Il security manager permette di valutare
        //in sicurezza espressioni OGNL provenienti da fonti non sicure, configurando i necessari permessi
        //(invoke.<declaring-class>.<method-name>). In Portofino non permettiamo agli utenti finali di valutare
        //espressioni OGNL arbitrarie, pertanto il security manager può essere disabilitato in sicurezza.
        logger.info("Disabling OGNL security manager");
        OgnlRuntime.setSecurityManager(null);

        logger.info("Initializing ehcache service");
        cacheManager = CacheManager.newInstance();
        servletContext.setAttribute(ApplicationAttributes.EHCACHE_MANAGER, cacheManager);

        logger.info("Creating the application starter...");
        String appId = configuration.getString(PortofinoProperties.APP_ID);
        applicationStarter = new ApplicationStarter(servletContext, configuration, appId);
        servletContext.setAttribute(ApplicationAttributes.APPLICATION_STARTER, applicationStarter);

        logger.info("Initializing Shiro environment");
        WebEnvironment environment = environmentLoader.initEnvironment(servletContext);
        logger.debug("Publishing the Application Realm in the servlet context");
        RealmSecurityManager rsm = (RealmSecurityManager) environment.getWebSecurityManager();

        Realm realm = new ApplicationRealm(applicationStarter);
        LifecycleUtils.init(realm);
        rsm.setRealm(realm);

        status = ModuleStatus.INITIALIZED;
    }

    @Override
    public void destroy() {
        logger.info("ManyDesigns Portofino web module stopping..."); //TODO
        applicationStarter.destroy();
        logger.info("Destroying Shiro environment...");
        environmentLoader.destroyEnvironment(servletContext);
        logger.info("Shutting down cache...");
        cacheManager.shutdown();
        logger.info("ManyDesigns Portofino web module stopped.");
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
