package com.manydesigns.elements.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A factory for annotation proxies.
 * @author Alessio Stalla - alessiostalla@gmail.com
 */
public class AnnotationFactory {
    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    private final ClassLoader classLoader;

    public AnnotationFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public AnnotationFactory() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public <T extends Annotation> T make(Class<T> annotationClass) {
        return make(annotationClass, Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> T make(Class<T> annotationClass, Map<String, Object> values) {
        getAnnotationMethods(annotationClass).forEach(m -> {
            if(m.getDefaultValue() == null && !values.containsKey(m.getName())) {
                throw new IllegalArgumentException(m.getName() + " has no default and is missing from values");
            }
        });
        return (T) Proxy.newProxyInstance(classLoader, new Class[] { annotationClass },
                (proxy, method, args) -> {
                    Object value = values.get(method.getName());
                    if (value == null) {
                        value = method.getDefaultValue();
                    }
                    return value;
                });
    }

    public static List<Method> getAnnotationMethods(Class<?> annotationClass) {
        List<Method> methods = new ArrayList<>();
        for (Method method : annotationClass.getMethods()) {
            if (method.getParameterTypes().length == 0 && method.getDeclaringClass().isAnnotation()) {
                methods.add(method);
            }
        }
        return methods;
    }

}
