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
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
