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

import java.util.List;
import java.util.Optional;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface Annotated {
    String copyright = "Copyright (C) 2005-2019 ManyDesigns srl";

    List<Annotation> getAnnotations();

    @SuppressWarnings("unchecked")
    default <T extends java.lang.annotation.Annotation> Optional<T> getAnnotation(Class<T> annotationClass) {
        return (Optional<T>) getAnnotations().stream()
                .filter(a -> annotationClass.isAssignableFrom(a.getJavaAnnotationClass()))
                .map(Annotation::getJavaAnnotation)
                .findFirst();
    }
}
