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

package com.manydesigns.elements.gfx;

import java.awt.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ColorUtils {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";


    public static Color add(Color color1, Color color2) {
        float[] components1 = color1.getRGBComponents(null);
        float[] components2 = color2.getRGBComponents(null);
        float[] components = new float[4];
        for (int i = 0; i < 4; i++) {
            components[i] = Math.min(components1[i] + components2[i], 1.0f);
        }
        return new Color(components[0], components[1], components[2], components[3]);
    }

    public static Color subtract(Color color1, Color color2) {
        float[] components1 = color1.getRGBComponents(null);
        float[] components2 = color2.getRGBComponents(null);
        float[] components = new float[4];
        for (int i = 0; i < 4; i++) {
            components[i] = Math.max(components1[i] - components2[i], 0.0f);
        }
        return new Color(components[0], components[1], components[2], components[3]);
    }

    public static Color average(Color color1, Color color2) {
        float[] components1 = color1.getRGBComponents(null);
        float[] components2 = color2.getRGBComponents(null);
        float[] components = new float[4];
        for (int i = 0; i < 4; i++) {
            components[i] = 0.5f * (components1[i] + components2[i]);
        }
        return new Color(components[0], components[1], components[2], components[3]);
    }

    public static Color multiply(Color color1, Color color2) {
        float[] components1 = color1.getRGBComponents(null);
        float[] components2 = color2.getRGBComponents(null);
        float[] components = new float[4];
        for (int i = 0; i < 4; i++) {
            components[i] = components1[i] * components2[i];
        }
        return new Color(components[0], components[1], components[2], components[3]);
    }

    public static Color screen(Color color1, Color color2) {
        float[] components1 = color1.getRGBComponents(null);
        float[] components2 = color2.getRGBComponents(null);
        float[] components = new float[4];
        for (int i = 0; i < 4; i++) {
            components[i] = 1.0f - (1.0f - components1[i]) * (1.0f - components2[i]);
        }
        return new Color(components[0], components[1], components[2], components[3]);
    }

    public static float getLuminance(Color color) {
        float[] components = color.getRGBComponents(null);
        return 0.3f * components[0] +
                0.59f * components[1] +
                0.11f * components[2];
    }

    public static String toHtmlColor(Color color) {
        return String.format("#%06x", color.getRGB() & 0xffffff);
    }
}
