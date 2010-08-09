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

import com.manydesigns.elements.logging.LogUtil;
import org.hibernate.Hibernate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class DbUtil {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static Logger logger = LogUtil.getLogger(DbUtil.class);

    public static void closeResultSetStatementAndConnection(ResultSet rs,
                                                            Statement st,
                                                            Connection conn) {
        closeResultSet(rs);
        closeStatement(st);
        closeConnection(conn);
    }

    public static void closeResultSetAndStatement(ResultSet rs, Statement st) {
        closeResultSet(rs);
        closeStatement(st);
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) try {
            rs.close();
        } catch (Exception e) {
            LogUtil.finer(logger, "Could not close result set", e);
        }
    }

    public static void closeResultSetAndStatement(ResultSet rs) {
        closeResultSet(rs);
        try {
            Statement st = rs.getStatement();
            if (st != null) {
                st.close();
            }
        } catch (Exception e) {
            LogUtil.finer(logger, "Could not close statement", e);
        }
    }

    public static void closeStatement(Statement st) {
        if (st != null) try {
            st.close();
        } catch (Exception e) {
            LogUtil.finer(logger, "Could not close statement", e);
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) try {
            conn.close();
        } catch (Exception e) {
            LogUtil.finer(logger, "Could not close connection", e);
        }
    }


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

}
