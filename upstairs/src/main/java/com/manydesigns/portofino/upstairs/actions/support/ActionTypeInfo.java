package com.manydesigns.portofino.upstairs.actions.support;

public class ActionTypeInfo {
    public String className;
    public String name;
    public String description;
    public boolean supportsDetail;

    public ActionTypeInfo(String className, String name, String description, boolean supportsDetail) {
        this.className = className;
        this.name = name;
        this.description = description;
        this.supportsDetail = supportsDetail;
    }
}
