package com.manydesigns.elements.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class AbstractAnnotatedAccessor implements AnnotatedElement {
    protected final Map<Class, Annotation> annotations;

    public AbstractAnnotatedAccessor() {
        this.annotations = new HashMap<Class, Annotation>();
    }

        //**************************************************************************
    // AnnotatedElement implementation
    //**************************************************************************

    public boolean isAnnotationPresent(Class<? extends java.lang.annotation.Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotationClass) {
        return (T) annotations.get(annotationClass);
    }

    public java.lang.annotation.Annotation[] getAnnotations() {
        Collection<Annotation> annotationCollection = annotations.values();
        Annotation[] result = new Annotation[annotationCollection.size()];
        annotationCollection.toArray(result);
        return result;
    }

    public Annotation[] getDeclaredAnnotations() {
        return getAnnotations();
    }
}
