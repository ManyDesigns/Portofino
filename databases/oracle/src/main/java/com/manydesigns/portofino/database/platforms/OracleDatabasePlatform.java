/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.portofino.database.model.ConnectionProvider;
import com.manydesigns.portofino.database.model.platforms.AbstractDatabasePlatform;
import org.hibernate.dialect.OracleDialect;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class OracleDatabasePlatform extends AbstractDatabasePlatform {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public final static String DESCRIPTION = "Oracle";
    public final static String STANDARD_DRIVER_CLASS_NAME =
            "oracle.jdbc.driver.OracleDriver";

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public OracleDatabasePlatform() {
        super(OracleDialect.class.getName(), "jdbc:oracle:thin:@//<host>:<port, default 1521>:<sid>");
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
        return "Oracle".equals(connectionProvider.getDatabaseProductName());
    }

    @Override
    public List<String[]> getSchemaNames(DatabaseMetaData databaseMetaData) throws SQLException {
        List<String[]> schemaNames = super.getSchemaNames(databaseMetaData);
        schemaNames.removeIf(schemaName -> "SYS".equals(schemaName[1]) || "SYSTEM".equals(schemaName[1]));
        return schemaNames;
    }
}
