/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.reflection;

import com.manydesigns.portofino.model.Annotation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class AbstractAnnotatedAccessor extends com.manydesigns.elements.reflection.AbstractAnnotatedAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractAnnotatedAccessor.class);


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public AbstractAnnotatedAccessor(@Nullable Collection<Annotation> annotations) {
        super();

        if (annotations == null) {
            return;
        }

        for (Annotation annotation : annotations) {
            Class annotationClass = annotation.getJavaAnnotationClass();
            java.lang.annotation.Annotation javaAnnotation = annotation.getJavaAnnotation();
            this.annotations.put(annotationClass, javaAnnotation);
        }
    }

}
