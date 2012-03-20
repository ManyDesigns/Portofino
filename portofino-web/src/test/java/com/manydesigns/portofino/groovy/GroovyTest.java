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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
