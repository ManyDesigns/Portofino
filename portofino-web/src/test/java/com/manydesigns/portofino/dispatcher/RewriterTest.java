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

package com.manydesigns.portofino.dispatcher;

import junit.framework.TestCase;

import java.util.regex.Pattern;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class RewriterTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final Pattern FILTER_NOT_WEB_PATTERN = Pattern.compile("^/apps/[^/]+/(?!web).*$");
    public static final Pattern FILTER_PARENT_DIR_PATTERN = Pattern.compile("^/apps/(?=.*(\\.|%2e)(\\.|%2e)).*$");

    public void testNotWebPattern() {
        assertFalse(FILTER_NOT_WEB_PATTERN.matcher("/apps/test/web/test.jsp").matches());
        assertFalse(FILTER_NOT_WEB_PATTERN.matcher("/apps/test/web/").matches());
        assertFalse(FILTER_NOT_WEB_PATTERN.matcher("/apps/test/web").matches());
        assertTrue(FILTER_NOT_WEB_PATTERN.matcher("/apps/test/groovy").matches());
        assertTrue(FILTER_NOT_WEB_PATTERN.matcher("/apps/test/groovy/").matches());
        assertTrue(FILTER_NOT_WEB_PATTERN.matcher("/apps/test/groovy/test.groovy").matches());
        assertTrue(FILTER_NOT_WEB_PATTERN.matcher("/apps/test/groovy/web").matches());

        assertFalse(FILTER_NOT_WEB_PATTERN.matcher("/apps/test-2/web/test.jsp").matches());
        assertFalse(FILTER_NOT_WEB_PATTERN.matcher("/apps/test-2/web/").matches());
        assertFalse(FILTER_NOT_WEB_PATTERN.matcher("/apps/test-2/web").matches());
        assertTrue(FILTER_NOT_WEB_PATTERN.matcher("/apps/test-2/groovy").matches());
        assertTrue(FILTER_NOT_WEB_PATTERN.matcher("/apps/test-2/groovy/").matches());
        assertTrue(FILTER_NOT_WEB_PATTERN.matcher("/apps/test-2/groovy/test.groovy").matches());
        assertTrue(FILTER_NOT_WEB_PATTERN.matcher("/apps/test-2/groovy/web").matches());

        assertFalse(FILTER_NOT_WEB_PATTERN.matcher("/apps/test_3/web/test.jsp").matches());
        assertFalse(FILTER_NOT_WEB_PATTERN.matcher("/apps/test_3/web/").matches());
        assertFalse(FILTER_NOT_WEB_PATTERN.matcher("/apps/test_3/web").matches());
        assertTrue(FILTER_NOT_WEB_PATTERN.matcher("/apps/test_3/groovy").matches());
        assertTrue(FILTER_NOT_WEB_PATTERN.matcher("/apps/test_3/groovy/").matches());
        assertTrue(FILTER_NOT_WEB_PATTERN.matcher("/apps/test_3/groovy/test.groovy").matches());
        assertTrue(FILTER_NOT_WEB_PATTERN.matcher("/apps/test_3/groovy/web").matches());
    }

    public void testParentDirPattern() {
        assertTrue(FILTER_PARENT_DIR_PATTERN.matcher("/apps/test/web/../groovy").matches());
        assertTrue(FILTER_PARENT_DIR_PATTERN.matcher("/apps/test/web/../groovy/").matches());
        assertTrue(FILTER_PARENT_DIR_PATTERN.matcher("/apps/test/web/../groovy/test.groovy").matches());
        assertTrue(FILTER_PARENT_DIR_PATTERN.matcher("/apps/test/web/../groovy/web").matches());

        assertTrue(FILTER_PARENT_DIR_PATTERN.matcher("/apps/test/web/foo/../../groovy").matches());
        assertTrue(FILTER_PARENT_DIR_PATTERN.matcher("/apps/test/web/foo/../../groovy/").matches());
        assertTrue(FILTER_PARENT_DIR_PATTERN.matcher("/apps/test/web/foo/../../groovy/test.groovy").matches());
        assertTrue(FILTER_PARENT_DIR_PATTERN.matcher("/apps/test/web/foo/../../groovy/web").matches());

        assertTrue(FILTER_PARENT_DIR_PATTERN.matcher("/apps/test/web/%2e%2e/groovy").matches());
        assertTrue(FILTER_PARENT_DIR_PATTERN.matcher("/apps/test/web/%2e./groovy").matches());
        assertTrue(FILTER_PARENT_DIR_PATTERN.matcher("/apps/test/web/.%2e/groovy").matches());
    }

}
