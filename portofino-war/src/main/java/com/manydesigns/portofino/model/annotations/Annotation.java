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

package com.manydesigns.portofino.model.annotations;

import com.manydesigns.elements.annotations.AnnotationsManager;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.xml.XmlAttribute;
import com.manydesigns.portofino.xml.XmlCollection;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
@XmlCollection(itemClasses = String.class, itemNames = "value")
public class Annotation extends ArrayList<String> implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String type;

    //**************************************************************************
    // Fields for wire up
    //**************************************************************************

    protected Class javaAnnotationClass;
    protected java.lang.annotation.Annotation javaAnnotation;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LogUtil.getLogger(Annotation.class);

    //**************************************************************************
    // Contruction
    //**************************************************************************

    public Annotation() {}

    public Annotation(String type) {
        this();
        this.type = type;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void reset() {
        javaAnnotation = null;
        javaAnnotationClass = null;
    }

    public void init(Model model) {
        javaAnnotationClass = ReflectionUtil.loadClass(type);
        if (javaAnnotationClass == null) {
            LogUtil.warningMF(logger,
                    "Cannot load annotation class: {0}", type);
            return;
        }

        AnnotationsManager annotationsManager =
                AnnotationsManager.getManager();

        Class annotationImplClass =
                annotationsManager.getAnnotationImplementationClass(
                        javaAnnotationClass);
        if (annotationImplClass == null) {
            LogUtil.warningMF(logger,
                    "Cannot find implementation for annotation class: {0}",
                    javaAnnotationClass);
            return;
        }

        Constructor[] constructors =
                annotationImplClass.getConstructors();
        for (Constructor candidateConstructor : constructors) {
            Class[] parameterTypes =
                    candidateConstructor.getParameterTypes();
            if (parameterTypes.length != this.size()) {
                continue;
            }

            try {
                Object castValues[] = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class parameterType = parameterTypes[i];
                    String stringValue = this.get(i);
                    Object value;
                    if (parameterType.isArray()) {
                        value = Util.matchStringArray(stringValue);
                    } else {
                        value = stringValue;
                    }
                    castValues[i] = Util.convertValue(value, parameterType);
                }

                javaAnnotation = (java.lang.annotation.Annotation)
                        ReflectionUtil.newInstance(
                                candidateConstructor, castValues);
            } catch (Throwable e) {
                LogUtil.finerMF(logger, "Failed to use constructor: {0}", e,
                        candidateConstructor);
            }
        }

        if (javaAnnotation == null) {
            LogUtil.warningMF(logger,
                    "Cannot instanciate annotation: {0}", javaAnnotationClass);
        }
    }

    public String getQualifiedName() {
        return null;
    }


    //**************************************************************************
    // Getters and setters
    //**************************************************************************

    @XmlAttribute(required = true, order = 1)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Class getJavaAnnotationClass() {
        return javaAnnotationClass;
    }

    public java.lang.annotation.Annotation getJavaAnnotation() {
        return javaAnnotation;
    }
}
