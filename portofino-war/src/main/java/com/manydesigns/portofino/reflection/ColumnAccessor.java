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

import com.manydesigns.elements.annotations.Immutable;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.annotations.impl.ImmutableImpl;
import com.manydesigns.elements.annotations.impl.RequiredImpl;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.model.Column;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ColumnAccessor implements PropertyAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Column column;
    protected final Required requiredAnnotation;
    protected final Immutable immutableAnnotation;


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public ColumnAccessor(Column column, boolean inPk) {
        this.column = column;

        if (inPk) {
            immutableAnnotation = new ImmutableImpl();
        } else {
            immutableAnnotation = null;
        }

        if (column.isNullable()) {
            requiredAnnotation = null;
        } else {
            requiredAnnotation = new RequiredImpl();
        }
    }


    //**************************************************************************
    // PropertyAccessor implementation
    //**************************************************************************

    public String getName() {
        return column.getColumnName();
    }

    public Class getType() {
        return column.getJavaType();
    }

    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (annotationClass == Required.class) {
            return (T) requiredAnnotation;
        } else if (annotationClass == Immutable.class) {
            return (T) immutableAnnotation;
        }
        return null;
    }

    public Object get(Object obj) throws IllegalAccessException {
        return ((Map)obj).get(column.getColumnName());
    }

    @SuppressWarnings({"unchecked"})
    public void set(Object obj, Object value) throws IllegalAccessException {
        ((Map)obj).put(column.getColumnName(), value);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public Column getColumn() {
        return column;
    }

    //**************************************************************************
    // toString()
    //**************************************************************************

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("column", column.getQualifiedName()).toString();
    }
}
