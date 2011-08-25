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

package com.manydesigns.portofino.database;

import org.apache.commons.dbutils.DbUtils;
import org.hibernate.Hibernate;
import org.hibernate.type.NumericBooleanType;
import org.hibernate.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DbUtil {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static Logger logger = LoggerFactory.getLogger(DbUtil.class);

    public static final NumericBooleanType NUMERIC_BOOLEAN = new NumericBooleanType();

    public static void closeResultSetAndStatement(ResultSet rs) {
        try {
            DbUtils.closeQuietly(rs);
            Statement st = rs.getStatement();
            DbUtils.closeQuietly(st);
        } catch (Throwable e) {
            logger.debug("Could not close statement", e);
        }
    }


    /**
     * See getHibernateType(Class javaType, int jdbcType)
     * @param jdbcType jdbc type
     * @return the Hibernate type
     */
    @Deprecated()
    public static org.hibernate.type.Type getHibernateType(int jdbcType) {
        switch (jdbcType) {
            case Types.BIGINT:
                return Hibernate.LONG;
            case Types.BIT:
            case Types.BOOLEAN:
                return Hibernate.BOOLEAN;
            case Types.CHAR:
            case Types.VARCHAR:
                return Hibernate.STRING;
            case Types.DATE:
                return Hibernate.DATE;
            case Types.TIME:
                return Hibernate.TIME;
            case Types.TIMESTAMP:
                return Hibernate.TIMESTAMP;
            case Types.DECIMAL:
            case Types.NUMERIC:
                return Hibernate.BIG_DECIMAL;
            case Types.DOUBLE:
            case Types.REAL:
                return Hibernate.DOUBLE;
            case Types.FLOAT:
                return Hibernate.FLOAT;
            case Types.INTEGER:
                return Hibernate.INTEGER;
            case Types.SMALLINT:
                return Hibernate.SHORT;
            case Types.TINYINT:
                return Hibernate.BYTE;
            case Types.ARRAY:
            case Types.BINARY:
            case Types.BLOB:
            case Types.CLOB:
            case Types.LONGVARBINARY:
            case Types.LONGVARCHAR:
            case Types.VARBINARY:
                return Hibernate.CLOB;
            case Types.DATALINK:
            case Types.DISTINCT:
            case Types.JAVA_OBJECT:
            case Types.NULL:
            case Types.OTHER:
            case Types.REF:
            case Types.STRUCT:
            default:
                throw new Error("Unsupported type: " + jdbcType);
        }
    }

    public static String getHibernateTypeName(Class javaType, final int jdbcType) {
        org.hibernate.type.Type type = getHibernateType(javaType, jdbcType);
        if(type == null) {
            return null;
        }
        org.hibernate.type.Type basicType = TypeFactory.basic(type.getName());
        if(basicType != null) {
            return basicType.getName();
        } else {
            return type.getClass().getName();
        }
    }

    public static org.hibernate.type.Type getHibernateType(Class javaType, final int jdbcType) {
        if (javaType == Long.class) {
            return Hibernate.LONG;
        } else if (javaType == Short.class) {
            return Hibernate.SHORT;
        } else if (javaType == Integer.class) {
            return Hibernate.INTEGER;
        } else if (javaType == Byte.class) {
            return Hibernate.BYTE;
        } else if (javaType == Float.class) {
            return Hibernate.FLOAT;
        } else if (javaType == Double.class) {
            return Hibernate.DOUBLE;
        } else if (javaType == Character.class) {
            return Hibernate.CHARACTER;
        } else if (javaType == String.class) {
            return Hibernate.STRING;
        } else if (java.util.Date.class.isAssignableFrom(javaType)) {
            switch (jdbcType) {
                case Types.DATE:
                    return Hibernate.DATE;
                case Types.TIME:
                    return Hibernate.TIME;
                case Types.TIMESTAMP:
                    return Hibernate.TIMESTAMP;
                default:
                    throw new Error("Unsupported date type: " + jdbcType);
            }
        } else if (javaType == Boolean.class) {
            if(jdbcType == Types.BIT) {
                return Hibernate.BOOLEAN;
            } else if(jdbcType == Types.NUMERIC || jdbcType == Types.DECIMAL) {
                return NUMERIC_BOOLEAN;
            } else {
                throw new Error("Unsupported boolean type: " + jdbcType);
            }
        } else if (javaType == BigDecimal.class) {
            return Hibernate.BIG_DECIMAL;
        } else if (javaType == BigInteger.class) {
            return Hibernate.BIG_INTEGER;
        } else if (javaType == byte[].class) {
            return Hibernate.BLOB;
        } else {
            throw new Error("Unsupported java type: " + javaType);
        }
    }

}
