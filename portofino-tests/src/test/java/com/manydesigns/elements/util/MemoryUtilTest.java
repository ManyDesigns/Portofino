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

import com.manydesigns.elements.AbstractElementsTest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class MemoryUtilTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
