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

package com.manydesigns.portofino.database.platforms;

import com.manydesigns.portofino.model.database.ConnectionProvider;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DatabasePlatformsRegistry {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Configuration portofinoConfiguration;
    protected final List<DatabasePlatform> databasePlatformList = new ArrayList<DatabasePlatform>();

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(DatabasePlatformsRegistry.class);

    //**************************************************************************
    // Constructors / building
    //**************************************************************************

    public DatabasePlatformsRegistry(Configuration portofinoConfiguration) {
        this.portofinoConfiguration = portofinoConfiguration;
    }

    public void addDatabasePlatform(DatabasePlatform databasePlatform) {
        logger.debug("Adding database platform: {}", databasePlatform);
        databasePlatform.test();
        databasePlatformList.add(databasePlatform);
    }

    //**************************************************************************
    // Methods
    //**************************************************************************
    public DatabasePlatform findApplicableAbstraction(
            ConnectionProvider connectionProvider) {
        for (DatabasePlatform current : databasePlatformList) {
            if (current.isApplicable(connectionProvider)) {
                return current;
            }
        }
        return null;
    }

    public DatabasePlatform[] getDatabasePlatforms() {
        DatabasePlatform[] result =
                new DatabasePlatform[databasePlatformList.size()];
        databasePlatformList.toArray(result);
        return result;
    }

    //**************************************************************************
    // Gettes/setters
    //**************************************************************************


    public Configuration getPortofinoConfiguration() {
        return portofinoConfiguration;
    }
}
