/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.modules;

import com.manydesigns.portofino.di.Injections;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ModuleRegistry {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final NavigableSet<Module> modules =  new TreeSet<Module>(new ModuleComparator());
    protected final FileConfiguration configuration;

    public static final Logger logger = LoggerFactory.getLogger(ModuleRegistry.class);

    public ModuleRegistry(FileConfiguration configuration) {
        this.configuration = configuration;
    }

    public NavigableSet<Module> getModules() {
        return modules;
    }

    public void migrateAndInit(ServletContext servletContext) {
        for(Module module : modules) {
            int migrationVersion = module.getMigrationVersion();
            String key = "module." + module.getId() + ".migration.version";
            int installedVersion = configuration.getInt(key, -1);

            boolean migrationOk = true;
            Injections.inject(module, servletContext, null);

            if(installedVersion == -1) try { //Install
                logger.info("Installing module " + printModule(module) + "...");
                installedVersion = module.install();
                configuration.setProperty(key, installedVersion);
                configuration.save();
                logger.info("Installed module " + printModule(module));
            } catch (Throwable e) {
                logger.error(
                        "Could not install module " + printModule(module), e);
                migrationOk = false;
            }

            try { //Migrate
                while(installedVersion < migrationVersion) {
                    logger.info("Migrating module " + printModule(module) + " from version " + installedVersion + "...");
                    Method method = module.getClass().getMethod("migrateFrom" + installedVersion);
                    if(!Integer.TYPE.equals(method.getReturnType())) {
                        throw new RuntimeException("Migration method " + method + " does not return int");
                    }
                    Integer result = (Integer) method.invoke(module);
                    if(result > installedVersion) {
                        installedVersion = result;
                        configuration.setProperty(key, result);
                        configuration.save();
                        logger.info("Migrated module " + printModule(module));
                    } else {
                        throw new RuntimeException(
                                "Migration returned version " + result +
                                " while the installed one is " + installedVersion);
                    }
                }
            } catch (Throwable e) {
                logger.error(
                        "Could not migrate module " + printModule(module) + " from version " + installedVersion, e);
                migrationOk = false;
            }

            if(migrationOk) { //Init (skip if installation or migration failed)
                try {
                    logger.debug("Initializing module " + printModule(module) + "...");
                    module.init();
                    logger.info("Initialized module " + printModule(module));
                } catch (Throwable e) {
                    logger.error(
                            "Could not initialize module " + printModule(module), e);
                }
            }
        }
    }

    protected String printModule(Module module) {
        return "" + module.getName() + " (" + module.getId() + ")";
    }

    public void start() {
        for(Module module : modules) {
            if(module.getStatus() == ModuleStatus.ACTIVE) {
                try {
                    logger.debug("Module " + printModule(module) + " starting...");
                    module.start();
                    logger.info("Module " + printModule(module) + " started.");
                } catch (Throwable e) {
                    logger.error("Could not start module " + printModule(module), e);
                }
            }
        }
    }

    public void stop() {
        for(Module module : modules.descendingSet()) {
            if(module.getStatus() == ModuleStatus.STARTED) {
                try {
                    logger.debug("Stopping module " + printModule(module) + " ...");
                    module.stop();
                    logger.info("Module " + printModule(module) + " stopped.");
                } catch (Throwable e) {
                    logger.error("Could not stop module " + printModule(module), e);
                }
            }
        }
    }

    public void destroy() {
        stop();
        for(Module module : modules.descendingSet()) { //Iterate backwards
            if(module.getStatus() == ModuleStatus.STOPPED) {
                try {
                    logger.debug("Destroying module " + printModule(module) + "...");
                    module.destroy();
                    logger.info("Destroyed module " + printModule(module));
                } catch (Throwable e) {
                    logger.error("Could not destroy module " + printModule(module), e);
                }
            }
        }
    }

    public static class ModuleComparator implements Comparator<Module> {

        @Override
        public int compare(Module m1, Module m2) {
            int cmp = Double.compare(m1.getPriority(), m2.getPriority());
            //Establish a proper order even if priorities are equal
            return cmp != 0 ? cmp : m1.getId().compareTo(m2.getId());
        }
    }

    //Utilities
    public static String getPortofinoVersion() {
        try {
            return IOUtils.toString(ModuleRegistry.class.getResourceAsStream("/portofino.version"));
        } catch (IOException e) {
            return null;
        }
    }

}
