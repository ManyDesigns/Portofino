package com.manydesigns.elements.reflection.decorators;

import com.manydesigns.elements.reflection.AbstractAnnotatedAccessor;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ClassAccessorDecorator extends AbstractAnnotatedAccessor implements ClassAccessor {

    private final ClassAccessor delegate;
    private final PropertyAccessor[] properties;
    private final PropertyAccessor[] keyProperties;

    public ClassAccessorDecorator(ClassAccessor delegate) {
        ClassAccessor decoratorAccessor = getDecoratorAccessor();
        Object decorator = this;
        this.delegate = delegate;
        this.properties = new PropertyAccessor[delegate.getProperties().length];
        this.keyProperties = new PropertyAccessor[delegate.getKeyProperties().length];

        init(delegate, decoratorAccessor, decorator);
    }

    public ClassAccessorDecorator(ClassAccessor delegate, ClassAccessor decoratorAccessor, @Nullable Object decorator) {
        this.delegate = delegate;
        this.properties = new PropertyAccessor[delegate.getProperties().length];
        this.keyProperties = new PropertyAccessor[delegate.getKeyProperties().length];

        init(delegate, decoratorAccessor, decorator);
    }

    public ClassAccessorDecorator(ClassAccessor delegate, ClassAccessor decoratorAccessor) {
        this(delegate, decoratorAccessor, null);
    }

    private void init(ClassAccessor delegate, ClassAccessor decoratorAccessor, Object decorator) {
        int p = 0, k = 0;
        for(PropertyAccessor accessor : delegate.getProperties()) {
            try {
                PropertyAccessor decoratingProperty = decoratorAccessor.getProperty(accessor.getName());
                PropertyAccessorDecorator propertyAccessorDecorator = new PropertyAccessorDecorator(accessor, decoratingProperty);
                properties[p] = propertyAccessorDecorator;
                if(decoratingProperty.getType().isAssignableFrom(PropertyAccessor.class) && decorator != null) {
                    decoratingProperty.set(decorator, propertyAccessorDecorator);
                }
            } catch (NoSuchFieldException e) {
                properties[p] = accessor;
            }

            if(ArrayUtils.contains(delegate.getKeyProperties(), accessor)) {
                keyProperties[k++] = properties[p];
            }

            p++;
        }
        for(Annotation annotation : delegate.getAnnotations()) {
            this.annotations.put(annotation.getClass().getInterfaces()[0], annotation);
        }
        for(Annotation annotation : decoratorAccessor.getAnnotations()) {
            this.annotations.put(annotation.getClass().getInterfaces()[0], annotation);
        }
    }

    protected ClassAccessor getDecoratorAccessor() {
        return JavaClassAccessor.getClassAccessor(getClass());
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public PropertyAccessor getProperty(String propertyName) throws NoSuchFieldException {
        for(PropertyAccessor propertyAccessor : properties) {
            if(propertyAccessor.getName().equals(propertyName)) {
                return propertyAccessor;
            }
        }
        throw new NoSuchFieldException(propertyName);
    }

    @Override
    public PropertyAccessor[] getProperties() {
        return properties;
    }

    @Override
    public PropertyAccessor[] getKeyProperties() {
        return keyProperties;
    }

    @Override
    public Object newInstance() {
        return delegate.newInstance();
    }


}
