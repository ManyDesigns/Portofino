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

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.portofino.model.annotations.Annotation;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class AbstractAnnotatedAccessor
        implements AnnotatedElement {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Map<Class, java.lang.annotation.Annotation> annotations;

    public static final Logger logger =
            LogUtil.getLogger(AbstractAnnotatedAccessor.class);


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public AbstractAnnotatedAccessor(
            Collection<Annotation>
                    annotations) {
        this.annotations = new HashMap<Class, java.lang.annotation.Annotation>();

        for (Annotation annotation : annotations) {
            Class annotationClass = annotation.getJavaAnnotationClass();
            java.lang.annotation.Annotation javaAnnotation = annotation.getJavaAnnotation();
            this.annotations.put(annotationClass, javaAnnotation);
        }
    }

    //**************************************************************************
    // AnnotatedElement implementation
    //**************************************************************************

    public boolean isAnnotationPresent(Class<? extends java.lang.annotation.Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotationClass) {
        return (T) annotations.get(annotationClass);
    }

    public java.lang.annotation.Annotation[] getAnnotations() {
        Collection<java.lang.annotation.Annotation> annotationCollection = annotations.values();
        java.lang.annotation.Annotation[] result = new java.lang.annotation.Annotation[annotationCollection.size()];
        annotationCollection.toArray(result);
        return result;
    }

    public java.lang.annotation.Annotation[] getDeclaredAnnotations() {
        return getAnnotations();
    }
}
