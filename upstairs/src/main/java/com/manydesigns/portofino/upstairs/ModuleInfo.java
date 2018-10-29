package com.manydesigns.portofino.upstairs;

import com.manydesigns.elements.annotations.Status;

public final class ModuleInfo {
    public String moduleClass;
    public String name;
    public String version;
    @Status(red = { "FAILED", "DESTROYED" }, amber = { "CREATED", "STOPPED" }, green = { "ACTIVE", "STARTED" })
    public String status;
}
