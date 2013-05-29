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

package com.manydesigns.portofino.servlets;

import junit.framework.TestCase;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class TestForbiddenUrls extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public void testForbiddenUrls() {
        try {
            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/web/../dbs/redmine-PUBLIC-changelog.xml", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/web/../dbs/redmine-PUBLIC-changelog.xml", "UTF-8"));

            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/web/%2e%2e/dbs/redmine-PUBLIC-changelog.xml", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/web/%2e%2e/dbs/redmine-PUBLIC-changelog.xml", "UTF-8"));

            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/web/.%2e/dbs/redmine-PUBLIC-changelog.xml", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/web/.%2e/dbs/redmine-PUBLIC-changelog.xml", "UTF-8"));

            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/web/%2e./dbs/redmine-PUBLIC-changelog.xml", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/web/%2e./dbs/redmine-PUBLIC-changelog.xml", "UTF-8"));

            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/dbs/redmine-PUBLIC-changelog.xml", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("/apps/demo-tt/dbs/redmine-PUBLIC-changelog.xml", "ISO-8859-1"));

            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/web/../../../foo", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/web/../../../foo", "UTF-8"));

            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/web/../%2e%2e/../foo", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("/app/web/../%2e%2e/../foo", "UTF-8"));

            assertTrue(ApplicationFilter.filterForbiddenUrls("/app/web/latestProjects.jsp", "UTF-8"));
            assertTrue(ApplicationFilter.filterForbiddenUrls("/app/web/latestProjects.jsp?foo=..", "UTF-8"));
            assertTrue(ApplicationFilter.filterForbiddenUrls("/app/web/latestProjects.jsp?foo=%2e%2e", "UTF-8"));
            assertTrue(ApplicationFilter.filterForbiddenUrls("/app/web/latestProjects.jsp?foo=%2e%2e", "ISO-8859-1"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
