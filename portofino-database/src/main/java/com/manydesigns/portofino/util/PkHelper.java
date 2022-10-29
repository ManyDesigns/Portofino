/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.util;

import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.TextFormat;
import com.manydesigns.portofino.persistence.IdStrategy;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PkHelper extends IdStrategy {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public final static Logger logger = LoggerFactory.getLogger(PkHelper.class);

    public PkHelper(ClassAccessor classAccessor) {
        super(classAccessor);
    }

    //**************************************************************************
    // Methods
    //**************************************************************************

    public Serializable getPrimaryKey(String... params) {
        int i = 0;
        Serializable result = (Serializable)classAccessor.newInstance();
        if(params.length != classAccessor.getKeyProperties().length) {
            throw new RuntimeException("Wrong number of parameters for primary key: expected " + classAccessor.getKeyProperties().length + ", got " + params.length);
        }
        for(PropertyAccessor property : classAccessor.getKeyProperties()) {
            String stringValue = params[i];
            Object value = OgnlUtils.convertValue(stringValue, property.getType());
            property.set(result, value);
            i++;
        }

        return result;
    }

}
