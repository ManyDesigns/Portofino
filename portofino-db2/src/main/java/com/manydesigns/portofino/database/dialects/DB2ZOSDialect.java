/*
 * Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.database.dialects;

import org.hibernate.dialect.DB2Dialect;

/*
* @author Manuel Durán Aguete     - manuel@aguete.org
*/
public class DB2ZOSDialect extends DB2Dialect {
    @Override
    public boolean supportsSequences() {
        return true;
    }

    @Override
    public String getIdentitySelectString() {
        return "select identity_val_local() from sysibm.sysdummy1";
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsLimitOffset() {
        return false;
    }

    @Override
    public String getLimitString(String sql, int offset, int limit) {
        return new StringBuffer(sql.length() + 40)
                .append(sql)
                .append(" fetch first ")
                .append(limit)
                .append(" rows only ")
                .toString();
    }

    @Override
    public boolean useMaxForLimit() {
        return true;
    }

    @Override
    public boolean supportsVariableLimit() {
        return false;
    }

    @Override
    public String getSequenceNextValString(String sequenceName) {
        return "select nextval for " + sequenceName + " from sysibm.sysdummy1";
    }

    @Override
    public String getCreateSequenceString(String sequenceName) {
        return "create sequence " + sequenceName + " as integer start with 1 increment by 1 minvalue 1 nomaxvalue nocycle nocache"; //simple default settings..
    }

    @Override
    public String getDropSequenceString(String sequenceName) {
        return "drop sequence " + sequenceName + " restrict";
    }

    @Override
    public String getQuerySequencesString() {
        return "select name from sysibm.syssequences";
    }

    @Override
    public String getForUpdateString() {
        return " WITH RS USE AND KEEP EXCLUSIVE LOCKS";
    }

}
