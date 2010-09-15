/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

import junit.framework.TestCase;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UtilTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public void testMatchStringArray1() {
        String[] result = Util.matchStringArray("\"qui\",\"quo\",\"qua\"");
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("qui", result[0]);
        assertEquals("quo", result[1]);
        assertEquals("qua", result[2]);
    }

    public void testMatchStringArray2() {
        String[] result = Util.matchStringArray(" \"qui\" , \"quo\" , \"qua\" ");
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("qui", result[0]);
        assertEquals("quo", result[1]);
        assertEquals("qua", result[2]);
    }

    public void testMatchStringArray3() {
        String[] result = Util.matchStringArray("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    public void testMatchStringArray4() {
        String[] result = Util.matchStringArray(" \"qui\" ");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("qui", result[0]);
    }

    public void testMatchStringArray5() {
        String[] result = Util.matchStringArray("\"qui\",");
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("qui", result[0]);
        assertEquals("", result[1]);
    }

    public void testMatchStringArray6() {
        String[] result = Util.matchStringArray(",\"qui\"");
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("", result[0]);
        assertEquals("qui", result[1]);
    }

    public void testMatchStringArray7() {
        String[] result = Util.matchStringArray("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    public void testMatchStringArray8() {
        String[] result = Util.matchStringArray("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }


    public void testMatchStringArray9() {
        String[] result = Util.matchStringArray("\"qu\"\"i\",\"qu\"\"o\",\"qu\"\"a\"");
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("qu\"i", result[0]);
        assertEquals("qu\"o", result[1]);
        assertEquals("qu\"a", result[2]);
    }


    public void testMatchStringArray10() {
        String[] result = Util.matchStringArray("qu i , q uo,q u a");
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("qu i", result[0]);
        assertEquals("q uo", result[1]);
        assertEquals("q u a", result[2]);
    }
}
