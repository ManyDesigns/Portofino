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

package com.manydesigns.elements.annotations;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.util.InstanceBuilder;
import com.manydesigns.elements.util.ReflectionUtil;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class AnnotationsManager {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Static fields
    //**************************************************************************

    protected static final Configuration elementsConfiguration;
    protected static final AnnotationsManager manager;

    public static final Logger logger =
            LoggerFactory.getLogger(AnnotationsManager.class);

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Map<Class, Class> annotationClassMap;


    //**************************************************************************
    // Static initialization and methods
    //**************************************************************************

    static {
        elementsConfiguration = ElementsProperties.getConfiguration();
        String managerClassName =
                elementsConfiguration.getString(
                        ElementsProperties.ANNOTATIONS_MANAGER);
        InstanceBuilder<AnnotationsManager> builder =
                new InstanceBuilder<AnnotationsManager>(
                        AnnotationsManager.class,
                        AnnotationsManager.class,
                        logger);
        manager = builder.createInstance(managerClassName);
    }

    public static AnnotationsManager getManager() {
        return manager;
    }


    //**************************************************************************
    // Constructors and initialization
    //**************************************************************************

    public AnnotationsManager() {
        annotationClassMap = new HashMap<Class, Class>();
        Properties mappings = elementsConfiguration.getProperties(
                ElementsProperties.ANNOTATIONS_IMPLEMENTATION_LIST);
        if (mappings == null) {
            logger.debug("Empty list");
            return;
        }

        for (Map.Entry<Object, Object> mapping : mappings.entrySet()) {
            String annotationName = (String) mapping.getKey();
            String annotationImplName = (String) mapping.getValue();

            addAnnotationMapping(annotationName, annotationImplName);
        }
    }

    public void addAnnotationMapping(String annotationName, String annotationImplName) {
        logger.debug("Mapping annotation {} to implemetation {}",
                annotationName, annotationImplName);
        Class annotationClass = ReflectionUtil.loadClass(annotationName);
        if (annotationClass == null) {
            logger.warn("Failed to load annotation class: {}", annotationName);
            return;
        }
        if (!annotationClass.isAnnotation()) {
            logger.warn("Not an annotation: {}", annotationName);
            return;
        }
        Class annotationImplClass =
                ReflectionUtil.loadClass(annotationImplName);
        if (annotationImplClass == null) {
            logger.warn("Failed to load annotation implementation class: {}",
                    annotationImplName);
            return;
        }
        if (!Arrays.asList(annotationImplClass.getInterfaces()).contains(annotationClass)) {
            logger.warn("Class {} not an implementation of {}",
                    annotationImplName, annotationName);
            return;
        }

        annotationClassMap.put(annotationClass, annotationImplClass);
        logger.debug("Mapped annotation {} to implementation {}",
                annotationName, annotationImplName);

    }

    //**************************************************************************
    // Other methods
    //**************************************************************************

    public Set<Class> getManagedAnnotationClasses() {
        return annotationClassMap.keySet();
    }

    public Class getAnnotationImplementationClass(Class annotationClass) {
        return annotationClassMap.get(annotationClass);
    }

}
