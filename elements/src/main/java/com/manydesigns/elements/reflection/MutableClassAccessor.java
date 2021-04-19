/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.reflection;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MutableClassAccessor extends AbstractAnnotatedAccessor implements ClassAccessor {
    
    protected String name;
    protected Class<?> type;
    protected final List<PropertyAccessor> properties = new ArrayList<>();
    protected final List<PropertyAccessor> keyProperties = new ArrayList<>();
    
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public PropertyAccessor getProperty(String propertyName) throws NoSuchFieldException {
        for(PropertyAccessor p : properties) {
            if(p.getName().equals(propertyName)) {
                return p;
            }
        }
        throw new NoSuchFieldException(propertyName);
    }

    @Override
    public PropertyAccessor[] getProperties() {
        return (PropertyAccessor[]) properties.toArray();
    }

    @Override
    public PropertyAccessor[] getKeyProperties() {
        return (PropertyAccessor[]) keyProperties.toArray();
    }

    @Override
    public Object newInstance() {
        throw new UnsupportedOperationException();
    }
    
    public void addProperty(PropertyAccessor propertyAccessor) {
        properties.add(propertyAccessor);
    }
    
    public void addKeyProperty(PropertyAccessor propertyAccessor) {
        addProperty(propertyAccessor);
        keyProperties.add(propertyAccessor);
    }
    
    public List<PropertyAccessor> getMutableProperties() {
        return properties;
    }
    
    public List<PropertyAccessor> getMutableKeyProperties() {
        return keyProperties;
    }
    
    public MutableClassAccessor configureAnnotation(Annotation annotation) {
        annotations.put(annotation.annotationType(), annotation);
        return this;
    }

}
