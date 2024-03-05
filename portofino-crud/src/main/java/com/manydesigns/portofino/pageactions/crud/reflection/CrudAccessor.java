/*
 * Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.crud.reflection;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.annotations.impl.EnabledImpl;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.MutablePropertyAccessor;
import com.manydesigns.elements.reflection.OGNLPropertyAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudConfiguration;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.pageactions.crud.configuration.VirtualCrudProperty;
import com.manydesigns.portofino.reflection.AbstractAnnotatedAccessor;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 **/
public class CrudAccessor extends AbstractAnnotatedAccessor implements ClassAccessor {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";


    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final CrudConfiguration crudConfiguration;
    protected final ClassAccessor nestedAccessor;
    protected final CrudPropertyAccessor[] propertyAccessors;
    protected final CrudPropertyAccessor[] keyPropertyAccessors;

    public final static Logger logger =
            LoggerFactory.getLogger(CrudAccessor.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public CrudAccessor(@NotNull final CrudConfiguration crudConfiguration, @NotNull ClassAccessor nestedAccessor) {
        super(null);
        this.crudConfiguration = crudConfiguration;
        this.nestedAccessor = nestedAccessor;
        PropertyAccessor[] columnAccessors = nestedAccessor.getProperties();
        PropertyAccessor[] keyColumnAccessors = nestedAccessor.getKeyProperties();
        List<VirtualCrudProperty> virtualCrudProperties = new ArrayList<>();
        for(CrudProperty crudProperty : crudConfiguration.getProperties()) {
            if(crudProperty instanceof VirtualCrudProperty) {
                virtualCrudProperties.add((VirtualCrudProperty) crudProperty);
            }
        }

        propertyAccessors =
                new CrudPropertyAccessor[columnAccessors.length + virtualCrudProperties.size()];
        keyPropertyAccessors =
                new CrudPropertyAccessor[keyColumnAccessors.length];

        int i = 0;
        for (PropertyAccessor columnAccessor : columnAccessors) {
            CrudProperty crudProperty =
                    findCrudPropertyByName(
                            crudConfiguration, columnAccessor.getName());
            boolean inKey = ArrayUtils.contains(keyColumnAccessors, columnAccessor);
            CrudPropertyAccessor propertyAccessor =
                        new CrudPropertyAccessor(crudProperty, columnAccessor, inKey);
            propertyAccessors[i] = propertyAccessor;
            i++;
        }
        for(VirtualCrudProperty virtualCrudProperty : virtualCrudProperties) {
            PropertyAccessor nestedPropertyAccessor;
            try {
                if("ognl".equalsIgnoreCase(virtualCrudProperty.getLanguage())) {
                    nestedPropertyAccessor = new OGNLPropertyAccessor(
                            virtualCrudProperty.getName(),
                            virtualCrudProperty.getType(),
                            virtualCrudProperty.getExpression(),
                            ElementsThreadLocals.getOgnlContext());
                } else {
                    logger.warn("Unsupported language: " + virtualCrudProperty.getLanguage());
                    nestedPropertyAccessor = new MutablePropertyAccessor(
                            virtualCrudProperty.getName(), virtualCrudProperty.getType()).
                            configureAnnotation(new EnabledImpl(false));
                }
            } catch (Exception e) {
                logger.error("Could not create OGNL accessor", e);
                nestedPropertyAccessor = new MutablePropertyAccessor(
                        virtualCrudProperty.getName(), virtualCrudProperty.getType()).
                        configureAnnotation(new EnabledImpl(false));
            }
            CrudPropertyAccessor propertyAccessor =
                        new CrudPropertyAccessor(virtualCrudProperty, nestedPropertyAccessor, false);
            propertyAccessors[i] = propertyAccessor;
            i++;
        }

        i = 0;
        for (PropertyAccessor keyColumnAccessor : keyColumnAccessors) {
            String propertyName = keyColumnAccessor.getName();
            try {
                CrudPropertyAccessor keyPropertyAccessor =
                        getProperty(keyColumnAccessor.getName());
                keyPropertyAccessors[i] = keyPropertyAccessor;
            } catch (NoSuchFieldException e) {
                logger.error("Could not find key property: " + propertyName, e);
            }
            i++;
        }

        if(crudConfiguration.isUseLocalOrder()) {
            logger.debug("Sorting crud properties to preserve their previous order as much as possible");
            Arrays.sort(propertyAccessors, new Comparator<CrudPropertyAccessor>() {
                private int oldIndex(CrudPropertyAccessor c) {
                    int i = 0;
                    for (CrudProperty old : crudConfiguration.getProperties()) {
                        if (old.equals(c.getCrudProperty())) {
                            return i;
                        }
                        i++;
                    }
                    return -1;
                }

                public int compare(CrudPropertyAccessor c1, CrudPropertyAccessor c2) {
                    Integer index1 = oldIndex(c1);
                    Integer index2 = oldIndex(c2);
                    if (index1 != -1) {
                        if (index2 != -1) {
                            return index1.compareTo(index2);
                        } else {
                            return -1;
                        }
                    } else {
                        return index2 == -1 ? 0 : 1;
                    }
                }
            });
        }
    }

    public static CrudProperty findCrudPropertyByName(CrudConfiguration crudConfiguration, String propertyName) {
        for (CrudProperty current : crudConfiguration.getProperties()) {
            if (current.getName().equalsIgnoreCase(propertyName)) {
                return current;
            }
        }
        return null;
    }


    //**************************************************************************
    // ClassAccessor implementation
    //**************************************************************************

    public String getName() {
        return crudConfiguration.getName();
    }

    @Override
    public Class<?> getType() {
        return nestedAccessor.getType();
    }

    public CrudPropertyAccessor getProperty(String propertyName) throws NoSuchFieldException {
        for (CrudPropertyAccessor current : propertyAccessors) {
            //XXX Alessio verificare
            if (current.getName().equalsIgnoreCase(propertyName)) {
                return current;
            }
        }

        throw new NoSuchFieldException(propertyName + " (of use case " + getName() + ")");
    }

    public PropertyAccessor[] getProperties() {
        return propertyAccessors.clone();
    }

    public PropertyAccessor[] getKeyProperties() {
        return keyPropertyAccessors.clone();
    }

    public Object newInstance() {
        return nestedAccessor.newInstance();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T annotation = super.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        return nestedAccessor.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        List<Annotation> allAnnotations = new ArrayList<Annotation>(annotations.values());
        for(Annotation nestedAnnotation : nestedAccessor.getAnnotations()) {
            if(!annotations.containsKey(nestedAnnotation.annotationType())) {
                allAnnotations.add(nestedAnnotation);
            }
        }
        return allAnnotations.toArray(new Annotation[allAnnotations.size()]);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public CrudConfiguration getCrudConfiguration() {
        return crudConfiguration;
    }

}
