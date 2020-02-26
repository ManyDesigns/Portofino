/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.quartz.PortofinoJobFactory;
import org.apache.commons.configuration2.Configuration;
import org.quartz.*;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import java.util.Date;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class QuartzModule implements Module, ApplicationContextAware {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Autowired
    public Configuration configuration;

    @Autowired
    public ServletContext servletContext;

    protected ApplicationContext applicationContext;

    protected Scheduler scheduler;

    protected boolean startOnLoad;
    protected boolean waitOnShutdown;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(QuartzModule.class);

    @Override
    public String getModuleVersion() {
        return PortofinoProperties.getPortofinoVersion();
    }

    @Override
    public String getName() {
        return "Quartz";
    }

    @PostConstruct
    public void init() {
        StdSchedulerFactory factory;
        try {
            String configFile = configuration.getString("quartz.config-file");
            startOnLoad = configuration.getBoolean("quartz.start-on-load", true);
            waitOnShutdown = configuration.getBoolean("quartz.wait-on-shutdown", true);
            factory = getSchedulerFactory(configFile);

            // Always want to get the scheduler, even if it isn't starting,
            // to make sure it is both initialized and registered.
            scheduler = factory.getScheduler();
            scheduler.setJobFactory(new PortofinoJobFactory(applicationContext));

            String factoryKey = configuration.getString("quartz.servlet-context-factory-key");
            if (factoryKey == null) {
                factoryKey = QuartzInitializerListener.QUARTZ_FACTORY_KEY;
            }

            logger.info("Storing the Quartz Scheduler Factory in the servlet context at key: "
                    + factoryKey);
            servletContext.setAttribute(factoryKey, factory);

            String servletCtxtKey = configuration.getString("quartz.scheduler-context-servlet-context-key");
            if (servletCtxtKey != null) {
                logger.info("Storing the ServletContext in the scheduler context at key: "
                        + servletCtxtKey);
                scheduler.getContext().put(servletCtxtKey, servletContext);
            }

            if(startOnLoad) {
                scheduler.start();
            }

            try {
                int seconds = configuration.getInt("database.failed.connections.retry.every.seconds", 60);
                if(seconds > 0) {
                    Class jobClass = Class.forName("com.manydesigns.portofino.quartz.FailedDatabaseConnectionRetryJob");
                    JobDetail job = JobBuilder.newJob(jobClass).build();

                    Trigger trigger = TriggerBuilder.newTrigger()
                            .startAt(new Date())
                            .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(seconds))
                            .build();

                    scheduler.scheduleJob(job, trigger);
                }
            } catch (NoClassDefFoundError e) {
                logger.debug("Database module not available, not retrying failed connections", e);
            }

            status = ModuleStatus.STARTED;
        } catch (Exception e) {
            logger.error("Quartz Scheduler failed to initialize", e);
        }
    }

    protected StdSchedulerFactory getSchedulerFactory(String configFile) throws SchedulerException {
        StdSchedulerFactory factory;
        if (configFile != null) {
            factory = new StdSchedulerFactory(configFile);
        } else {
            factory = new StdSchedulerFactory();
        }
        return factory;
    }

    @PreDestroy
    public void destroy() {
        try {
            scheduler.shutdown(waitOnShutdown);
        } catch (SchedulerException e) {
            logger.warn("Cannot pause scheduler", e);
        } finally {
            status = ModuleStatus.DESTROYED;
        }
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
