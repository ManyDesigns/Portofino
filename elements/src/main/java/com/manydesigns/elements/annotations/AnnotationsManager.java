/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.elements.annotations;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.InstanceBuilder;
import com.manydesigns.elements.util.ReflectionUtil;

import java.util.*;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class AnnotationsManager {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Static fields
    //**************************************************************************

    protected static final Properties elementsProperties;
    protected static final AnnotationsManager manager;

    public static final Logger logger =
            LogUtil.getLogger(AnnotationsManager.class);

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Map<Class, Class> annotationClassMap;


    //**************************************************************************
    // Static initialization and methods
    //**************************************************************************

    static {
        elementsProperties = ElementsProperties.getProperties();
        String managerClassName =
                elementsProperties.getProperty(
                        ElementsProperties.ANNOTATIONS_MANAGER_PROPERTY);
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
        String listString = elementsProperties.getProperty(
                ElementsProperties.ANNOTATIONS_IMPLEMENTATION_LIST_PROPERTY);
        if (listString == null) {
            logger.finer("Empty list");
            return;
        }

        String[] mappingsArray = listString.split(",");
        for (String mapping : mappingsArray) {
            addAnnotationMapping(mapping);
        }
    }

    public void addAnnotationMapping(String mapping) {
        String[] split = mapping.split(":");
        if (split.length != 2) {
            LogUtil.warningMF(logger,
                        "Incorrect annotation mapping syntax: {0}", mapping);
            return;
        }
        String annotationName = split[0].trim();
        String annotationImplName = split[1].trim();

        LogUtil.finerMF(logger,
                "Mapping annotation {0} to implemetation {1}",
                annotationName, annotationImplName);
        Class annotationClass = ReflectionUtil.loadClass(annotationName);
        if (annotationClass == null) {
            LogUtil.warningMF(logger,
                    "Failed to load annotation class: {0}", annotationName);
            return;
        }
        if (!annotationClass.isAnnotation()) {
            LogUtil.warningMF(logger,
                    "Not an annotation: {0}", annotationName);
            return;
        }
        Class annotationImplClass =
                ReflectionUtil.loadClass(annotationImplName);
        if (annotationImplClass == null) {
            LogUtil.warningMF(logger,
                    "Failed to load annotation implementation class: {0}",
                    annotationImplName);
            return;
        }
        if (!Arrays.asList(annotationImplClass.getInterfaces()).contains(annotationClass)) {
            LogUtil.warningMF(logger,
                    "Class {0} not an implementation of {1}",
                    annotationImplName, annotationName);
            return;
        }

        annotationClassMap.put(annotationClass, annotationImplClass);
        LogUtil.finerMF(logger,
                "Mapped annotation {0} to implementation {1}",
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
