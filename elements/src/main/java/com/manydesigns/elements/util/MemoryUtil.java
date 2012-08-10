/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
