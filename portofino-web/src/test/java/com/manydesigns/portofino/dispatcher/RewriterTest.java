/*
* Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
