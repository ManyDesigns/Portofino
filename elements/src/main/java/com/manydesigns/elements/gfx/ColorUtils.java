/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
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
            "Copyright (c) 2005-2011, ManyDesigns srl";


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

    public static String toHexString(Color color) {
        return String.format("%x", color.getRGB());
    }
}
