/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    private final ClassAccessor delegate;
    private final PropertyAccessor[] properties;
    private final PropertyAccessor[] keyProperties;

    protected ClassAccessorDecorator(ClassAccessor delegate) {
        if(ClassAccessorDecorator.class.equals(getClass())) {
            throw new IllegalStateException("This constructor is supposed to be called on subclasses that act as decorator definitions");
        }
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
            this.annotations.put(annotation.annotationType(), annotation);
        }
        for(Annotation annotation : decoratorAccessor.getAnnotations()) {
            this.annotations.put(annotation.annotationType(), annotation);
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

    @Override
    public Class<?> getType() {
        return delegate.getType();
    }


}
