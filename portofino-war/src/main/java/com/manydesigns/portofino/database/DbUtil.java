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

    public static int getSQLType(String type) {
        if ("VARCHAR".equalsIgnoreCase(type)){
            return Types.VARCHAR;
        }
        if ("BIT".equalsIgnoreCase(type)){
            return Types.BIT;
        }
        if ("BLOB".equalsIgnoreCase(type)){
            return Types.BLOB;
        }
        if ("BOOLEAN".equalsIgnoreCase(type)){
            return Types.BOOLEAN;
        }
        if ("CHAR".equalsIgnoreCase(type)){
            return Types.CHAR;
        }
        if ("CLOB".equalsIgnoreCase(type)){
            return Types.CLOB;
        }
        if ("DATE".equalsIgnoreCase(type)){
            return Types.DATE;
        }
        if ("DECIMAL".equalsIgnoreCase(type)){
            return Types.DECIMAL;
        }
        if ("DOUBLE".equalsIgnoreCase(type)){
            return Types.DOUBLE;
        }
        if ("FLOAT".equalsIgnoreCase(type)){
            return Types.FLOAT;
        }
        if ("INTEGER".equalsIgnoreCase(type)){
            return Types.INTEGER;
        }
        if ("NUMERIC".equalsIgnoreCase(type)){
            return Types.NUMERIC;
        }
        if ("SMALLINT".equalsIgnoreCase(type)){
            return Types.SMALLINT;
        }
        if ("TIME".equalsIgnoreCase(type)){
            return Types.TIME;
        }
        if ("TIMESTAMP".equalsIgnoreCase(type)){
            return Types.TIMESTAMP;
        }

        return 0;
    }


    public static org.hibernate.type.Type getHibernateType(String type) {
        if ("VARCHAR".equalsIgnoreCase(type)){
            return Hibernate.STRING;
        }
        if ("BIT".equalsIgnoreCase(type)){
            return Hibernate.BYTE;
        }
        if ("BOOLEAN".equalsIgnoreCase(type)){
            return Hibernate.BOOLEAN;
        }
        if ("CHAR".equalsIgnoreCase(type)){
            return Hibernate.CHARACTER;
        }
        if ("CLOB".equalsIgnoreCase(type)){
            return Hibernate.CLOB;
        }
        if ("BLOB".equalsIgnoreCase(type)){
            return Hibernate.BLOB;
        }
        if ("DATE".equalsIgnoreCase(type)){
            return Hibernate.DATE;
        }
        if ("DECIMAL".equalsIgnoreCase(type)){
            return Hibernate.BIG_DECIMAL;
        }
        if ("DOUBLE".equalsIgnoreCase(type)){
            return Hibernate.DOUBLE;
        }
        if ("FLOAT".equalsIgnoreCase(type)){
            return Hibernate.FLOAT;
        }
        if ("INTEGER".equalsIgnoreCase(type)){
            return Hibernate.INTEGER;
        }
        if ("NUMERIC".equalsIgnoreCase(type)){
            return Hibernate.FLOAT;
        }
        if ("SMALLINT".equalsIgnoreCase(type)){
            return Hibernate.INTEGER;
        }
        if ("TIME".equalsIgnoreCase(type)){
            return Hibernate.TIME;
        }
        if ("TIMESTAMP".equalsIgnoreCase(type)){
            return Hibernate.TIMESTAMP;
        }

        return null;
    }

}
