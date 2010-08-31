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

import com.manydesigns.elements.annotations.InSummary;
import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Searchable;
import com.manydesigns.elements.annotations.impl.InSummaryImpl;
import com.manydesigns.elements.annotations.impl.LabelImpl;
import com.manydesigns.elements.annotations.impl.SearchableImpl;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.model.usecases.UseCaseProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

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

    protected final Label labelAnnotation;
    protected final Searchable searchableAnnotation;
    protected final InSummary inSummaryAnnotation;

    public UseCasePropertyAccessor(UseCaseProperty useCaseProperty,
                                   PropertyAccessor nestedAccessor) {
        this.useCaseProperty = useCaseProperty;
        this.nestedAccessor = nestedAccessor;

        if (useCaseProperty.getLabel() == null) {
            labelAnnotation = nestedAccessor.getAnnotation(Label.class);
        } else {
            labelAnnotation = new LabelImpl(useCaseProperty.getLabel());
        }

        searchableAnnotation =
                new SearchableImpl(useCaseProperty.isSearchable());

        inSummaryAnnotation =
                new InSummaryImpl(useCaseProperty.isInSummary());
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
        if (annotationClass == Label.class) {
            return (T)labelAnnotation;
        } else if (annotationClass == Searchable.class) {
            return (T)searchableAnnotation;
        } else if (annotationClass == InSummary.class) {
            return (T)inSummaryAnnotation;
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
