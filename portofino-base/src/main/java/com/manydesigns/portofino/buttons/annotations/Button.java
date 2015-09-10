/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that the method on which this annotation is attached is to be exposed as a button on a web page.
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
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String
            TYPE_DEFAULT = " btn-default ",
            TYPE_PRIMARY = " btn-primary ",
            TYPE_INFO = " btn-info ",
            TYPE_SUCCESS = " btn-success ",
            TYPE_WARNING = " btn-warning ",
            TYPE_DANGER = " btn-danger ",
            TYPE_INVERSE = " btn-inverse ",
            TYPE_LINK = " btn-link ",
            TYPE_NO_UI_BLOCK = " no-ui-block ";

    public static final String
            ICON_WHITE = " white ",
            ICON_EDIT = " glyphicon-edit ",
            ICON_PLUS = " glyphicon-plus ",
            ICON_MINUS = " glyphicon-minus ",
            ICON_TRASH = " glyphicon-trash ",
            ICON_WRENCH = " glyphicon-wrench ",
            ICON_RELOAD= " glyphicon-refresh ",
            ICON_PICTURE = " glyphicon-picture ",
            ICON_SEARCH = " glyphicon-search ",
            ICON_REMOVE = " glyphicon-remove ",
            ICON_MAP_MARKER = " glyphicon-map-marker ",
            ICON_COMMENT = " glyphicon-comment ",
            ICON_PAPERCLIP = " glyphicon-paperclip ",
            ICON_IMPORT = " glyphicon-import ",
            ICON_EXPORT = " glyphicon-export ",
            ICON_RIGHT = " glyphicon-chevron-right ",
            ICON_LEFT= " glyphicon-chevron-left ",
            ICON_HOME= " glyphicon-home",
            ICON_SAVE= " glyphicon-floppy-disk",
            ICON_DUPLICATE = " glyphicon-duplicate ",
            ICON_UPLOAD = " glyphicon-upload ",
            ICON_DOWNLOAD = " glyphicon-download ",
            ICON_FLASH = " glyphicon-flash ",
            ICON_GEAR = " glyphicon-cog ";


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
     * The resource bundle key for the button's title (shown as a tooltip on most browsers).
     */
    String titleKey() default "";

    /**
     * The name of the button's icon. You can add " white" after the name to produce a white icon instead of
     * a black one.
     */
    String icon() default "";

    /**
     * Allows to specify the type of button to render: for example "primary", "link" or "success".
     */
    String type() default TYPE_DEFAULT;

    /**
     * The group this button belongs to. Buttons in the same group are rendered close together to separate them
     * from other groups.
     */
    String group() default "default-group";

    /**
     * Allows to specify the icon position (before text or not).
     */
    boolean iconBefore() default true;
}
