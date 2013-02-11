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

package com.manydesigns.elements.xml;

import junit.framework.TestCase;

/**
 * @author predo
 */
public class XmlBufferTest extends TestCase {
    public XmlBufferTest(String testName) {
        super(testName);
    }

    public void testWrite1() {
        XmlBuffer xb1 = new XmlBuffer();
        XmlBuffer xb2 = new XmlBuffer();

        xb1.openElement("elemento");
        xb1.write(xb2);
        xb1.closeElement("elemento");

        assertEquals("<elemento></elemento>", xb1.toString());
    }

    public void testWrite2() {
        XmlBuffer xb1 = new XmlBuffer();
        XmlBuffer xb2 = new XmlBuffer();

        xb2.openElement("bla");

        xb1.openElement("elemento");
        xb1.write(xb2);
        xb1.closeElement("bla");
        xb1.closeElement("elemento");

        assertEquals("<elemento><bla></bla></elemento>", xb1.toString());
    }

    public void testWrite3() {
        XmlBuffer xb1 = new XmlBuffer();

        xb1.openElement("elemento");
        xb1.write("€");
        xb1.closeElement("elemento");

        //assertEquals("<elemento>&euro;</elemento>", xb1.toString());
        //assertEquals("<elemento>€</elemento>", xb1.toString());
        assertEquals("<elemento>&#8364;</elemento>", xb1.toString());
    }

    public void testWrite4() {
        XmlBuffer xb = new XmlBuffer();
        assertEquals(0, xb.getAllowedEmptyTags().length);

        xb.setAllowedEmptyTags(new String[] {"elemento1"});

        xb.openElement("elemento1");
        xb.closeElement("elemento1");
        xb.openElement("elemento2");
        xb.closeElement("elemento2");

        // for browser compatibility, the empty tag tequires a space before the "/"
        assertEquals("<elemento1 /><elemento2></elemento2>", xb.toString());
    }

    public void testWrite5() {
        XmlBuffer xb1 = new XmlBuffer();

        xb1.write((String)null);
        assertEquals("", xb1.toString());

        xb1.writeNoHtmlEscape(null);
        assertEquals("", xb1.toString());

        xb1.write((XmlBuffer)null);
        assertEquals("", xb1.toString());
    }
}
