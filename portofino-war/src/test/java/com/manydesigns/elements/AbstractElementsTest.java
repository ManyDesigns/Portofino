package com.manydesigns.elements;

import com.manydesigns.elements.text.BasicTextProvider;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XmlBuffer;
import junit.framework.TestCase;
import ognl.Ognl;
import ognl.OgnlContext;

import java.util.Locale;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class AbstractElementsTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public DummyHttpServletRequest req;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        XmlBuffer.checkWellFormed = true;

        req = new DummyHttpServletRequest();
        req.setContextPath("");

        ElementsThreadLocals.setHttpServletRequest(req);

        ElementsThreadLocals.setTextProvider(
                new BasicTextProvider(Locale.ENGLISH));
        ElementsThreadLocals.setOgnlContext(
                (OgnlContext) Ognl.createDefaultContext(null));
    }

    public String elementToString(Element element) {
        XhtmlBuffer xb = new XhtmlBuffer();
        element.toXhtml(xb);
        return xb.toString();
    }
}
