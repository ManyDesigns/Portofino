/*
 * Copyright (C) 2005-2022 ManyDesigns srl.  All rights reserved.
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

import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents all known information about a data type in a database.
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*  @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Type {
    public static final String copyright =
            "Copyright (C) 2005-2022 ManyDesigns srl";

    public final static Logger logger = LoggerFactory.getLogger(Type.class);

    protected final String typeName;
    /**
     * The JDBC type number, as returned by the JDBC driver.
     */
    protected final int jdbcType;
    protected final boolean autoincrement;
    protected final Integer maximumPrecision;
    protected final String literalPrefix;
    protected final String literalSuffix;
    protected final boolean nullable;
    protected final boolean caseSensitive;
    protected final boolean searchable;
    protected final int minimumScale;
    protected final int maximumScale;
    protected final Boolean precisionRequired;
    protected final Boolean scaleRequired;

    public Type(String typeName, int jdbcType, Integer maximumPrecision,
                String literalPrefix, String literalSuffix,
                boolean nullable, boolean caseSensitive, boolean searchable,
                boolean autoincrement, int minimumScale, int maximumScale) {
        this.typeName = typeName;
        this.jdbcType = jdbcType;
        this.maximumPrecision = maximumPrecision;
        this.literalPrefix = literalPrefix;
        this.literalSuffix = literalSuffix;
        this.nullable = nullable;
        this.caseSensitive = caseSensitive;
        this.searchable = searchable;
        this.autoincrement = autoincrement;
        this.minimumScale = minimumScale;
        this.maximumScale = maximumScale;

        this.precisionRequired = null;
        this.scaleRequired = null;

    }

    public Type(String typeName, int jdbcType, Integer maximumPrecision,
                String literalPrefix, String literalSuffix,
                boolean nullable, boolean caseSensitive, boolean searchable,
                boolean autoincrement, int minimumScale, int maximumScale,
                boolean precisionRequired, boolean scaleRequired) {
        this.typeName = typeName;
        this.jdbcType = jdbcType;
        this.maximumPrecision = maximumPrecision;
        this.literalPrefix = literalPrefix;
        this.literalSuffix = literalSuffix;
        this.nullable = nullable;
        this.caseSensitive = caseSensitive;
        this.searchable = searchable;
        this.autoincrement = autoincrement;
        this.minimumScale = minimumScale;
        this.maximumScale = maximumScale;

        this.precisionRequired = precisionRequired;
        this.scaleRequired = scaleRequired;

    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public String getTypeName() {
        return typeName;
    }

    public int getJdbcType() {
        return jdbcType;
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    public Integer getMaximumPrecision() {
        return maximumPrecision;
    }

    public String getLiteralPrefix() {
        return literalPrefix;
    }

    public String getLiteralSuffix() {
        return literalSuffix;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public Integer getMinimumScale() {
        return minimumScale;
    }

    public Integer getMaximumScale() {
        return maximumScale;
    }

    @Override
    public String toString() {
        return "Type{" +
                "typeName='" + typeName + '\'' +
                ", jdbcType=" + jdbcType +
                ", autoincrement=" + autoincrement +
                ", maximumPrecision=" + maximumPrecision +
                ", literalPrefix='" + literalPrefix + '\'' +
                ", literalSuffix='" + literalSuffix + '\'' +
                ", nullable=" + nullable +
                ", caseSensitive=" + caseSensitive +
                ", searchable=" + searchable +
                ", minimumScale=" + minimumScale +
                ", maximumScale=" + maximumScale +
                '}';
    }

    public boolean isPrecisionRequired() {
        if (precisionRequired!= null){
            return precisionRequired;
        }
        switch (jdbcType) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.DECIMAL:
            case Types.NUMERIC:
                return true;
            default:
                return false;
        }

    }

    public boolean isScaleRequired() {
        if (scaleRequired!= null){
            return scaleRequired;
        }
        switch (jdbcType) {
            case Types.DECIMAL:
            case Types.NUMERIC:
                return true;
            default:
                return false;
        }
    }

    public boolean isNumeric() {
        switch (jdbcType) {
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.INTEGER:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.BIGINT:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.REAL:
                return true;
            default: return false;
        }
    }

}
