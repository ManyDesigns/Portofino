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
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.model.usecases.UseCaseProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UseCasePropertyAccessor
        extends AbstractAnnotatedAccessor
        implements PropertyAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final UseCaseProperty useCaseProperty;
    protected final PropertyAccessor nestedAccessor;

    public static final Logger logger =
            LogUtil.getLogger(UseCasePropertyAccessor.class);

    
    //**************************************************************************
    // Constructors
    //**************************************************************************

    public UseCasePropertyAccessor(UseCaseProperty useCaseProperty,
                                   PropertyAccessor nestedAccessor) {
        super(useCaseProperty.getAnnotations());
        this.useCaseProperty = useCaseProperty;
        this.nestedAccessor = nestedAccessor;
    }


    //**************************************************************************
    // PropertyAccessor implementation
    //**************************************************************************

    public String getName() {
        return nestedAccessor.getName();
    }

    public Class getType() {
        return nestedAccessor.getType();
    }

    public int getModifiers() {
        return nestedAccessor.getModifiers();
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T annotation = super.getAnnotation(annotationClass);
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
