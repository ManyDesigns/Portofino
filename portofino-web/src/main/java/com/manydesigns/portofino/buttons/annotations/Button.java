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

package com.manydesigns.portofino.buttons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that the method on which this annotation is attached is to be exposed as a button on a web page.
 * This annotation is only supposed to work on handler methods in a
 * {@link com.manydesigns.portofino.actions.admin.page.PageAdminAction}.
 *
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Button {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    /**
     * The list where this button is to be placed. Web pages will include lists of buttons by name.
     */
    String list();

    /**
     * The order of the button inside the list. Buttons with lower order come before buttons with higher order.
     */
    double order() default 1.0;

    /**
     * The resource bundle key for the button's label.
     */
    String key() default "";

    /**
     * The name of the button's icon. This is a CSS class that is added to the button element.
     */
    String icon() default "";
}
