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

package com.manydesigns.portofino.js;

import com.manydesigns.elements.AbstractElementsTest;
import org.mozilla.javascript.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class RhinoTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    Context cx;
    Scriptable scope;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cx = Context.enter();
        scope = cx.initStandardObjects();
    }

    @Override
    public void tearDown() throws Exception {
        Context.exit();
        super.tearDown();
    }

    public void testStandardFunction() {
        String s = "Math.cos(Math.PI)";
        Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
        assertNotNull(result);
        assertEquals(Double.class, result.getClass());
        assertEquals(-1.0, result);
    }

    public void testMapBinding() {
        Map myMap = new HashMap();
        myMap.put("foo", 3);
        myMap.put("bar", "42");
        Object wrappedMap = new MapScriptable(myMap);
        ScriptableObject.putProperty(scope, "myMap", wrappedMap);

        String s = "myMap";
        Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
        assertEquals(wrappedMap, result);

        s = "myMap.foo";
        result = cx.evaluateString(scope, s, "<cmd>", 1, null);
        assertNotNull(result);
        assertEquals(Integer.class, result.getClass());
        assertEquals(3, result);

        s = "myMap['bar']";
        result = cx.evaluateString(scope, s, "<cmd>", 1, null);
        assertNotNull(result);
        assertEquals(String.class, result.getClass());
        assertEquals("42", result);

        s = "myMap['bar'] = 12";
        result = cx.evaluateString(scope, s, "<cmd>", 1, null);
        assertEquals(12, result);
        assertEquals(12, myMap.get("bar"));

        s = "myMap['bar'] = myMap";
        result = cx.evaluateString(scope, s, "<cmd>", 1, null);
        assertEquals(wrappedMap, result);
        assertEquals(wrappedMap, myMap.get("bar"));
    }

    public void testVariableDefinition() {
        assertFalse(scope.has("pippo", scope));

        String s = "var pippo = 2";
        Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
        assertEquals(2, scope.get("pippo", scope));
    }


    public void testException() {
        String s = "var pippo;\n pippo.bla = 0;";
        try {
            Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
            fail();
        } catch (EcmaError e) {
            assertNotNull(e);
            assertEquals("TypeError", e.getName());
            assertEquals("Cannot set property \"bla\" of undefined to \"0\"", e.getErrorMessage());
            assertEquals(2, e.lineNumber());
            assertNull(e.lineSource());
            assertEquals("<cmd>", e.sourceName());
        }
    }

    public void testReturn() {
        String s = "return true;";
        try {
            Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
            fail();
        } catch (EvaluatorException e) {
            assertEquals("invalid return (<cmd>#1)", e.getMessage());
        }
    }

}
