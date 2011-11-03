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

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class StringBooleanType implements UserType, ParameterizedType {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    private int sqlType = Types.CHAR;

    public static final String NULL = new StringBuilder("null").toString();

    private String trueString = "T";
    private String falseString = "F";

    public int[] sqlTypes() {
        return new int[] { sqlType };
    }

    public Class returnedClass() {
        return Boolean.class;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return ObjectUtils.equals(x, y);
    }

    public int hashCode(Object x) throws HibernateException {
        return ObjectUtils.hashCode(x);
    }

    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
        throws HibernateException, SQLException {
        String value = resultSet.getString(names[0]);
        if(value != null) {
            if(value.trim().equalsIgnoreCase(trueString)) {
                return Boolean.TRUE;
            } else if(value.trim().equalsIgnoreCase(falseString)) {
                return Boolean.FALSE;
            } else {
                throw new HibernateException("Invalid boolean value: " + value);
            }
        } else {
            if(trueString == null) {
                return true;
            } else if(falseString == null) {
                return false;
            } else {
                return null;
            }
        }
    }

    public void nullSafeSet(PreparedStatement statement, Object value, int index)
        throws HibernateException, SQLException {
        if(value == null) {
            if(trueString != null && falseString != null) {
                statement.setString(index, null);
            } else {
                throw new HibernateException("Null is not supported as a boolean value for this type");
            }
        } else {
            statement.setString(index, (Boolean) value ? trueString : falseString);
        }
    }

    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    public boolean isMutable() {
        return false;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    public String getTrueString() {
        return trueString;
    }

    public String getFalseString() {
        return falseString;
    }

    public void setParameterValues(Properties parameters) {
        //Strings are compared with == and != on purpose
        String nullValue = new String();
        String newString = parameters.getProperty("true", nullValue);
        if(newString != nullValue) {
            trueString = newString != NULL ? newString : null;
        }
        newString = parameters.getProperty("false", nullValue);
        if(newString != nullValue) {
            falseString = newString != NULL ? newString : null;
        }
        if((trueString == falseString) || ((trueString != null) && trueString.equals(falseString))) {
            throw new IllegalArgumentException("trueString and falseString must be distinct");
        }
        String sqlTypeCode = parameters.getProperty("sqlType");
        if(sqlTypeCode != null) {
            sqlType = Integer.parseInt(sqlTypeCode);
        }
    }
}
