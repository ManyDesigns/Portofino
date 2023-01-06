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

package com.manydesigns.portofino.database.model.platforms;

import com.manydesigns.portofino.database.model.Column;
import com.manydesigns.portofino.database.model.ConnectionProvider;
import com.manydesigns.portofino.database.model.Type;
import com.manydesigns.portofino.model.Annotation;
import org.apache.commons.dbutils.DbUtils;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class AbstractDatabasePlatform implements DatabasePlatform {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final static String[] tableTypes = {"TABLE"};
    public static final String TABLE_CAT = "TABLE_CAT";
    public static final String TABLE_CATALOG = "TABLE_CATALOG";
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
    protected String hibernateDialect;
    protected String connectionStringTemplate;
    protected final List<TypeProvider> additionalTypeProviders = new ArrayList<>();

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractDatabasePlatform.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public AbstractDatabasePlatform(String hibernateDialect, String connectionStringTemplate) {
        this.hibernateDialect = hibernateDialect;
        this.connectionStringTemplate = connectionStringTemplate;
        status = DatabasePlatform.STATUS_CREATED;
    }

    //**************************************************************************
    // Implementation of DatabasePlatform
    //**************************************************************************

    public void test() {
        boolean success = DbUtils.loadDriver(getStandardDriverClassName());
        if (success) {
            status = DatabasePlatform.STATUS_OK;
        } else {
            status = DatabasePlatform.STATUS_DRIVER_NOT_FOUND;
        }
    }

    public String getStatus() {
        return status;
    }

    @Override
    public TypeDescriptor getDatabaseSpecificType(Column column) {
        return null;
    }

    @Override
    public List<Annotation> getAdditionalAnnotations(Column column) {
        return Collections.emptyList();
    }

    public String getHibernateDialect() {
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
    // Types
    //**************************************************************************

    @Override
    public List<TypeProvider> getAdditionalTypeProviders() {
        return additionalTypeProviders;
    }

    @Nullable
    @Override
    public Class<?> getDefaultJavaType(int jdbcType, String typeName, Integer precision, Integer scale) {
        for (TypeProvider typeProvider : additionalTypeProviders) {
            Class<?> defaultJavaType = typeProvider.getDefaultJavaType(jdbcType, typeName, precision, scale);
            if (defaultJavaType != null) {
                logger.debug(
                        "Additional type provider " + typeProvider + " returned "+ defaultJavaType + " for" +
                        jdbcType + ", " + typeName + ", " + precision + ", " + scale);
                return defaultJavaType;
            }
        }
        switch (jdbcType) {
            case Types.BIGINT:
                return Long.class;
            case Types.BIT:
            case Types.BOOLEAN:
                return Boolean.class;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.CLOB:
            case Types.LONGVARCHAR:
                return String.class;
            case Types.DATE:
                return java.sql.Date.class;
            case Types.TIME:
                return Time.class;
            case Types.TIMESTAMP:
                return Timestamp.class;
            case Types.DECIMAL:
            case Types.NUMERIC:
                if(scale != null && scale > 0) {
                    return BigDecimal.class;
                } else {
                    return getDefaultNumericType(precision);
                }
            case Types.DOUBLE:
            case Types.REAL:
                return Double.class;
            case Types.FLOAT:
                return Float.class;
            case Types.INTEGER:
                return getDefaultNumericType(precision);
            case Types.SMALLINT:
                return Short.class;
            case Types.TINYINT:
                return Byte.class;
            case Types.BINARY:
            case Types.BLOB:
            case Types.LONGVARBINARY:
            case Types.VARBINARY:
                return byte[].class;
            case Types.ARRAY:
                return java.sql.Array.class;
            case Types.DATALINK:
                return java.net.URL.class;
            case Types.DISTINCT:
            case Types.JAVA_OBJECT:
                return Object.class;
            case Types.NULL:
            case Types.REF:
                return java.sql.Ref.class;
            case Types.STRUCT:
                return java.sql.Struct.class;
            default:
                logger.warn("Unsupported JDBC type: {}", jdbcType);
                return null;
        }
    }

    @Nullable
    public Class<?> getDefaultJavaType(Type type) {
        return getDefaultJavaType(type.getJdbcType(), type.getTypeName(), type.getMaximumPrecision(), type.getMaximumScale());
    }

    protected Class<? extends Number> getDefaultNumericType(Integer precision) {
        if(precision == null) {
            return BigInteger.class;
        }
        if(precision < Math.log10(Integer.MAX_VALUE)) {
            return Integer.class;
        } else if(precision < Math.log10(Long.MAX_VALUE)) {
            return Long.class;
        } else {
            return BigInteger.class;
        }
    }


    @Override
    public Class[] getAvailableJavaTypes(Type type, Integer length) {
        for (TypeProvider typeProvider : additionalTypeProviders) {
            Class[] javaTypes = typeProvider.getAvailableJavaTypes(type, length);
            if (javaTypes != null) {
                logger.debug(
                        "Additional type provider " + typeProvider + " returned " + Arrays.toString(javaTypes) +
                                " for" + type + " with length " + length);
                return javaTypes;
            }
        }
        if(type.isNumeric()) {
            return new Class[] {
                    Integer.class, Long.class, Byte.class, Short.class,
                    Float.class, Double.class, BigInteger.class, BigDecimal.class,
                    Boolean.class };
        } else {
            Class<?> defaultJavaType = getDefaultJavaType(type);
            if(defaultJavaType == String.class) {
                if(length != null && length < 256) {
                    return new Class[] { String.class, Boolean.class };
                } else {
                    return new Class[] { String.class };
                }
            } else if(defaultJavaType == Timestamp.class) {
                return new Class[] { Timestamp.class, DateTime.class, java.sql.Date.class, LocalDateTime.class, ZonedDateTime.class, Instant.class };
            } else if(defaultJavaType == java.sql.Date.class) {
                return new Class[] { java.sql.Date.class, DateTime.class, LocalDate.class, Timestamp.class }; //TODO Joda LocalDate as well?
            } else {
                if(defaultJavaType != null) {
                    return new Class[] { defaultJavaType };
                } else {
                    return new Class[] { Object.class };
                }
            }
        }
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    public List<String[]> getSchemaNames(DatabaseMetaData databaseMetaData) throws SQLException {
        List<String[]> schemaNames = new ArrayList<>();
        try(ResultSet rs = databaseMetaData.getSchemas()) {
            while(rs.next()) {
                String schemaName = rs.getString(TABLE_SCHEM);
                String schemaCatalog = rs.getString(getCatalogColumnName());
                schemaNames.add(new String[] { schemaCatalog, schemaName });
            }
        }
        return schemaNames;
    }

    protected String getCatalogColumnName() {
        return TABLE_CATALOG;
    }
}
