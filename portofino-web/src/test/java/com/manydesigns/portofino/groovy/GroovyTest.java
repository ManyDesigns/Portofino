/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.groovy;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.ElementsThreadLocals;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import ognl.OgnlContext;

import java.io.File;
import java.io.IOException;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class GroovyTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testOgnlBinding() {
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();

        Binding binding = new Binding(ognlContext);

        ognlContext.put("foo", 2);
        assertEquals(2, binding.getVariable("foo"));

        GroovyShell shell = new GroovyShell(binding);

        Object value = shell.evaluate(
                "println 'Hello World!'; x = 123; return foo * 10");
        assertEquals(20, value);
        assertEquals(123, binding.getVariable("x"));
    }

    public void testGroovyFile() throws IOException, NoSuchMethodException {
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();

        Binding binding = new Binding(ognlContext);

        ognlContext.put("bar", 2);
        assertEquals(2, binding.getVariable("bar"));

        GroovyShell shell = new GroovyShell(binding);

        shell.evaluate(new File("/Users/alessio/projects/portofino4/portofino-web/src/test/java/com/manydesigns/portofino/groovy/Pippo.groovy"));
        Object value = shell.evaluate("new com.manydesigns.portofino.groovy.Pippo().foo()");
        assertFalse((Boolean) value);
        shell.evaluate(new File("/Users/alessio/projects/portofino4/portofino-web/src/test/java/com/manydesigns/portofino/groovy/Pippo.groovy"));
        value = shell.evaluate("new com.manydesigns.portofino.groovy.Pippo().foo()");
        assertFalse((Boolean) value);
    }

}
