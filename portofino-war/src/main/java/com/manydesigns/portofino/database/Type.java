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

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Type {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final String typeName;
    protected final int jdbcType;
    protected final boolean autoincrement;
    protected final int maximumPrecision;
    protected final String literalPrefix;
    protected final String literalSuffix;
    protected final boolean nullable;
    protected final boolean caseSensitive;
    protected final boolean searchable;
    protected final short minimumScale;
    protected final short maximumScale;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Type(String typeName, int jdbcType, int maximumPrecision,
                String literalPrefix, String literalSuffix,
                boolean nullable, boolean caseSensitive, boolean searchable,
                boolean autoincrement, short minimumScale, short maximumScale) {
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

    public Class getDefaultJavaType() {
        switch (jdbcType) {
            case Types.BIGINT:
                return Long.class;
            case Types.BIT:
            case Types.BOOLEAN:
                return Boolean.class;
            case Types.CHAR:
            case Types.VARCHAR:
                return String.class;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return Date.class;
            case Types.DECIMAL:
            case Types.NUMERIC:
                return BigDecimal.class;
            case Types.DOUBLE:
            case Types.REAL:
                return Double.class;
            case Types.FLOAT:
                return Float.class;
            case Types.INTEGER:
                return Integer.class;
            case Types.SMALLINT:
                return Short.class;
            case Types.TINYINT:
                return Byte.class;
            case Types.ARRAY:
            case Types.BINARY:
            case Types.BLOB:
            case Types.CLOB:
            case Types.DATALINK:
            case Types.DISTINCT:
            case Types.JAVA_OBJECT:
            case Types.LONGVARBINARY:
            case Types.LONGVARCHAR:
            case Types.NULL:
            case Types.OTHER:
            case Types.REF:
            case Types.STRUCT:
            case Types.VARBINARY:
            default:
                throw new Error("Unsupported type: " + jdbcType);
        }
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    public int getMaximumPrecision() {
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

    public short getMinimumScale() {
        return minimumScale;
    }

    public short getMaximumScale() {
        return maximumScale;
    }
}
