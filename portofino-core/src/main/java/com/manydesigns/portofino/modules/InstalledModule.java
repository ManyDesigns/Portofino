package com.manydesigns.portofino.modules;

public class InstalledModule {

    protected String version;

    public InstalledModule() {}

    public InstalledModule(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
