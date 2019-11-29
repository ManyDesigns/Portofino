package com.manydesigns.elements.options;

import java.util.List;

public class MemoizingOptionProvider implements OptionProvider {

    protected List<OptionProvider.Option> options;
    protected final OptionProvider delegate;

    public MemoizingOptionProvider(OptionProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<Option> getOptions() {
        if(options == null) {
            options = delegate.getOptions();
        }
        return options;
    }
}
