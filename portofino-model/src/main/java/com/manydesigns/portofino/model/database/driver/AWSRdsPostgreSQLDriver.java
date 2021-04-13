package com.manydesigns.portofino.model.database.driver;

public class AWSRdsPostgreSQLDriver extends AWSRdsDriver {

    public static final String SUBPREFIX = "postgresql";

    static {
        AWSRdsDriver.register(new AWSRdsPostgreSQLDriver());
    }

    public AWSRdsPostgreSQLDriver() {
        super();
    }

    @Override
    public String getPropertySubprefix() {
        return SUBPREFIX;
    }

    @Override
    public String getDefaultDriverClass() {
        return "org.postgresql.Driver";
    }
}
