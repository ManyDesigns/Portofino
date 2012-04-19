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

package com.manydesigns.portofino.database.platforms;

import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.model.database.ConnectionProvider;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DatabasePlatformsManager {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Configuration portofinoConfiguration;
    protected ArrayList<DatabasePlatform> databasePlatformList;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(DatabasePlatformsManager.class);

    //**************************************************************************
    // Constructors / building
    //**************************************************************************

    public DatabasePlatformsManager(Configuration portofinoConfiguration) {
        this.portofinoConfiguration = portofinoConfiguration;
        databasePlatformList = new ArrayList<DatabasePlatform>();
        String[] platformNames = portofinoConfiguration.getStringArray(
                PortofinoProperties.DATABASE_PLATFORMS_LIST);
        if (platformNames == null) {
            logger.debug("Empty list");
            return;
        }

        for (String current : platformNames) {
            addDatabasePlatform(current);
        }
    }

    protected void addDatabasePlatform(String databasePlatformClassName) {
        databasePlatformClassName = databasePlatformClassName.trim();
        logger.debug("Adding database platform: {}", databasePlatformClassName);
        DatabasePlatform databasePlatform =
                (DatabasePlatform)ReflectionUtil
                        .newInstance(databasePlatformClassName);
        if (databasePlatform == null) {
            logger.warn("Cannot load or instanciate: {}",
                    databasePlatformClassName);
        } else {
            databasePlatform.test();
            databasePlatformList.add(databasePlatform);
        }
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
