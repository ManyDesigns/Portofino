package com.manydesigns.portofino.model.database.platforms;

import com.manydesigns.portofino.model.database.ConnectionProvider;

public class GenericDatabasePlatform extends AbstractDatabasePlatform {

    public GenericDatabasePlatform() {
        super(null, "jdbc:");
        status = DatabasePlatform.STATUS_OK;
    }

    @Override
    public String getDescription() {
        return "Generic database platform";
    }

    @Override
    public String getStandardDriverClassName() {
        return null;
    }

    @Override
    public boolean isApplicable(ConnectionProvider connectionProvider) {
        return true;
    }
}
