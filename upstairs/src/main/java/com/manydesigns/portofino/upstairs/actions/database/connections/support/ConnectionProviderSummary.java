package com.manydesigns.portofino.upstairs.actions.database.connections.support;

public class ConnectionProviderSummary {

    public String name;
    public String status;
    public String description;

    public ConnectionProviderSummary(String name, String description, String status) {
        this.name = name;
        this.status = status;
        this.description = description;
    }
}
