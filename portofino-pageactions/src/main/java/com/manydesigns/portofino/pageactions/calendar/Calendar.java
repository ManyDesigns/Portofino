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

package com.manydesigns.portofino.pageactions.calendar;

import com.manydesigns.elements.gfx.ColorUtils;

import java.awt.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Calendar {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected String id;
    protected String name;
    protected Color foregroundColor;
    protected Color backgroundColor;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public Calendar() {
    }

    public Calendar(String id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.backgroundColor = color;
        this.foregroundColor = ColorUtils.subtract(backgroundColor, new Color(0x16, 0x16, 0x16));
    }

    public Calendar(String id, String name, Color backgroundColor, Color foregroundColor) {
        this.id = id;
        this.name = name;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getForegroundHtmlColor() {
        return ColorUtils.toHtmlColor(foregroundColor);
    }

    public String getBackgroundHtmlColor() {
        return ColorUtils.toHtmlColor(backgroundColor);
    }
}
