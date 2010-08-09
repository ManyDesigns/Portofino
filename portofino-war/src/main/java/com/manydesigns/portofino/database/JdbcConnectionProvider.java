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

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Password;
import com.manydesigns.elements.logging.LogUtil;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.text.MessageFormat;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class JdbcConnectionProvider implements ConnectionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final String databaseName;
    protected final String driverClass;
    protected final String connectionURL;
    protected final String username;
    protected final String password;

    protected DatabaseAbstraction databaseAbstraction;

    public static final Logger logger =
            LogUtil.getLogger(JdbcConnectionProvider.class);


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public JdbcConnectionProvider(String databaseName,
                                  String driverClass,
                                  String connectionURL,
                                  String username,
                                  String password) {
        this.databaseName = databaseName;
        this.driverClass = driverClass;
        this.connectionURL = connectionURL;
        this.username = username;
        this.password = password;
    }


    //**************************************************************************
    // Implementation of ConnectionProvider
    //**************************************************************************

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDescription() {
        return MessageFormat.format(
                "JDBC connection to URL: {0}", connectionURL);
    }

    public DatabaseAbstraction getDatabaseAbstraction() {
        if (databaseAbstraction == null) {
            createDatabaseAbstraction();
        }
        return databaseAbstraction;
    }

    protected void createDatabaseAbstraction() {
        String databaseProductName;
        Connection conn = null;
        try {
            conn = acquireConnection();
            databaseProductName = conn.getMetaData().getDatabaseProductName();
        } catch (Throwable e) {
            LogUtil.severeMF(logger,
                    "Could not create database abstraction for {0}",
                    e, databaseName);
            return;
        } finally {
            releaseConnection(conn);
        }

        LogUtil.fineMF(logger, "Database product name: {0}", databaseProductName);

        try {
            if ("PostgreSQL".equals(databaseProductName)) {
                databaseAbstraction = new PostgreSQLDatabaseAbstraction(this);
            } else {
                LogUtil.warningMF(logger,
                        "Database product name {0} not supported",
                        databaseProductName);
            }
        } catch (Throwable e) {
            LogUtil.warning(logger, "Could not instanciate abstraction", e);
        }
    }

    public void test() throws Exception {

    }

    public Connection acquireConnection() throws Exception {
        Class.forName(driverClass);
        return DriverManager.getConnection(connectionURL,
                username, password);
    }

    public void releaseConnection(Connection conn) {
        DbUtil.closeConnection(conn);
    }

    //**************************************************************************
    // Getters
    //**************************************************************************

    public String getDriverClass() {
        return driverClass;
    }

    @Label("connection URL")
    public String getConnectionURL() {
        return connectionURL;
    }

    public String getUsername() {
        return username;
    }

    @Password
    public String getPassword() {
        return password;
    }

    //**************************************************************************
    // Other methods
    //**************************************************************************

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("databaseName", connectionURL)
                .append("driverClass", driverClass)
                .append("connectionURL", connectionURL)
                .append("username", username)
                .append("password", "********")
                .toString();
    }
}
