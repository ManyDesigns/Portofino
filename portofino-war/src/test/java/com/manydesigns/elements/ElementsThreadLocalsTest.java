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

package com.manydesigns.elements;

import com.manydesigns.elements.composites.AbstractCompositeElement;
import com.manydesigns.elements.xml.XhtmlBuffer;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ElementsThreadLocalsTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    private TestComposite1<Element> composite;
    private TestElement1 element;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        composite = new TestComposite1<Element>();
        element = new TestElement1();
        composite.add(element);
    }

    public void testAllMethods() {
        assertNull(composite.getId());
        composite.readFromRequest(null);
        composite.readFromObject(null);
        composite.validate();
        composite.toXhtml(null);
    }

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

        public Mode getMode() {
            return null;
        }

        public void setMode(Mode mode) {
        }
    }
}
