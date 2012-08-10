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

import com.manydesigns.elements.AbstractElementsTest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class MemoryUtilTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public void testBytesToHumanString() {
        assertEquals("0 bytes", MemoryUtil.bytesToHumanString(0));
        assertEquals("1 byte", MemoryUtil.bytesToHumanString(1));
        assertEquals("2 bytes", MemoryUtil.bytesToHumanString(2));
        assertEquals("10 bytes", MemoryUtil.bytesToHumanString(10));
        assertEquals("100 bytes", MemoryUtil.bytesToHumanString(100));
        assertEquals("999 bytes", MemoryUtil.bytesToHumanString(999));

        assertEquals("1.0K", MemoryUtil.bytesToHumanString(1000));
        assertEquals("1.0K", MemoryUtil.bytesToHumanString(1001));
        assertEquals("1.0K", MemoryUtil.bytesToHumanString(1049));
        assertEquals("1.1K", MemoryUtil.bytesToHumanString(1050));
        assertEquals("1.1K", MemoryUtil.bytesToHumanString(1100));
        assertEquals("9.9K", MemoryUtil.bytesToHumanString(9949));
        assertEquals("10K", MemoryUtil.bytesToHumanString(9950));
        assertEquals("10K", MemoryUtil.bytesToHumanString(9951));
        assertEquals("10K", MemoryUtil.bytesToHumanString(10000));

        assertEquals("999K", MemoryUtil.bytesToHumanString(999499));
        assertEquals("1.0M", MemoryUtil.bytesToHumanString(999500));
        assertEquals("1.0M", MemoryUtil.bytesToHumanString(1049999));
        assertEquals("1.1M", MemoryUtil.bytesToHumanString(1050000));

        assertEquals("1.0G", MemoryUtil.bytesToHumanString(1000000000L));
        assertEquals("1.0T", MemoryUtil.bytesToHumanString(1000000000000L));
        assertEquals("1.0P", MemoryUtil.bytesToHumanString(1000000000000000L));
    }
}
