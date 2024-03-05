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

package com.manydesigns.elements.fields.search;

import com.manydesigns.elements.FieldEncrypter;
import com.manydesigns.elements.reflection.PropertyAccessor;
import org.apache.commons.lang.StringUtils;
import javax.servlet.http.HttpServletRequest;

/*
 * @author Emanuele Poggi     - emanuele.poggi@manydesigns.com
 * @author Marco Stanizzi       - marco.stanizzi@manydesigns.com
 */
public class EncryptedTextSearchField extends TextSearchField {
    public static final String copyright = "Copyright (C) 2005-2019 ManyDesigns srl";

    private FieldEncrypter encrypter;

    //**************************************************************************
    // Costruttori
    //**************************************************************************

    public EncryptedTextSearchField(PropertyAccessor accessor) {
        this(accessor, null,null);
    }

    public EncryptedTextSearchField(PropertyAccessor accessor, String prefix, String classPath) {
        super(accessor, prefix);

        try {
            Class<?> clazz = Class.forName(classPath);
            encrypter = (FieldEncrypter)clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(),e);
        }
    }

    //**************************************************************************
    // Element implementation
    //**************************************************************************

    @Override
    public void readFromRequest(HttpServletRequest req) {
        value = StringUtils.trimToNull(req.getParameter(inputName));
    }

    public void configureCriteria(Criteria criteria) {
        if (value != null) {
            criteria.ilike(accessor, encrypter.encrypt(value), matchMode);
        }
    }
}
