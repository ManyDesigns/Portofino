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

package com.manydesigns.elements.fields.helpers;

import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.NumericField;
import com.manydesigns.elements.fields.search.SearchField;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;

import java.math.BigDecimal;
import java.math.BigInteger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class NumericFieldHelper implements FieldHelper {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public Field tryToInstantiateField(ClassAccessor classAccessor,
                                  PropertyAccessor propertyAccessor,
                                  String prefix) {
        Class type = propertyAccessor.getType();
        if (type == BigDecimal.class
                || type == BigInteger.class
                || type == Byte.class
                || type == Byte.TYPE
                || type == Short.class
                || type == Short.TYPE
                || type == Integer.class
                || type == Integer.TYPE
                || type == Long.class
                || type == Long.TYPE
                ) {
            return new NumericField(propertyAccessor, prefix);
        }

        return null;
    }

    public SearchField tryToInstantiateSearchField(ClassAccessor classAccessor, PropertyAccessor propertyAccessor, String prefix) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
