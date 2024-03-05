/*
 * Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.elements.fields.DatabaseBlobField;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.search.SearchField;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DatabaseBlobFieldHelper implements FieldHelper {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBlobFieldHelper.class);

    public Field tryToInstantiateField(ClassAccessor classAccessor,
                                       PropertyAccessor propertyAccessor,
                                       Mode mode,
                                       String prefix) {
        if (byte[].class.isAssignableFrom(propertyAccessor.getType())) {
            try {
                return new DatabaseBlobField(classAccessor, propertyAccessor, mode, prefix);
            } catch (NoSuchFieldException e) {
                logger.error("Could not instantiate DatabaseBlobField", e);
                return null;
            }
        }
        return null;
    }

    public SearchField tryToInstantiateSearchField(ClassAccessor classAccessor,
                                                   PropertyAccessor propertyAccessor,
                                                   String prefix) {
        return null;
    }

}
