/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.annotations.AnnotationsManager;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.elements.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public class Annotation implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Object parent;
    protected String type;
    protected List<String> values;

    //**************************************************************************
    // Fields for wire up
    //**************************************************************************

    protected Class javaAnnotationClass;
    protected java.lang.annotation.Annotation javaAnnotation;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(Annotation.class);

    //**************************************************************************
    // Contruction
    //**************************************************************************

    public Annotation() {
        values = new ArrayList<String>();
    }

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

    public void init(Model model) {
        javaAnnotationClass = ReflectionUtil.loadClass(type);
        if (javaAnnotationClass == null) {
            logger.warn("Cannot load annotation class: {}", type);
            return;
        }

        AnnotationsManager annotationsManager =
                AnnotationsManager.getManager();

        Class annotationImplClass =
                annotationsManager.getAnnotationImplementationClass(
                        javaAnnotationClass);
        if (annotationImplClass == null) {
            logger.warn("Cannot find implementation for annotation class: {}",
                    javaAnnotationClass);
            return;
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

                javaAnnotation = (java.lang.annotation.Annotation)
                        ReflectionUtil.newInstance(
                                candidateConstructor, castValues);
            } catch (Throwable e) {
                logger.debug("Failed to use constructor: " +
                        candidateConstructor, e);
            }
        }

        if (javaAnnotation == null) {
            logger.warn("Cannot instanciate annotation: {}", javaAnnotationClass);
        }
    }

    public void link(Model model) {}

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

    @XmlElement(name = "value", type = java.lang.String.class)
    public List<String> getValues() {
        return values;
    }

    public Class getJavaAnnotationClass() {
        return javaAnnotationClass;
    }

    public java.lang.annotation.Annotation getJavaAnnotation() {
        return javaAnnotation;
    }
}
