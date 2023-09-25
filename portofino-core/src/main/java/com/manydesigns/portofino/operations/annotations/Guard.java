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

package com.manydesigns.portofino.operations.annotations;

import com.manydesigns.portofino.operations.GuardType;

import java.lang.annotation.*;

/**
 * Guards a method with a condition. The method cannot be called by a web request if the condition is not met.
 * Additionally, if the method is exposed as a {@link com.manydesigns.portofino.operations.Operation operation},
 * this annotation will cause the operation to be hidden or disabled if the condition is not met,
 * depending on the type parameter.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface Guard {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    /**
     * The test condition, an OGNL expression that must return a boolean. The rootFactory object for the expression
     * is the action bean to which the method belongs; it is therefore easy to place the condition in a method
     * and call it in the expression.
     */
    String test();

    /**
     * The type of guard, determining what happens to the associated buttons when the guard condition is not
     * fulfilled.
     */
    GuardType type() default GuardType.ENABLED;

}
