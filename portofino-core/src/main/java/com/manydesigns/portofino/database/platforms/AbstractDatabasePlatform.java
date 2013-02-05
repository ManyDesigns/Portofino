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
import org.hibernate.dialect.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class AbstractDatabasePlatform implements DatabasePlatform {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final static String[] tableTypes = {"TABLE"};
    public static final String TABLE_CAT = "TABLE_CAT";
    public static final String TABLE_SCHEM = "TABLE_SCHEM";
    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String COLUMN_NAME = "COLUMN_NAME";
    public static final String KEY_SEQ = "KEY_SEQ";
    public static final String PK_NAME = "PK_NAME";
    public static final String TYPE_NAME = "TYPE_NAME";
    public static final String NULLABLE = "NULLABLE";
    public static final String COLUMN_SIZE = "COLUMN_SIZE";
    public static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";
    public static final String FKTABLE_SCHEM = "FKTABLE_SCHEM";
    public static final String FKTABLE_NAME = "FKTABLE_NAME";
    public static final String FKCOLUMN_NAME = "FKCOLUMN_NAME";
    public static final String PKTABLE_SCHEM = "PKTABLE_SCHEM";
    public static final String PKTABLE_NAME = "PKTABLE_NAME";
    public static final String PKCOLUMN_NAME = "PKCOLUMN_NAME";
    public static final String UPDATE_RULE = "UPDATE_RULE";
    public static final String DELETE_RULE = "DELETE_RULE";
    public static final String DEFERRABILITY = "DEFERRABILITY";
    public static final String FK_NAME = "FK_NAME";
    public static final String FKTABLE_CAT = "FKTABLE_CAT";
    public static final String PKTABLE_CAT = "PKTABLE_CAT";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String status;
    protected Dialect hibernateDialect;
    protected String connectionStringTemplate;
    public static final Logger logger =
            LoggerFactory.getLogger(AbstractDatabasePlatform.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public AbstractDatabasePlatform(Dialect hibernateDialect, String connectionStringTemplate) {
        this.hibernateDialect = hibernateDialect;
        this.connectionStringTemplate = connectionStringTemplate;
        status = STATUS_CREATED;
    }

    //**************************************************************************
    // Implementation of DatabasePlatform
    //**************************************************************************

    public void test() {
        boolean success = DbUtils.loadDriver(getStandardDriverClassName());
        if (success) {
            status = STATUS_OK;
        } else {
            status = STATUS_DRIVER_NOT_FOUND;
        }
    }

    public String getStatus() {
        return status;
    }

    public Dialect getHibernateDialect() {
        return hibernateDialect;
    }

    public boolean isDialectAutodetected() {
        return true;
    }

    public String getConnectionStringTemplate() {
        return connectionStringTemplate;
    }

    public void shutdown(ConnectionProvider connectionProvider) {
        logger.info("Shutting down connection provider: {}",
                connectionProvider.getDatabase().getDatabaseName());
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    public List<String> getSchemaNames(DatabaseMetaData databaseMetaData) throws SQLException {
        ResultSet rs = databaseMetaData.getSchemas();
        List<String> schemaNames = new ArrayList<String>();
        try {
            while(rs.next()) {
                String schemaName = rs.getString(TABLE_SCHEM);
                schemaNames.add(schemaName);
            }
        } finally {
            DbUtils.closeQuietly(rs);
        }
        return schemaNames;
    }
}
