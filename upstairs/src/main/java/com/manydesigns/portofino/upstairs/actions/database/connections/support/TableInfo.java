package com.manydesigns.portofino.upstairs.actions.database.connections.support;

import com.manydesigns.portofino.model.database.Reference;
import com.manydesigns.portofino.model.database.Table;

import java.util.ArrayList;
import java.util.List;

public class TableInfo {

    public final Table table;
    public boolean root = true;
    public final List<Reference> children = new ArrayList<>();

    public TableInfo(Table table) {
        this.table = table;
    }
}
