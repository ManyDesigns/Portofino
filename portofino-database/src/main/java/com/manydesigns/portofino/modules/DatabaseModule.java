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

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.admin.ConnectionProvidersAction;
import com.manydesigns.portofino.actions.admin.ReloadModelAction;
import com.manydesigns.portofino.actions.admin.TablesAction;
import com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.liquibase.LiquibaseUtils;
import com.manydesigns.portofino.menu.*;
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Inject(BaseModule.SERVLET_CONTEXT)
    public ServletContext servletContext;

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Inject(BaseModule.APPLICATION_DIRECTORY)
    public File applicationDirectory;

    @Inject(BaseModule.ADMIN_MENU)
    public MenuBuilder adminMenu;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String PERSISTENCE = "com.manydesigns.portofino.modules.DatabaseModule.persistence";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(DatabaseModule.class);

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
        return 2;
    }

    @Override
    public String getId() {
        return "portofino-database";
    }

    @Override
    public String getName() {
        return "Portofino Database";
    }

    @Override
    public int install() {
        return 1;
    }

    @Override
    public void init() {
        logger.info("Setting up Liquibase");
        LiquibaseUtils.setup();

        logger.info("Initializing persistence");
        Persistence persistence =
                new Persistence(applicationDirectory, configuration, new DatabasePlatformsManager(configuration));
        persistence.loadXmlModel();
        servletContext.setAttribute(PERSISTENCE, persistence);

        appendToAdminMenu();

        status = ModuleStatus.ACTIVE;
    }

    protected void appendToAdminMenu() {
        SimpleMenuAppender group;
        SimpleMenuAppender link;

        group = SimpleMenuAppender.group("dataModeling", null, "Data modeling", 3.0);
        adminMenu.menuAppenders.add(group);

        link = SimpleMenuAppender.link(
                "dataModeling", "wizard", null, "Wizard", ApplicationWizard.URL_BINDING, 1.0);
        adminMenu.menuAppenders.add(link);
        link = SimpleMenuAppender.link(
                "dataModeling", "connectionProviders", null, "Connection providers", ConnectionProvidersAction.URL_BINDING, 2.0);
        adminMenu.menuAppenders.add(link);
        link = SimpleMenuAppender.link(
                "dataModeling", "tables", null, "Tables", TablesAction.BASE_ACTION_PATH, 3.0);
        adminMenu.menuAppenders.add(link);
        link = SimpleMenuAppender.link(
                "dataModeling", "reloadModel", null, "Reload model", ReloadModelAction.URL_BINDING, 4.0);
        adminMenu.menuAppenders.add(link);
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
