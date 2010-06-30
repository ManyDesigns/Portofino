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

package com.manydesigns.portofino.base.reflection;

import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.base.model.Column;

import java.lang.reflect.Modifier;
import java.lang.annotation.Annotation;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ColumnAccessor implements PropertyAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected final Column column;

    public ColumnAccessor(Column column) {
        this.column = column;
    }

    public String getName() {
        return column.getColumnName();
    }

    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> clazz) {
        return false;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

    public Object get(Object obj) throws IllegalAccessException {
        return ((Map)obj).get(column.getColumnName());
    }

    public void set(Object obj, Object value) throws IllegalAccessException {
        ((Map)obj).put(column.getColumnName(), value);
    }

    public boolean isAssignableTo(Class clazz) {
        return false;
    }
}
