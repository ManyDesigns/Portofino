package com.manydesigns.portofino.liquibase.databases;

import liquibase.database.core.MariaDBDatabase;

public class PortofinoMariaDBDatabase extends MariaDBDatabase {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supportsSequences() {
        return false; //Until https://liquibase.jira.com/browse/CORE-3536 is resolved, Liquibase doesn't really support sequences on MariaDB
    }
}
