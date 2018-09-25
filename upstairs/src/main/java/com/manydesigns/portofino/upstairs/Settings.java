package com.manydesigns.portofino.upstairs;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Required;

public class Settings {

    @Required
    @Label("Application name")
    public String appName;

    @Label("Application version")
    public String appVersion;

    @Required
    @Label("Preload Groovy pages at startup")
    public boolean preloadGroovyPages;
    @Required
    @Label("Preload Groovy shared classes at startup")
    public boolean preloadGroovyClasses;

}