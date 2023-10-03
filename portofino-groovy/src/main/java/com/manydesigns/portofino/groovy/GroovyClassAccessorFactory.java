package com.manydesigns.portofino.groovy;

import com.manydesigns.elements.reflection.factories.ClassAccessorFactory;
import groovy.lang.GroovyObject;

public class GroovyClassAccessorFactory implements ClassAccessorFactory {
    @Override
    public boolean isApplicable(Class<?> type) {
        return GroovyObject.class.isAssignableFrom(type);
    }

    @Override
    public GroovyClassAccessor buildFor(Class<?> type) {
        return new GroovyClassAccessor(type);
    }
}
