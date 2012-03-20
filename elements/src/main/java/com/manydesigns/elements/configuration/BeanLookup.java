/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.elements.configuration;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class BeanLookup extends StrLookup {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected final Object bean;
    protected final ClassAccessor accessor;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LoggerFactory.getLogger(BeanLookup.class);

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public BeanLookup(Object bean) {
        this.bean = bean;
        Class clazz = bean.getClass();
        accessor = JavaClassAccessor.getClassAccessor(clazz);
    }

    //--------------------------------------------------------------------------
    // StrLookup implementation
    //--------------------------------------------------------------------------

    @Override
    public String lookup(String key) {
        try {
            PropertyAccessor property = accessor.getProperty(key);
            return ObjectUtils.toString(property.get(bean));
        } catch (NoSuchFieldException e) {
            logger.warn("Cannot access property '{}' on class '{}'",
                    key,
                    accessor.getName());
            return null;
        }
    }
}
