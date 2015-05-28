/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.quartz.PortofinoJobFactory;
import org.apache.commons.configuration.Configuration;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class QuartzModule implements Module {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Inject(BaseModule.SERVLET_CONTEXT)
    public ServletContext servletContext;

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
        return ModuleRegistry.getPortofinoVersion();
    }

    @Override
    public int getMigrationVersion() {
        return 1;
    }

    @Override
    public double getPriority() {
        return 10;
    }

    @Override
    public String getId() {
        return "quartz";
    }

    @Override
    public String getName() {
        return "Quartz";
    }

    @Override
    public int install() {
        return 1;
    }

    @Override
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
            scheduler.setJobFactory(new PortofinoJobFactory(servletContext));

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

            status = ModuleStatus.ACTIVE;
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

    @Override
    public void start() {
        try {
            if(startOnLoad) {
                scheduler.start();
            }
            status = ModuleStatus.STARTED;
        } catch (SchedulerException e) {
            logger.error("Could not start scheduler", e);
        }
    }

    @Override
    public void stop() {
        try {
            scheduler.pauseAll();
        } catch (SchedulerException e) {
            logger.warn("Cannot pause scheduler", e);
        } finally {
            status = ModuleStatus.STOPPED;
        }
    }

    @Override
    public void destroy() {
        try {
            scheduler.shutdown(waitOnShutdown);
        } catch (SchedulerException e) {
            logger.error("Could not shut scheduler down", e);
        }
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
