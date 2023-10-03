package com.manydesigns.elements.reflection.factories;

import com.manydesigns.elements.reflection.ClassAccessor;

public interface ClassAccessorFactory {

    boolean isApplicable(Class<?> type);
    ClassAccessor buildFor(Class<?> type);

}
