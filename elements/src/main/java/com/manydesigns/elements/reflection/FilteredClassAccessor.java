/*
 * Copyright (C) 2005-2019 ManyDesigns srl.  All rights reserved.
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

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A ClassAccessor that restricts another accessor to certain properties. It can be created either with a whitelist of
 * properties to include or a blacklist of properties to exclude. 
 * 
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class FilteredClassAccessor extends AbstractAnnotatedAccessor implements ClassAccessor {

    protected final ClassAccessor delegate;
    protected final PropertyAccessor[] properties;
    protected final PropertyAccessor[] keyProperties;

    protected FilteredClassAccessor(ClassAccessor delegate, boolean whitelist, String... properties) {
        this.delegate = delegate;
        List<PropertyAccessor> propertiesList = new ArrayList<PropertyAccessor>();
        List<PropertyAccessor> keyPropertiesList = new ArrayList<PropertyAccessor>();
        for(PropertyAccessor p : delegate.getProperties()) {
            if(whitelist == ArrayUtils.contains(properties, p.getName())) {
                propertiesList.add(p);
                if(ArrayUtils.contains(delegate.getKeyProperties(), p)) {
                    keyPropertiesList.add(p);
                }
            }
        }
        if(whitelist && propertiesList.size() != properties.length) {
            for(String property : properties) {
                try {
                    delegate.getProperty(property); //Cause exception to be thrown
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        this.properties = propertiesList.toArray(new PropertyAccessor[propertiesList.size()]);
        this.keyProperties = keyPropertiesList.toArray(new PropertyAccessor[keyPropertiesList.size()]);
    }
    
    public String getName() {
        return delegate.getName();
    }

    public Class<?> getType() {
        return delegate.getType();
    }

    public PropertyAccessor getProperty(String propertyName) throws NoSuchFieldException {
        for (PropertyAccessor current : properties) {
            if (current.getName().equals(propertyName)) {
                return current;
            }
        }
        throw new NoSuchFieldException(propertyName);
    }

    public PropertyAccessor[] getProperties() {
        return properties;
    }

    public PropertyAccessor[] getKeyProperties() {
        return keyProperties;
    }

    public Object newInstance() {
        return delegate.newInstance();
    }
    
    public static FilteredClassAccessor exclude(ClassAccessor classAccessor, String... properties) {
        return new FilteredClassAccessor(classAccessor, false, properties);
    }
    
    public static FilteredClassAccessor include(ClassAccessor classAccessor, String... properties) {
        return new FilteredClassAccessor(classAccessor, true, properties);
    }
}
