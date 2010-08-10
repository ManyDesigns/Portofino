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
import com.manydesigns.portofino.model.Database;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class JdbcConnectionProvider implements ConnectionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static String STATUS_CREATED = "created";
    public final static String STATUS_CONNECTED = "connected";
    public final static String STATUS_ERROR = "error";

    //**************************************************************************
    // Fields (configured values)
    //**************************************************************************

    protected final String databaseName;
    protected final String driverClass;
    protected final String connectionURL;
    protected final String username;
    protected final String password;


    //**************************************************************************
    // Fields (detected values)
    //**************************************************************************

    protected String databaseProductName;
    protected String databaseProductVersion;
    protected Integer databaseMajorVersion;
    protected Integer databaseMinorVersion;
    protected String databaseMajorMinorVersion;

    protected String driverName;
    protected String driverVersion;
    protected Integer driverMajorVersion;
    protected Integer driverMinorVersion;
    protected String driverMajorMinorVersion;

    protected Integer JDBCMajorVersion;
    protected Integer JDBCMinorVersion;
    protected String JDBCMajorMinorVersion;

    protected Type[] types;

    protected DatabaseAbstraction databaseAbstraction;

    //**************************************************************************
    // Fields (others)
    //**************************************************************************

    protected String status;
    protected String errorMessage;

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

        status = STATUS_CREATED;
        errorMessage = null;
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

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public DatabaseAbstraction getDatabaseAbstraction() {
        return databaseAbstraction;
    }

    public void test() {
        Connection conn = null;
        ResultSet typeRs = null;
        try {
            conn = acquireConnection();

            DatabaseMetaData metadata = conn.getMetaData();

            databaseProductName = metadata.getDatabaseProductName();
            databaseProductVersion = metadata.getDatabaseProductVersion();

            try {
                databaseMajorVersion = metadata.getDatabaseMajorVersion();
                databaseMinorVersion = metadata.getDatabaseMinorVersion();
                databaseMajorMinorVersion = MessageFormat.format("{0}.{1}",
                        databaseMajorVersion, databaseMinorVersion);
            } catch (SQLException e) {
                databaseMajorMinorVersion = e.getMessage();
            }

            driverName = metadata.getDriverName();
            driverVersion = metadata.getDriverVersion();

            driverMajorVersion = metadata.getDriverMajorVersion();
            driverMinorVersion = metadata.getDriverMinorVersion();
            driverMajorMinorVersion = MessageFormat.format("{0}.{1}",
                    driverMajorVersion, driverMinorVersion);

            try {
                JDBCMajorVersion = metadata.getJDBCMajorVersion();
                JDBCMinorVersion = metadata.getJDBCMinorVersion();
                JDBCMajorMinorVersion = MessageFormat.format("{0}.{1}",
                        JDBCMajorVersion, JDBCMinorVersion);
            } catch (SQLException e) {
                JDBCMajorMinorVersion = e.getMessage();
            }

            // extract supported types
            List<Type> typeList = new ArrayList<Type>();
            typeRs = metadata.getTypeInfo();
            while (typeRs.next()) {
                String typeName = typeRs.getString("TYPE_NAME");
                int dataType = typeRs.getInt("DATA_TYPE");
                int maximumPrecision = typeRs.getInt("PRECISION");
                String literalPrefix = typeRs.getString("LITERAL_PREFIX");
                String literalSuffix = typeRs.getString("LITERAL_SUFFIX");
                boolean nullable =
                        (typeRs.getShort("NULLABLE") ==
                                DatabaseMetaData.typeNullable);
                boolean caseSensitive = typeRs.getBoolean("CASE_SENSITIVE");
                boolean searchable =
                        (typeRs.getShort("SEARCHABLE") ==
                                DatabaseMetaData.typeSearchable);
                boolean autoincrement = typeRs.getBoolean("AUTO_INCREMENT");
                short minimumScale = typeRs.getShort("MINIMUM_SCALE");
                short maximumScale = typeRs.getShort("MAXIMUM_SCALE");

                Type type = new Type(typeName, dataType, maximumPrecision,
                        literalPrefix, literalSuffix, nullable, caseSensitive,
                        searchable, autoincrement, minimumScale, maximumScale);
                typeList.add(type);
            }
            types = new Type[typeList.size()];
            typeList.toArray(types);

            if ("PostgreSQL".equals(databaseProductName)) {
                databaseAbstraction = new PostgreSQLDatabaseAbstraction();
                status = STATUS_CONNECTED;
                errorMessage = null;
            } else {
                status = STATUS_ERROR;
                errorMessage = MessageFormat.format(
                        "Database product name {0} not supported",
                        databaseProductName);
                logger.warning(errorMessage);
            }
        } catch (Throwable e) {
            status = STATUS_ERROR;
            errorMessage = e.getMessage();
            LogUtil.severeMF(logger,
                    "Could not create database abstraction for {0}",
                    e, databaseName);
        } finally {
            DbUtil.closeResultSetAndStatement(typeRs);
            releaseConnection(conn);
        }
    }

    public Connection acquireConnection() throws Exception {
        Class.forName(driverClass);
        return DriverManager.getConnection(connectionURL,
                username, password);
    }

    public void releaseConnection(Connection conn) {
        DbUtil.closeConnection(conn);
    }

    public String getDatabaseProductName() {
        return databaseProductName;
    }

    public String getDatabaseProductVersion() {
        return databaseProductVersion;
    }

    public Integer getDatabaseMajorVersion() {
        return databaseMajorVersion;
    }

    public Integer getDatabaseMinorVersion() {
        return databaseMinorVersion;
    }

    @Label("database major/minor version")
    public String getDatabaseMajorMinorVersion() {
        return databaseMajorMinorVersion;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverVersion() {
        return driverVersion;
    }

    public Integer getDriverMajorVersion() {
        return driverMajorVersion;
    }

    public Integer getDriverMinorVersion() {
        return driverMinorVersion;
    }

    @Label("driver major/minor version")
    public String getDriverMajorMinorVersion() {
        return driverMajorMinorVersion;
    }

    public Integer getJDBCMajorVersion() {
        return JDBCMajorVersion;
    }

    public Integer getJDBCMinorVersion() {
        return JDBCMinorVersion;
    }

    @Label("JDBC major/minor version")
    public String getJDBCMajorMinorVersion() {
        return JDBCMajorMinorVersion;
    }

    public Type[] getTypes() {
        return types.clone();
    }

    public Type getTypeByName(String typeName) {
        for (Type current : types) {
            if (current.getTypeName().equalsIgnoreCase(typeName)) {
                return current;
            }
        }
        return null;
    }

    public Database readModel() {
        return databaseAbstraction.readModel(this);
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
