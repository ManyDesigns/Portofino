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

package com.manydesigns.portofino.base.database;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class CommonDatabaseAbstraction implements DatabaseAbstraction {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private final ConnectionProvider connectionProvider;

    private final String databaseProductName;
    private final String databaseProductVersion;
    private final Integer databaseMajorVersion;
    private final Integer databaseMinorVersion;
    private final String databaseMajorMinorVersion;

    private final String driverName;
    private final String driverVersion;
    private final Integer driverMajorVersion;
    private final Integer driverMinorVersion;
    private final String driverMajorMinorVersion;

    private final Integer JDBCMajorVersion;
    private final Integer JDBCMinorVersion;
    private final String JDBCMajorMinorVersion;


    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public CommonDatabaseAbstraction(ConnectionProvider connectionProvider)
            throws SQLException {
        this.connectionProvider = connectionProvider;

        Connection conn = null;
        try {
            conn = connectionProvider.getConnection();

            DatabaseMetaData metadata = conn.getMetaData();

            databaseProductName = metadata.getDatabaseProductName();
            databaseProductVersion = metadata.getDatabaseProductVersion();

            Integer tmpMajorVersion = null;
            Integer tmpMinorVersion = null;
            String tmpMajorMinorVersion;
            try {
                tmpMajorVersion = metadata.getDatabaseMajorVersion();
                tmpMinorVersion = metadata.getDatabaseMinorVersion();
                tmpMajorMinorVersion = MessageFormat.format("{0}.{1}",
                        tmpMajorVersion, tmpMinorVersion);
            } catch (SQLException e) {
                tmpMajorMinorVersion = e.getMessage();
            }
            databaseMajorVersion = tmpMajorVersion;
            databaseMinorVersion = tmpMinorVersion;
            databaseMajorMinorVersion = tmpMajorMinorVersion;

            this.driverName = metadata.getDriverName();
            this.driverVersion = metadata.getDriverVersion();

            this.driverMajorVersion = metadata.getDriverMajorVersion();
            this.driverMinorVersion = metadata.getDriverMinorVersion();
            this.driverMajorMinorVersion = MessageFormat.format("{0}.{1}",
                    driverMajorVersion, driverMinorVersion);

            tmpMajorVersion = null;
            tmpMinorVersion = null;
            try {
                tmpMajorVersion = metadata.getJDBCMajorVersion();
                tmpMinorVersion = metadata.getJDBCMinorVersion();
                tmpMajorMinorVersion = MessageFormat.format("{0}.{1}",
                        tmpMajorVersion, tmpMinorVersion);
            } catch (SQLException e) {
                tmpMajorMinorVersion = e.getMessage();
            }
            this.JDBCMajorVersion = tmpMajorVersion;
            this.JDBCMinorVersion = tmpMinorVersion;
            this.JDBCMajorMinorVersion = tmpMajorMinorVersion;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    /*IGNORE*/
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    // Implementation of DatabaseAbstraction
    //--------------------------------------------------------------------------

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

    public String getDriverMajorMinorVersion() {
        return driverMajorMinorVersion;
    }

    public Integer getJDBCMajorVersion() {
        return JDBCMajorVersion;
    }

    public Integer getJDBCMinorVersion() {
        return JDBCMinorVersion;
    }

    public String getJDBCMajorMinorVersion() {
        return JDBCMajorMinorVersion;
    }
}
