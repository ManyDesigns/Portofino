/*
 * Copyright (C) 2005-2022 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.persistence;

import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.TextFormat;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Strategy used by CRUD actions to work with object IDs.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class IdStrategy {
    protected final ClassAccessor classAccessor;

    public IdStrategy(ClassAccessor classAccessor) {
        this.classAccessor = classAccessor;
    }

    public abstract Object getPrimaryKey(String... identifier);

    public String[] generatePkStringArray(Object object) {
        PropertyAccessor[] keyProperties = classAccessor.getKeyProperties();
        String[] array = new String[keyProperties.length];
        for(int i = 0; i < keyProperties.length; i++) {
            PropertyAccessor property = keyProperties[i];
            Object value = property.get(object);
            String stringValue = OgnlUtils.convertValue(value, String.class);
            array[i] = stringValue;
        }
        return array;
    }

    public TextFormat createPkGenerator() {
        String formatString = getFormatString();
        return OgnlTextFormat.create(formatString);
    }

    @NotNull
    public String getFormatString() {
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
        return sb.toString();
    }

    public String getPkStringForUrl(Object o, String encoding) throws UnsupportedEncodingException {
        return getPkStringForUrl(generatePkStringArray(o), encoding);
    }

    public String getPkStringForUrl(String[] pk, String encoding) throws UnsupportedEncodingException {
        String[] escapedPk = new String[pk.length];
        for(int i = 0; i < pk.length; i++) {
            escapedPk[i] = URLEncoder.encode(pk[i], encoding);
        }
        return getPkString(escapedPk);
    }

    @Nullable
    public String getPkString(String[] pkStringArray) {
        return StringUtils.join(pkStringArray, "/");
    }

    /**
     * Formats an object's ID as a string. Composite IDs are separated with slashes.
     * @param object the object to compute the ID of.
     * @return the object ID as a string.
     */
    public String getPkString(Object object) {
        if (object instanceof String) {
            return (String) object;
        }
        return getPkString(generatePkStringArray(object));
    }
}
