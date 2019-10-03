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

package com.manydesigns.portofino.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.manydesigns.elements.annotations.AnnotationFactory;
import com.manydesigns.elements.annotations.AnnotationsManager;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.elements.util.Util;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public class Annotation implements ModelObject {
    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Object parent;
    protected String type;
    @Deprecated
    protected List<String> values = new ArrayList<>();
    protected List<Property> properties = new ArrayList<>();

    //**************************************************************************
    // Fields for wire up
    //**************************************************************************

    protected Class<? extends java.lang.annotation.Annotation> javaAnnotationClass;
    protected java.lang.annotation.Annotation javaAnnotation;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(Annotation.class);

    //**************************************************************************
    // Contruction
    //**************************************************************************

    public Annotation() {}

    public Annotation(String type) {
        this();
        this.type = type;
    }

    public Annotation(Object parent, String type) {
        this();
        this.parent = parent;
        this.type = type;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        this.parent = parent;
    }

    public void reset() {
        javaAnnotation = null;
        javaAnnotationClass = null;
    }

    public void init(Model model, Configuration configuration) {
        javaAnnotationClass = ReflectionUtil.loadClass(type);
        if (javaAnnotationClass == null) {
            logger.error("Cannot load annotation class: {}", type);
            return;
        }
        if(!java.lang.annotation.Annotation.class.isAssignableFrom(javaAnnotationClass)) {
            logger.error("Not an annotation: " + javaAnnotationClass);
            javaAnnotationClass = null;
            return;
        }

        if(properties.isEmpty()) {
            java.lang.annotation.Annotation legacyAnnotation = createLegacyAnnotation();
            if(legacyAnnotation != null) {
                javaAnnotation = legacyAnnotation;
                try {
                    logger.info("Migrating annotation " + javaAnnotation + " to new format");
                    List<Property> properties = new ArrayList<>();
                    for (Method method : AnnotationFactory.getAnnotationMethods(javaAnnotationClass)) {
                        Property property = new Property();
                        property.setName(method.getName());
                        Object value = method.invoke(javaAnnotation);
                        property.setValue(OgnlUtils.convertValueToString(value));
                        properties.add(property);
                    }
                    this.properties.addAll(properties);
                    values.clear();
                } catch (Exception e) {
                    logger.error("Migration failed", e);
                }
            }
        }
        if(javaAnnotation == null) {
            Map<String, Object> values = new HashMap<>();
            properties.forEach(p -> {
                String name = p.getName();
                try {
                    values.put(name, OgnlUtils.convertValue(p.getValue(), javaAnnotationClass.getMethod(name).getReturnType()));
                } catch (NoSuchMethodException e) {
                    logger.warn("Ignoring nonexistent annotation property " + name + " for " + javaAnnotationClass);
                }
            });
            try {
                javaAnnotation = new AnnotationFactory().make(javaAnnotationClass, values);
            } catch (Exception e) {
                logger.error("Could not make annotation proxy for " + javaAnnotationClass, e);
            }
        }
    }

    protected java.lang.annotation.Annotation createLegacyAnnotation() {
        if(values.isEmpty()) {
            return null;
        }
        //Legacy
        AnnotationsManager annotationsManager = AnnotationsManager.getManager();

        Class annotationImplClass =
                annotationsManager.getAnnotationImplementationClass(
                        javaAnnotationClass);
        if (annotationImplClass == null) {
            return null;
        }

        Constructor[] constructors =
                annotationImplClass.getConstructors();
        for (Constructor candidateConstructor : constructors) {
            Class[] parameterTypes =
                    candidateConstructor.getParameterTypes();
            if (parameterTypes.length != values.size()) {
                continue;
            }

            try {
                Object castValues[] = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class parameterType = parameterTypes[i];
                    String stringValue = values.get(i);
                    Object value;
                    if (parameterType.isArray()) {
                        value = Util.matchStringArray(stringValue);
                    } else if (parameterType.isEnum()) {
                        Object[] enumValues = parameterType.getEnumConstants();
                        value = stringValue;
                        for (Object current : enumValues) {
                            Enum enumValue = (Enum)current;
                            if (enumValue.name().equals(stringValue)) {
                                value = enumValue;
                                break;
                            }
                        }
                    } else {
                        value = stringValue;
                    }
                    castValues[i] = OgnlUtils.convertValue(value, parameterType);
                }

                return (java.lang.annotation.Annotation)
                        ReflectionUtil.newInstance(
                                candidateConstructor, castValues);
            } catch (Throwable e) {
                logger.debug("Failed to use constructor: " +
                        candidateConstructor, e);
            }
        }
        return null;
    }

    public void link(Model model, Configuration configuration) {}

    public void visitChildren(ModelObjectVisitor visitor) {}

    //**************************************************************************
    // Getters and setters
    //**************************************************************************

    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    @XmlAttribute(required = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("values")
    @XmlElement(name = "value", type = String.class)
    @Deprecated
    public List<String> getValues() {
        return values;
    }

    //Needed for Jackson
    @Deprecated
    public void setValues(List<String> values) {
        this.values.clear();
        this.values.addAll(values);
    }

    @JsonProperty("properties")
    @XmlElement(name = "property", type = Property.class)
    public List<Property> getProperties() {
        return properties;
    }

    //Needed for Jackson
    public void setProperties(List<Property> properties) {
        this.properties.clear();
        this.properties.addAll(properties);
    }

    public Class getJavaAnnotationClass() {
        return javaAnnotationClass;
    }

    public java.lang.annotation.Annotation getJavaAnnotation() {
        return javaAnnotation;
    }

    public Property getProperty(String name) {
        for(Property property : properties) {
            if(name.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }
}
