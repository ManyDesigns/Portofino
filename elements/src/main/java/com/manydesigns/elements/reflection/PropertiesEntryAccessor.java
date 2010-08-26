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

package com.manydesigns.elements.reflection;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.impl.LabelImpl;

import java.lang.annotation.Annotation;
import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class PropertiesEntryAccessor implements PropertyAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected final String name;
    protected final Label labelAnnotation;

    public PropertiesEntryAccessor(String name) {
        this.name = name;
        labelAnnotation = new LabelImpl(name);
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return String.class;
    }

    public int getModifiers() {
        return 0;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (annotationClass == Label.class) {
            //noinspection unchecked
            return (T) labelAnnotation;
        }
        return null;
    }

    public String get(Object obj) {
        return ((Properties)obj).getProperty(name);
    }

    public void set(Object obj, Object value) {
        ((Properties)obj).setProperty(name, (String)value);
    }
}
