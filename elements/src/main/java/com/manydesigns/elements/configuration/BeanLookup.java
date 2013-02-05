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
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
