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

package com.manydesigns.elements.util;

import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class MemoryUtil {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public final static double LOG2 = Math.log(2);

    public final static String[] suffixes = {
            " bytes",
            "K",
            "M",
            "G",
            "T",
            "P"
    };

    public static String bytesToHumanString(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("Negative argument: " + bytes);
        }
        if (bytes == 1) {
            return "1 byte";
        } else if (bytes == 0) {
            return MessageFormat.format("0{1}", bytes, suffixes[0]);
        }
        int log = (int) Math.log10(bytes);
        int pos = log / 3;
        double scaled = bytes / Math.pow(10, pos * 3);
        long rounded = Math.round(scaled);
        double rounded2 = (double)Math.round(scaled * 10) / 10d;

        if (pos == 0) {
            return MessageFormat.format("{0,number,0}{1}",
                    rounded, suffixes[pos]);
        } else if (rounded >= 1000) {
            scaled = scaled / 1000;
            pos = pos + 1;
            rounded2 = (double)Math.round(scaled * 10) / 10d;
            return MessageFormat.format("{0,number,0.0}{1}",
                    rounded2, suffixes[pos]);
        } else if (rounded2 < 10) {
            return MessageFormat.format("{0,number,0.0}{1}",
                    rounded2, suffixes[pos]);
        } else {
            return MessageFormat.format("{0,number,0}{1}",
                    rounded, suffixes[pos]);
        }
    }
}
