/*
 * $Source: /home/cvs/cvsroot/portofino/manydesigns-portofino/test/com/manydesigns/methods/XmlBufferTest.java,v $
 * $Id: XmlBufferTest.java,v 1.1 2007-04-18 15:54:40 predo Exp $
 *
 * Copyright ManyDesigns srl 2006-2009
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
        XmlBuffer xb1 = new XmlBuffer();

        xb1.write((String)null);
        assertEquals("", xb1.toString());

        xb1.writeNoHtmlEscape(null);
        assertEquals("", xb1.toString());

        xb1.write((XmlBuffer)null);
        assertEquals("", xb1.toString());
    }
}
