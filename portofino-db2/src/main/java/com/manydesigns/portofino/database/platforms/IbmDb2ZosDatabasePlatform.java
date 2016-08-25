package com.manydesigns.portofino.database.platforms;

import com.manydesigns.portofino.model.database.ConnectionProvider;
import com.manydesigns.portofino.database.dialects.DB2ZOSDialect;

public class IbmDb2ZosDatabasePlatform extends AbstractDatabasePlatform{
    public static final String copyright =
            "Copyright (C) 2005-2016, ManyDesigns srl";

    public final static String DESCRIPTION = "IBM DB2 for ZOS";
    public final static String STANDARD_DRIVER_CLASS_NAME =
            "com.ibm.db2.jcc.DB2Driver";

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public IbmDb2ZosDatabasePlatform()  {
        super(new DB2ZOSDialect(), "jdbc:db2://<host>[:<port>]/<database_name>");
    }

    //**************************************************************************
    // Implementation of DatabaseAbstraction
    //**************************************************************************

    public boolean isDialectAutodetected() {
        return false;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    public String getStandardDriverClassName() {
        return STANDARD_DRIVER_CLASS_NAME;
    }

    public boolean isApplicable(ConnectionProvider connectionProvider) {
        return connectionProvider.getDatabaseProductName().startsWith("DB2");
    }
}
