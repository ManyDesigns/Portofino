package com.manydesigns.elements;

import com.manydesigns.elements.servlet.MutableHttpServletRequest;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XmlBuffer;
import junit.framework.TestCase;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class AbstractElementsTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public MutableHttpServletRequest req;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        XmlBuffer.checkWellFormed = true;

        req = new MutableHttpServletRequest();
        req.setContextPath("");

        ElementsThreadLocals.setupDefaultElementsContext();
        ElementsThreadLocals.setHttpServletRequest(req);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ElementsThreadLocals.removeElementsContext();
    }

    public String elementToString(Element element) {
        XhtmlBuffer xb = new XhtmlBuffer();
        element.toXhtml(xb);
        return xb.toString();
    }
}
