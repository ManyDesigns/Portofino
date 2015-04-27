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

import com.manydesigns.portofino.database.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.di.Injections;
import com.manydesigns.portofino.persistence.Persistence;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DatabaseModule implements Module {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Inject(BaseModule.SERVLET_CONTEXT)
    public ServletContext servletContext;

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Inject(BaseModule.APPLICATION_DIRECTORY)
    public File applicationDirectory;

    protected Persistence persistence;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String PERSISTENCE =
            "com.manydesigns.portofino.modules.DatabaseModule.persistence";
    public static final String DATABASE_PLATFORMS_REGISTRY =
            "com.manydesigns.portofino.modules.DatabaseModule.databasePlatformsRegistry";
    //Liquibase properties
    public static final String LIQUIBASE_ENABLED = "liquibase.enabled";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(DatabaseModule.class);

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
        return "database";
    }

    @Override
    public String getName() {
        return "Database";
    }

    @Override
    public int install() {
        return 1;
    }

    @Override
    public void init() {
        logger.info("Initializing persistence");
        DatabasePlatformsRegistry databasePlatformsRegistry = new DatabasePlatformsRegistry(configuration);

        persistence = new Persistence(applicationDirectory, configuration, databasePlatformsRegistry);
        Injections.inject(persistence, servletContext, null);
        servletContext.setAttribute(DATABASE_PLATFORMS_REGISTRY, databasePlatformsRegistry);
        servletContext.setAttribute(PERSISTENCE, persistence);

        status = ModuleStatus.ACTIVE;
    }

    @Override
    public void start() {
        persistence.loadXmlModel();
        status = ModuleStatus.STARTED;
    }

    @Override
    public void stop() {
        persistence.shutdown();
        status = ModuleStatus.STOPPED;
    }

    @Override
    public void destroy() {
        logger.info("ManyDesigns Portofino database module stopping...");
        logger.info("ManyDesigns Portofino database module stopped.");
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
