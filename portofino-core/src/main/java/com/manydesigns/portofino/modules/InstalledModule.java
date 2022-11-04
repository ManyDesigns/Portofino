package com.manydesigns.portofino.modules;

public class InstalledModule {

    protected final String version;

    public InstalledModule(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
