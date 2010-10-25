package com.manydesigns.elements;

import com.manydesigns.elements.servlet.MutableHttpServletRequest;
import com.manydesigns.elements.servlet.WebFramework;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XmlBuffer;
import com.manydesigns.portofino.PortofinoProperties;
import junit.framework.TestCase;

import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class AbstractElementsTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public Properties elementsProperties;
    public Properties portofinoProperties;

    public MutableHttpServletRequest req;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        XmlBuffer.checkWellFormed = true;

        setUpProperties();
        setUpSingletons();
        setUpRequest();
        setUpElementsThreadLocals();
    }

    public void setUpProperties() {
        // restore default properties
        ElementsProperties.reloadProperties();
        PortofinoProperties.reloadProperties();

        elementsProperties = ElementsProperties.getProperties();
        portofinoProperties = PortofinoProperties.getProperties();
    }

    public void setUpSingletons() {
        WebFramework.resetSingleton();
    }

    public void setUpRequest() {
        req = new MutableHttpServletRequest();
        req.setContextPath("");
    }

    public void setUpElementsThreadLocals() {
        ElementsThreadLocals.setupDefaultElementsContext();
        ElementsThreadLocals.setHttpServletRequest(req);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        ElementsThreadLocals.removeElementsContext();
    }

    public String elementToString(Element element) {
        XhtmlBuffer xb = new XhtmlBuffer();
        element.toXhtml(xb);
        return xb.toString();
    }
}
