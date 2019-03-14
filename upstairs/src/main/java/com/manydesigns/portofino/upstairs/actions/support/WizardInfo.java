package com.manydesigns.portofino.upstairs.actions.support;

import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.Table;

import java.util.List;
import java.util.Map;

public class WizardInfo {

    public Map connectionProvider;
    public String encryptionAlgorithm;
    public String newConnectionType;
    public List<Map> schemas;
    public String strategy;
    public List<TableInfo> tables;
    public Column userIdProperty;
    public Column userNameProperty;
    public Column userPasswordProperty;
    public Table usersTable;

}
