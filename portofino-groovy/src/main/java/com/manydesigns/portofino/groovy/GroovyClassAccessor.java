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

package com.manydesigns.portofino.groovy;

import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import org.apache.commons.lang.ArrayUtils;

/**
 * {@link com.manydesigns.elements.reflection.ClassAccessor} for Groovy classes, that doesn't cache them as {@link JavaClassAccessor} does.
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class GroovyClassAccessor extends JavaClassAccessor {

    public final static String[] PROPERTY_NAME_BLACKLIST = {"metaClass"};

    public GroovyClassAccessor(Class javaClass) {
        super(javaClass); //Don't cache Groovy classes as they can be reloaded
    }

    @Override
    protected boolean isValidProperty(PropertyAccessor propertyAccessor) {
        // blacklisted?
        if (ArrayUtils.contains(PROPERTY_NAME_BLACKLIST, propertyAccessor.getName())) {
            return false;
        }
        return super.isValidProperty(propertyAccessor);
    }
}
