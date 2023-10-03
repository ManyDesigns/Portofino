package com.manydesigns.elements.reflection.factories;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClassAccessorFactories {

    public static final List<ClassAccessorFactory> LIST = new CopyOnWriteArrayList<>();

    public static ClassAccessor get(Class<?> type) {
        for (ClassAccessorFactory factory : LIST) {
            if (factory.isApplicable(type)) {
                return factory.buildFor(type);
            }
        }
        return JavaClassAccessor.getClassAccessor(type);
    }

}
