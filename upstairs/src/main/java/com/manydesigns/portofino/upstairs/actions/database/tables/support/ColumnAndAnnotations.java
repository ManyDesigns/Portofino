package com.manydesigns.portofino.upstairs.actions.database.tables.support;

import com.manydesigns.portofino.model.database.Column;

import java.util.HashMap;
import java.util.Map;

public class ColumnAndAnnotations {

    private Column column;
    private Map annotations = new HashMap();

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public Map getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map annotations) {
        this.annotations = annotations;
    }
}
