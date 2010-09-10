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

package com.manydesigns.portofino.reflection;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.annotations.AnnotationsManager;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.usecases.UseCaseProperty;
import ognl.OgnlContext;
import ognl.TypeConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UseCasePropertyAccessor implements PropertyAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected final UseCaseProperty useCaseProperty;
    protected final PropertyAccessor nestedAccessor;

    protected final Map<Class, Annotation> annotations;

    public static final Logger logger =
            LogUtil.getLogger(UseCasePropertyAccessor.class);

    public UseCasePropertyAccessor(UseCaseProperty useCaseProperty,
                                   PropertyAccessor nestedAccessor) {
        this.useCaseProperty = useCaseProperty;
        this.nestedAccessor = nestedAccessor;

        annotations = new HashMap<Class, Annotation>();
        AnnotationsManager manager = AnnotationsManager.getManager();

        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        TypeConverter typeConverter = ognlContext.getTypeConverter();

        for (com.manydesigns.portofino.model.annotations.Annotation
                propertyAnnotation : useCaseProperty.getAnnotations()) {
            instanciateOneAnnotation(manager, ognlContext,
                    typeConverter, propertyAnnotation);
        }
    }

    private void instanciateOneAnnotation(AnnotationsManager manager,
                                          OgnlContext ognlContext,
                                          TypeConverter typeConverter,
                                          com.manydesigns.portofino.model.annotations.Annotation propertyAnnotation) {
        String type = propertyAnnotation.getType();

        Class annotationClass = ReflectionUtil.loadClass(type);
        if (annotationClass == null) {
            LogUtil.warningMF(logger,
                    "Cannot load annotation class: {0}", type);
            return;
        }

        Class annotationImplClass =
                manager.getAnnotationImplementationClass(
                        annotationClass);
        if (annotationImplClass == null) {
            LogUtil.warningMF(logger,
                    "Cannot find implementation for annotation class: {0}",
                    type);
            return;
        }

        Constructor annotationConstructor = null;
        Constructor[] constructors =
                annotationImplClass.getConstructors();
        for (Constructor candidateConstructor : constructors) {
            Class[] parameterTypes =
                    candidateConstructor.getParameterTypes();
            if (parameterTypes.length != 1) {
                continue;
            }
            annotationConstructor = candidateConstructor;
        }
        if (annotationConstructor == null) {
            LogUtil.warningMF(logger,
                    "Cannot find constructor for annotation class: {0}",
                    type);
            return;
        }

        Class parameterType = annotationConstructor.getParameterTypes()[0];
        Object value = typeConverter.convertValue(
                ognlContext, null, null, null,
                propertyAnnotation.getValue(), parameterType);

        Annotation annotation =
                (Annotation) ReflectionUtil.newInstance(
                        annotationConstructor, value);
        if (annotation == null) {
            LogUtil.warningMF(logger,
                    "Cannot instanciate annotation: {0}", type);
            return;
        }

        annotations.put(annotationClass, annotation);
    }

    public String getName() {
        return nestedAccessor.getName();
    }

    public Class getType() {
        return nestedAccessor.getType();
    }

    public int getModifiers() {
        return nestedAccessor.getModifiers();
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T annotation = (T) annotations.get(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        return nestedAccessor.getAnnotation(annotationClass);
    }

    public Object get(Object obj)
            throws IllegalAccessException, InvocationTargetException {
        return nestedAccessor.get(obj);
    }

    public void set(Object obj, Object value)
            throws IllegalAccessException, InvocationTargetException {
        nestedAccessor.set(obj, value);
    }
}
