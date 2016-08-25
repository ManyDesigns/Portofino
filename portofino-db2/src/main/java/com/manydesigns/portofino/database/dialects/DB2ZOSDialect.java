package com.manydesigns.portofino.database.dialects;

import org.hibernate.dialect.DB2Dialect;


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
