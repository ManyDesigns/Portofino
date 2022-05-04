package com.manydesigns.portofino.microservices.boot;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class SpringEnvironmentConfiguration extends AbstractConfiguration {

    protected final Environment environment;
    /** A flag whether trimming of property values should be disabled. */
    private boolean trimmingDisabled;

    public SpringEnvironmentConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void addPropertyDirect(String key, Object value) {
        throw new UnsupportedOperationException("This configuration is read-only.");
    }

    @Override
    protected void clearPropertyDirect(String key) {
        if (environment.containsProperty(key)) {
            throw new UnsupportedOperationException("This configuration is read-only.");
        }
    }

    @Override
    protected Iterator<String> getKeysInternal() {
        return Collections.emptyIterator();
    }

    @Override
    protected Object getPropertyInternal(String key) {
        String value = environment.getProperty(key);
        if (value != null) {
            final Collection<String> list = getListDelimiterHandler().split(value, !isTrimmingDisabled());
            return list.size() > 1 ? list : list.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    protected boolean isEmptyInternal() {
        return false;
    }

    @Override
    protected boolean containsKeyInternal(String key) {
        return environment.containsProperty(key);
    }


    /**
     * Returns the flag whether trimming of property values is disabled.
     *
     * @return <b>true</b> if trimming of property values is disabled;
     *         <b>false</b> otherwise
     */
    public boolean isTrimmingDisabled()
    {
        return trimmingDisabled;
    }

    /**
     * Sets a flag whether trimming of property values is disabled. This flag is
     * only evaluated if list splitting is enabled. Refer to the header comment
     * for more information about list splitting and trimming.
     *
     * @param trimmingDisabled a flag whether trimming of property values should
     *        be disabled
     */
    public void setTrimmingDisabled(final boolean trimmingDisabled)
    {
        this.trimmingDisabled = trimmingDisabled;
    }
}
