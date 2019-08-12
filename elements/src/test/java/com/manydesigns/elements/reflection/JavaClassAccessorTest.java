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

package com.manydesigns.elements.reflection;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.annotations.Key;
import com.manydesigns.elements.util.ReflectionUtil;
import org.json.JSONStringer;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Test
public class JavaClassAccessorTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static class TestBeanNoPk {
        public String p1, p2;
    }

    public static class TestBeanDefaultPkSingle {
        @Key
        public String key;
        public String p1, p2;
    }

    public static class TestBeanDefaultPkMulti {
        @Key(order = 2)
        public String key2;
        @Key(order = 1)
        public String key1;
        public String p1, p2;
    }

    public static class TestBeanMultiPk {
        @Key(order = 2)
        public String key2;
        @Key(order = 1)
        public String key1;
        @Key(name = "alternate")
        public String alternateKey;
        public String p1, p2;
    }

    @Key(name = "primary")
    public static class TestBeanMultiPkWithDefault {
        @Key(name = "primary", order = 2)
        public String key2;
        @Key(name = "primary", order = 1)
        public String key1;
        @Key(name = "alternate")
        public String alternateKey;
        public String p1, p2;
    }

    @Key(name = "wrong")
    public static class TestBeanMultiPkWithWrongDefault {
        @Key(order = 2)
        public String key2;
        @Key(order = 1)
        public String key1;
        @Key(name = "alternate")
        public String alternateKey;
        public String p1, p2;
    }

    @Key(name = "wrong")
    public static class TestBeanMultiPkWithWrongDefaultNoFallback {
        @Key(name = "primary", order = 2)
        public String key2;
        @Key(name = "primary", order = 1)
        public String key1;
        //@Key(name = "alternate") non testable (prop order not guaranteed)
        //public String alternateKey;
        public String p1, p2;
    }

    public void testKeyPropertyAccessors() throws Exception {
        JavaClassAccessor javaClassAccessor;

        javaClassAccessor = new JavaClassAccessor(TestBeanNoPk.class);
        assertEquals(0, javaClassAccessor.getKeyProperties().length);

        javaClassAccessor = new JavaClassAccessor(TestBeanDefaultPkSingle.class);
        assertEquals(1, javaClassAccessor.getKeyProperties().length);
        assertEquals("key", javaClassAccessor.getKeyProperties()[0].getName());

        javaClassAccessor = new JavaClassAccessor(TestBeanDefaultPkMulti.class);
        assertEquals(2, javaClassAccessor.getKeyProperties().length);
        assertEquals("key1", javaClassAccessor.getKeyProperties()[0].getName());
        assertEquals("key2", javaClassAccessor.getKeyProperties()[1].getName());

        javaClassAccessor = new JavaClassAccessor(TestBeanMultiPk.class);
        assertEquals(2, javaClassAccessor.getKeyProperties().length);
        assertEquals("key1", javaClassAccessor.getKeyProperties()[0].getName());
        assertEquals("key2", javaClassAccessor.getKeyProperties()[1].getName());

        javaClassAccessor = new JavaClassAccessor(TestBeanMultiPkWithDefault.class);
        assertEquals(2, javaClassAccessor.getKeyProperties().length);
        assertEquals("key1", javaClassAccessor.getKeyProperties()[0].getName());
        assertEquals("key2", javaClassAccessor.getKeyProperties()[1].getName());

        javaClassAccessor = new JavaClassAccessor(TestBeanMultiPkWithWrongDefault.class);
        assertEquals(2, javaClassAccessor.getKeyProperties().length);
        assertEquals("key1", javaClassAccessor.getKeyProperties()[0].getName());
        assertEquals("key2", javaClassAccessor.getKeyProperties()[1].getName());

        javaClassAccessor = new JavaClassAccessor(TestBeanMultiPkWithWrongDefaultNoFallback.class);
        assertEquals(2, javaClassAccessor.getKeyProperties().length);
        assertEquals("key1", javaClassAccessor.getKeyProperties()[0].getName());
        assertEquals("key2", javaClassAccessor.getKeyProperties()[1].getName());
    }

    @Test(enabled = false) //TODO find a way to compare JSON values (JSONAssert only supports JUnit)
    public void testJSON() {
        ClassAccessor javaClassAccessor = new JavaClassAccessor(TestBeanMultiPk.class);
        JSONStringer jsonStringer = new JSONStringer();
        ReflectionUtil.classAccessorToJson(javaClassAccessor, jsonStringer);
        assertEquals(
                jsonStringer.toString(),
                "{\"name\":\"com.manydesigns.elements.reflection.JavaClassAccessorTest$TestBeanMultiPk\",\"keyProperties\":[\"key1\",\"key2\"],\"properties\":[{\"name\":\"key2\",\"type\":\"java.lang.String\",\"modifiers\":[\"public\"],\"label\":\"key2\",\"annotations\":[{\"type\":\"com.manydesigns.elements.annotations.Key\",\"properties\":{\"order\":2,\"name\":\"default\"}}]},{\"name\":\"key1\",\"type\":\"java.lang.String\",\"modifiers\":[\"public\"],\"label\":\"key1\",\"annotations\":[{\"type\":\"com.manydesigns.elements.annotations.Key\",\"properties\":{\"order\":1,\"name\":\"default\"}}]},{\"name\":\"alternateKey\",\"type\":\"java.lang.String\",\"modifiers\":[\"public\"],\"label\":\"alternate key\",\"annotations\":[{\"type\":\"com.manydesigns.elements.annotations.Key\",\"properties\":{\"order\":-2147483648,\"name\":\"alternate\"}}]},{\"name\":\"p1\",\"type\":\"java.lang.String\",\"modifiers\":[\"public\"],\"label\":\"p1\",\"annotations\":[]},{\"name\":\"p2\",\"type\":\"java.lang.String\",\"modifiers\":[\"public\"],\"label\":\"p2\",\"annotations\":[]}],\"annotations\":[]}");
    }
}
