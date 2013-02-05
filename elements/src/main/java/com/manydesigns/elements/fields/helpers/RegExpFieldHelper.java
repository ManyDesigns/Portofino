/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.fields.helpers;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.RegExp;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.RegExpTextField;
import com.manydesigns.elements.fields.search.SearchField;
import com.manydesigns.elements.fields.search.TextMatchMode;
import com.manydesigns.elements.fields.search.TextSearchField;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class RegExpFieldHelper implements FieldHelper {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public Field tryToInstantiateField(ClassAccessor classAccessor,
                                       PropertyAccessor propertyAccessor,
                                       Mode mode,
                                       String prefix) {
        RegExp regExp = propertyAccessor.getAnnotation(RegExp.class);
        if (regExp != null &&
            String.class.isAssignableFrom(propertyAccessor.getType())) {
            RegExpTextField field = new RegExpTextField(propertyAccessor, mode, prefix, regExp.value());
            field.setErrorString(field.getText(regExp.errorMessage(), regExp.value()));
            return field;
        }
        return null;
    }

    public SearchField tryToInstantiateSearchField(ClassAccessor classAccessor,
                                                   PropertyAccessor propertyAccessor,
                                                   String prefix) {
        if (String.class.isAssignableFrom(propertyAccessor.getType())
                && propertyAccessor.isAnnotationPresent(RegExp.class)) {
            TextSearchField textSearchField =
                    new TextSearchField(propertyAccessor, prefix);
            textSearchField.setShowMatchMode(false);
            textSearchField.setMatchMode(TextMatchMode.EQUALS);
            return textSearchField;
        }
        return null;
    }
}
