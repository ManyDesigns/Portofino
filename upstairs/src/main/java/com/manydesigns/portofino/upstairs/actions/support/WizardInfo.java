package com.manydesigns.portofino.upstairs.actions.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.Table;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WizardInfo {

    public Map connectionProvider;
    public String encryptionAlgorithm;
    public String newConnectionType;
    public List<Map> schemas;
    public String strategy;
    public List<TableInfo> tables;
    public TableInfo usersTable;
    public Column userNameProperty;
    public Column userPasswordProperty;
    public Column userEmailProperty;
    public Column userTokenProperty;
    public String adminGroupName;
    public TableInfo groupsTable;
    public Column groupNameProperty;
    public TableInfo userGroupTable;
    public Column groupLinkProperty;
    public Column userLinkProperty;

}
