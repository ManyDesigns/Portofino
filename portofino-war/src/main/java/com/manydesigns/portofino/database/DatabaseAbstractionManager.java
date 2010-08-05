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

package com.manydesigns.portofino.database;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.InstanceBuilder;
import com.manydesigns.portofino.PortofinoProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class DatabaseAbstractionManager {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected static final Properties portofinoProperties;
    protected static final DatabaseAbstractionManager manager;

    public static final Logger logger =
            LogUtil.getLogger(DatabaseAbstractionManager.class);


    static {
        portofinoProperties = PortofinoProperties.getProperties();
        String managerClassName =
                portofinoProperties.getProperty(
                        PortofinoProperties
                                .DATABASE_ABSTRACTION_MANAGER_CLASS_PROPERTY);
        InstanceBuilder<DatabaseAbstractionManager> builder =
                new InstanceBuilder<DatabaseAbstractionManager>(
                        DatabaseAbstractionManager.class,
                        DatabaseAbstractionManager.class,
                        logger);
        manager = builder.createInstance(managerClassName);
    }

    public static DatabaseAbstractionManager getManager() {
        return manager;
    }

    public DatabaseAbstractionManager() {}

    public DatabaseAbstraction getDatabaseAbstraction(
            ConnectionProvider connectionProvider) {
        String databaseProductName;
        Connection conn = null;
        try {
            conn = connectionProvider.acquireConnection();
            databaseProductName = conn.getMetaData().getDatabaseProductName();
        } catch (Throwable e) {
            LogUtil.severeMF(logger,
                    "Could not create database abstraction for {0}",
                    e, connectionProvider);
            return null;
        } finally {
            connectionProvider.releaseConnection(conn);
        }

        LogUtil.fineMF(logger, "Database product name: {0}", databaseProductName);

        DatabaseAbstraction databaseAbstraction = null;
        if (databaseProductName == null) {
            LogUtil.warningMF(logger,
                    "Unknown database product name: {0}", databaseProductName);
            return null;
        }
        try {
            if ("PostgreSQL".equals(databaseProductName)) {
                databaseAbstraction = new PostgreSQLDatabaseAbstraction(connectionProvider);
            } else {
                LogUtil.warningMF(logger,
                        "Database {0} not supported", databaseProductName);
            }
        } catch (SQLException e) {
            LogUtil.warning(logger, "Could not instanciate abstraction", e);
        }
        return databaseAbstraction;
    }
}
