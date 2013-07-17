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

package com.manydesigns.portofino.util;

import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.TextFormat;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PkHelper {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    public final static Logger logger = LoggerFactory.getLogger(PkHelper.class);

    protected final ClassAccessor classAccessor;


    //**************************************************************************
    // Constructor
    //**************************************************************************

    public PkHelper(ClassAccessor classAccessor) {
        this.classAccessor = classAccessor;
    }


    //**************************************************************************
    // Methods
    //**************************************************************************

    public TextFormat createPkGenerator() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (PropertyAccessor property : classAccessor.getKeyProperties()) {
            if (first) {
                first = false;
            } else {
                sb.append("/");
            }
            sb.append("%{");
            sb.append(property.getName());
            sb.append("}");
        }
        return OgnlTextFormat.create(sb.toString());
    }

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

    public String[] generatePkStringArray(Object object) {
        PropertyAccessor[] keyProperties = classAccessor.getKeyProperties();
        String[] array = new String[keyProperties.length];
        for(int i = 0; i < keyProperties.length; i++) {
            PropertyAccessor property = keyProperties[i];
            Object value = property.get(object);
            String stringValue =
                    (String) OgnlUtils.convertValue(value, String.class);
            array[i] = stringValue;
        }
        return array;
    }

    public String getPkStringForUrl(Object o, String encoding) throws UnsupportedEncodingException {
        return getPkStringForUrl(generatePkStringArray(o), encoding);
    }

    public String getPkStringForUrl(String[] pk, String encoding) throws UnsupportedEncodingException {
        String[] escapedPk = new String[pk.length];
        for(int i = 0; i < pk.length; i++) {
            escapedPk[i] = URLEncoder.encode(pk[i], encoding);
        }
        return StringUtils.join(escapedPk, "/");
    }

}
