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

package com.manydesigns.elements;

import com.manydesigns.elements.composites.AbstractCompositeElement;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ElementTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2019, ManyDesigns srl";

    private TestComposite1<Element> composite;
    private TestElement1 element;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        composite = new TestComposite1<>();
        element = new TestElement1();
        composite.add(element);
    }

    @Test
    public void testAllMethods() {
        assertNull(composite.getId());
        composite.readFromRequest(null);
        composite.readFromObject(null);
        composite.validate();
        XhtmlBuffer xb = new XhtmlBuffer();
        composite.toXhtml(xb);
    }

    @Test
    public void testCompositeId() {
        assertNull(composite.getId());
        composite.setId("prova");
        assertNotNull(composite.getId());

        composite.readFromRequest(null);
    }

    class TestComposite1<T extends Element> extends AbstractCompositeElement<T> {
        public void toXhtml(@NotNull XhtmlBuffer xb) {
            for (Element current : this) {
                current.toXhtml(xb);
            }
        }
    }

    class TestElement1 implements Element {
        public String getId() {
            return null;
        }

        public void readFromRequest(HttpServletRequest req) {
        }

        public boolean validate() {
            return false;
        }

        public void readFromObject(Object obj) {
        }

        public void writeToObject(Object obj) {
        }

        public void toXhtml(@NotNull XhtmlBuffer xb) {
        }

        @Override
        public boolean isValid() {
            return true;
        }

        public Mode getMode() {
            return null;
        }

        public void setMode(Mode mode) {}
    }
}
