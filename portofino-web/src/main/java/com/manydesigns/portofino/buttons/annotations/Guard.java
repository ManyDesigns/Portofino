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

package com.manydesigns.portofino.buttons.annotations;

import com.manydesigns.portofino.buttons.GuardType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Guards a method with a condition. The method cannot be called by a web request if the condition is not met.
 * Additionally, if the method is exposed as a {@link Button button}, this annotation will cause the button
 * to be hidden or disabled if the condition is not met, depending on the type parameter.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Guard {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    /**
     * The test condition, an OGNL expression that must return a boolean. The root object for the expression
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
