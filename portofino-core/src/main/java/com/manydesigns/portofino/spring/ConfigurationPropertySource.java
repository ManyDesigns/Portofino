package com.manydesigns.portofino.spring;
import org.apache.commons.configuration2.Configuration;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigurationPropertySource extends EnumerablePropertySource<Configuration> {

    public ConfigurationPropertySource(String name, Configuration source) {
        super(name, source);
    }

    @NotNull
    @Override
    public String[] getPropertyNames() {
        List<String> keys = new ArrayList<>();
        for (Iterator<String> it = source.getKeys(); it.hasNext(); ) {
            keys.add(it.next());
        }
        return keys.toArray(new String[0]);
    }

    @Override
    public Object getProperty(@NotNull String name) {
        return source.getProperty(name);
    }
}
