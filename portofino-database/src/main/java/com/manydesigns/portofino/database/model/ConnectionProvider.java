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

package com.manydesigns.portofino.database.model;

import com.manydesigns.elements.annotations.DateFormat;
import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Updatable;
import com.manydesigns.portofino.model.Unmarshallable;
import com.manydesigns.portofino.database.model.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.model.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.database.model.platforms.GenericDatabasePlatform;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.Date;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ConnectionProvider implements Unmarshallable {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final static String STATUS_DISCONNECTED = "disconnected";
    public final static String STATUS_CONNECTED = "connected";
    public final static String STATUS_ERROR = "error";

    //**************************************************************************
    // Fields (initialized and detected values)
    //**************************************************************************

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
    protected Database database;
    protected String hibernateDialect;
    protected Configuration configuration;

    public static final Logger logger = LoggerFactory.getLogger(ConnectionProvider.class);

    public ConnectionProvider() {
        types = new ArrayList<>();
    }

    public void init(DatabasePlatformsRegistry databasePlatformsRegistry) {
        configuration = databasePlatformsRegistry.getPortofinoConfiguration();
        String databaseName = getDatabase().getDatabaseName();
        try(Connection conn = acquireConnection()) {
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
            try(ResultSet typeRs = metadata.getTypeInfo()) {
                while (typeRs.next()) {
                    readType(typeRs);
                }
                fixMissingTypeAliases(types);
                types.sort(new TypeComparator());
            }

            databasePlatform = databasePlatformsRegistry.findApplicableAbstraction(this);
            if (databasePlatform == null) {
                errorMessage = MessageFormat.format("Database platform not found for {0}, using generic one", databaseProductName);
                logger.warn(errorMessage);
                databasePlatform = new GenericDatabasePlatform();
            } else {
                errorMessage = null;
            }

            status = STATUS_CONNECTED;
        } catch (Throwable e) {
            status = STATUS_ERROR;
            errorMessage = e.getMessage();
            logger.warn("Could not create database platform for " + databaseName, e);
        }
    }

    protected void fixMissingTypeAliases(List<Type> types) {
        Type numericType = null;
        Type decimalType = null;
        for(Type type : types) {
            if(type.getJdbcType() == Types.NUMERIC) {
                numericType = type;
            } else if(type.getJdbcType() == Types.DECIMAL) {
                decimalType = type;
            }
        }
        if(numericType == null && decimalType != null) {
            numericType = new Type(
                    "NUMERIC", Types.NUMERIC, decimalType.getMaximumPrecision(), decimalType.getLiteralPrefix(),
                    decimalType.getLiteralSuffix(), decimalType.isNullable(), decimalType.isCaseSensitive(),
                    decimalType.isSearchable(), decimalType.isAutoincrement(), decimalType.getMinimumScale(),
                    decimalType.getMaximumScale(), decimalType.isPrecisionRequired(), decimalType.isScaleRequired());
            types.add(numericType);
            logger.info("Added NUMERIC type as an alias of DECIMAL");
        } else if(decimalType == null && numericType != null) {
            decimalType = new Type(
                    "DECIMAL", Types.DECIMAL, numericType.getMaximumPrecision(), numericType.getLiteralPrefix(),
                    numericType.getLiteralSuffix(), numericType.isNullable(), numericType.isCaseSensitive(),
                    numericType.isSearchable(), numericType.isAutoincrement(), numericType.getMinimumScale(),
                    numericType.getMaximumScale(), numericType.isPrecisionRequired(), numericType.isScaleRequired());
            types.add(decimalType);
            logger.info("Added DECIMAL type as an alias of NUMERIC");
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
        Integer maximumPrecision;
        Object maximumPrecisionObj = typeRs.getObject("PRECISION");
        if (maximumPrecisionObj instanceof Number) {
            maximumPrecision = ((Number)maximumPrecisionObj).intValue();
        } else {
            maximumPrecision = null;
            logger.warn("Cannot get maximum precision for type: {} value: {}", typeName, maximumPrecisionObj);
        }
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

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        setDatabase((Database) parent);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

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

    @XmlAttribute(required = false)
    public String getHibernateDialect() {
        return hibernateDialect;
    }

    public void setHibernateDialect(String hibernateDialect) {
        this.hibernateDialect = hibernateDialect;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Type[] getTypes() {
        Type[] result = new Type[types.size()];
        return types.toArray(result);
    }

    public boolean isHibernateDialectAutodetected() {
        return StringUtils.isBlank(hibernateDialect) &&
                (getDatabasePlatform() == null || getDatabasePlatform().isDialectAutodetected());
    }

    public String getActualHibernateDialectName() {
        if(StringUtils.isBlank(hibernateDialect)) {
            return getDatabasePlatform().getHibernateDialect();
        } else {
            return hibernateDialect;
        }
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

        if (database != null ? !database.equals(that.database) : that.database != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return database != null ? database.hashCode() : 0;
    }
}
