/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.connections;

import com.manydesigns.elements.annotations.*;
import com.manydesigns.portofino.database.DbUtil;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.model.datamodel.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class ConnectionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final static String STATUS_DISCONNECTED = "disconnected";
    public final static String STATUS_CONNECTED = "connected";
    public final static String STATUS_ERROR = "error";

    //**************************************************************************
    // Fields (detected values)
    //**************************************************************************

    protected String databaseName;
    protected String includeSchemas;
    protected String excludeSchemas;

    //**************************************************************************
    // Fields (initialized and detected values)
    //**************************************************************************

    protected Pattern includeSchemasPattern;
    protected Pattern excludeSchemasPattern;

    protected final List<Type> types;

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
    protected DatabasePlatform databasePlatform;
    protected String status;
    protected String errorMessage;
    protected Date lastTested;



    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(JdbcConnectionProvider.class);


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public ConnectionProvider() {
        types = new ArrayList<Type>();
    }


    //**************************************************************************
    // Initialization
    //**************************************************************************

    public void reset() {
        includeSchemasPattern = null;
        excludeSchemasPattern = null;

        types.clear();

        databaseProductName = null;
        databaseProductVersion = null;
        databaseMajorVersion = null;
        databaseMinorVersion = null;
        databaseMajorMinorVersion = null;
        driverName = null;
        driverVersion = null;
        driverMajorVersion = null;
        driverMinorVersion = null;
        driverMajorMinorVersion = null;
        JDBCMajorVersion = null;
        JDBCMinorVersion = null;
        JDBCMajorMinorVersion = null;
        databasePlatform = null;
        status = null;
        errorMessage = null;
        lastTested = null;
    }

    public void init(DatabasePlatformsManager databasePlatformsManager) {
        if (includeSchemas == null) {
            includeSchemasPattern = null;
        } else {
            includeSchemasPattern = Pattern.compile(includeSchemas);
        }

        if (excludeSchemas == null) {
            excludeSchemasPattern = null;
        } else {
            excludeSchemasPattern = Pattern.compile(excludeSchemas);
        }

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
            } catch (Throwable e) {
                JDBCMajorMinorVersion = e.getMessage();
            }

            // extract supported types
            types.clear();
            typeRs = metadata.getTypeInfo();
            while (typeRs.next()) {
                readType(typeRs);
            }
            Collections.sort(types, new TypeComparator());

            databasePlatform =
                    databasePlatformsManager.findApplicableAbstraction(this);
            if (databasePlatform == null) {
                status = STATUS_ERROR;
                errorMessage = MessageFormat.format(
                        "Database abstraction not found for {0}",
                        databaseProductName);
                logger.warn(errorMessage);
            } else {
                status = STATUS_CONNECTED;
                errorMessage = null;
            }
        } catch (Throwable e) {
            status = STATUS_ERROR;
            errorMessage = e.getMessage();
            logger.warn("Could not create database abstraction for " +
                    databaseName, e);
        } finally {
            DbUtil.closeResultSetAndStatement(typeRs);
            releaseConnection(conn);
            lastTested = new Date();
        }
    }

    public void shutdown() {
        if (databasePlatform != null) {
            databasePlatform.shutdown(this);
        }
    }

    protected void readType(ResultSet typeRs)
            throws SQLException {
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
        types.add(type);
    }

    public abstract String getDescription();
    public abstract Connection acquireConnection() throws Exception;
    public abstract void releaseConnection(Connection conn);


    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlAttribute(required = true)
    @Updatable(false)
    @Required
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @XmlAttribute()
    @FieldSize(100)
    public String getIncludeSchemas() {
        return includeSchemas;
    }

    public void setIncludeSchemas(String includeSchemas) {
        this.includeSchemas = includeSchemas;
    }

    @XmlAttribute()
    @FieldSize(100)
    public String getExcludeSchemas() {
        return excludeSchemas;
    }

    public void setExcludeSchemas(String excludeSchemas) {
        this.excludeSchemas = excludeSchemas;
    }

    @Status(red={STATUS_ERROR}, amber={STATUS_DISCONNECTED}, green={STATUS_CONNECTED})
    @Updatable(false)
    public String getStatus() {
        return status;
    }

    @Updatable(false)
    public String getErrorMessage() {
        return errorMessage;
    }

    @DateFormat("yyyy-MM-dd HH:mm:ss")
    @Updatable(false)
    public Date getLastTested() {
        return lastTested;
    }

    public DatabasePlatform getDatabasePlatform() {
        return databasePlatform;
    }

    public Pattern getIncludeSchemasPattern() {
        return includeSchemasPattern;
    }

    public Pattern getExcludeSchemasPattern() {
        return excludeSchemasPattern;
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
        Type[] result = new Type[types.size()];
        return types.toArray(result);
    }

    public Type getTypeByName(String typeName) {
        for (Type current : types) {
            if (current.getTypeName().equalsIgnoreCase(typeName)) {
                return current;
            }
        }
        logger.error("Could not find type: {}", typeName);
        return null;
    }

    public Database readModel() {
        return databasePlatform.readModel(this);
    }

    private static class TypeComparator implements Comparator<Type> {
        public int compare(Type o1, Type o2) {
            return o1.getTypeName().compareToIgnoreCase(o2.getTypeName());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionProvider that = (ConnectionProvider) o;

        if (databaseName != null ? !databaseName.equals(that.databaseName) : that.databaseName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return databaseName != null ? databaseName.hashCode() : 0;
    }
}
