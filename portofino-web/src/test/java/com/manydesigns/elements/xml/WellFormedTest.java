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

package com.manydesigns.elements.xml;

import com.manydesigns.elements.AbstractElementsTest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class WellFormedTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public void testUnderflow() {
        XmlBuffer xb = new XmlBuffer();
        try {
            xb.openElement("bla");
            xb.closeElement("bla");
            xb.closeElement("bla");
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Stack underflow: <bla></bla>", e.getMessage());
        }
    }

    public void testNoCheckWellFormed() {
        XmlBuffer.checkWellFormed = false;
        XmlBuffer xb = new XmlBuffer();
        xb.openElement("bla");
        xb.closeElement("bla");
        xb.closeElement("bla");
    }
    public void testMismatch() {
        XmlBuffer xb = new XmlBuffer();
        try {
            xb.openElement("foo");
            xb.closeElement("bar");
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Expected: foo - Actual: bar\n<foo", e.getMessage());
        }
    }

    public void testUnclosed() {
        try {
            XmlBuffer xb = new XmlBuffer();
            xb.openElement("foo");
            xb.toString();
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Stack not empty: <foo", e.getMessage());
        }
    }
}
