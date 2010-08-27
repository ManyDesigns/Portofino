/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.database.platforms;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.InstanceBuilder;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.database.ConnectionProvider;

import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class DatabasePlatformManager {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected static final Properties portofinoProperties;
    protected static final DatabasePlatformManager manager;

    public static final Logger logger =
            LogUtil.getLogger(DatabasePlatformManager.class);

    protected ArrayList<DatabasePlatform> databasePlatformList;

    //**************************************************************************
    // Static initializer
    //**************************************************************************

    static {
        portofinoProperties = PortofinoProperties.getProperties();
        String managerClassName =
                portofinoProperties.getProperty(
                        PortofinoProperties.DATABASE_PLATFORM_MANAGER_PROPERTY);
        InstanceBuilder<DatabasePlatformManager> builder =
                new InstanceBuilder<DatabasePlatformManager>(
                        DatabasePlatformManager.class,
                        DatabasePlatformManager.class,
                        logger);
        manager = builder.createInstance(managerClassName);
    }

    public static DatabasePlatformManager getManager() {
        return manager;
    }

    //**************************************************************************
    // Constructors / building
    //**************************************************************************

    public DatabasePlatformManager() {
        databasePlatformList = new ArrayList<DatabasePlatform>();
        String listString = portofinoProperties.getProperty(
                PortofinoProperties.DATABASE_PLATFORM_LIST_PROPERTY);
        if (listString == null) {
            logger.finer("Empty list");
            return;
        }

        String[] helperClassArray = listString.split(",");
        for (String current : helperClassArray) {
            addDatabasePlatform(current);
        }
    }

    protected void addDatabasePlatform(String databasePlatformClassName) {
        databasePlatformClassName = databasePlatformClassName.trim();
        LogUtil.finerMF(logger,
                    "Adding database platform: {0}", databasePlatformClassName);
        DatabasePlatform databasePlatform =
                (DatabasePlatform)ReflectionUtil
                        .newInstance(databasePlatformClassName);
        if (databasePlatform == null) {
            LogUtil.warningMF(logger, "Cannot load or instanciate: {0}",
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
}
