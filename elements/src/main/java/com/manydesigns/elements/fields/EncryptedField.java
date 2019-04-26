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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.FieldEncrypter;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class EncryptedField extends TextField {
    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    private FieldEncrypter encrypter;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public EncryptedField(PropertyAccessor accessor, Mode mode, String prefix , String classPath) {
        super(accessor, mode, prefix);
        try {
            Class<?> clazz = Class.forName(classPath);
            encrypter = (FieldEncrypter)clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(),e);
        }
    }

    @Override
    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        if (obj == null) {
            stringValue = null;
        } else {
            stringValue = encrypter.decrypt( (String)accessor.get(obj) );
        }
    }

    @Override
    public void writeToObject(Object obj) {
        writeToObject(obj, encrypter.encrypt(stringValue));
    }
}
