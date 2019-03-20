package com.manydesigns.portofino.upstairs.actions.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.manydesigns.portofino.model.database.Reference;
import com.manydesigns.portofino.model.database.Table;

import java.util.ArrayList;
import java.util.List;

public class TableInfo {

    public String database;
    public String schema;
    public Table table;
    public boolean root = true;
    public boolean selected = false;
    public final List<Reference> children = new ArrayList<>();

    public TableInfo() {}

    public TableInfo(Table table) {
        this.database = table.getDatabaseName();
        this.schema = table.getSchemaName();
        this.table = table;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getQualifiedName() {
        return database + "." + schema + "." + table.getTableName();
    }
}
