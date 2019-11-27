package com.manydesigns.elements.options;

import java.util.List;

public interface OptionProvider {

    List<Option> getOptions();

    class Option {
        final Object[] values;
        final String[] labels;
        final boolean active;

        public Option(Object[] values, String[] labels, boolean active) {
            this.values = values;
            this.labels = labels;
            this.active = active;
        }

        public Object[] getValues() {
            return values;
        }

        public String[] getLabels() {
            return labels;
        }

        public boolean isActive() {
            return active;
        }
    }
}
