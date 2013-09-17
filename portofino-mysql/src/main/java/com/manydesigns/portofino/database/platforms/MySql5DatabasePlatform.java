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
import org.apache.commons.dbutils.DbUtils;
import org.hibernate.dialect.MySQLDialect;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class MySql5DatabasePlatform extends AbstractDatabasePlatform {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public final static String DESCRIPTION = "MySQL 5.x";
    public final static String STANDARD_DRIVER_CLASS_NAME =
            "com.mysql.jdbc.Driver";

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public MySql5DatabasePlatform() {
        super(new MySQLDialect(), "jdbc:mysql://<host>[:<port>]/<database>");
    }

    //**************************************************************************
    // Implementation of DatabaseAbstraction
    //**************************************************************************

    public String getDescription() {
        return DESCRIPTION;
    }

    public String getStandardDriverClassName() {
        return STANDARD_DRIVER_CLASS_NAME;
    }

    public boolean isApplicable(ConnectionProvider connectionProvider) {
        return "MySQL".equals(connectionProvider.getDatabaseProductName());
    }


    public List<String> getSchemaNames(DatabaseMetaData databaseMetaData) throws SQLException {
        ResultSet rs = databaseMetaData.getCatalogs();
        List<String> schemaNames = new ArrayList<String>();
        try {
            while(rs.next()) {
                String schemaName = rs.getString(TABLE_CAT);
                schemaNames.add(schemaName);
            }
        } finally {
            DbUtils.closeQuietly(rs);
        }
        return schemaNames;
    }
}

