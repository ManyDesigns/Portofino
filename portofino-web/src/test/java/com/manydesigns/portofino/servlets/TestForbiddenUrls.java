/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/../dbs/redmine-PUBLIC-changelog.xml", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/../dbs/redmine-PUBLIC-changelog.xml", "UTF-8"));

            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/%2e%2e/dbs/redmine-PUBLIC-changelog.xml", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/%2e%2e/dbs/redmine-PUBLIC-changelog.xml", "UTF-8"));

            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/.%2e/dbs/redmine-PUBLIC-changelog.xml", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/.%2e/dbs/redmine-PUBLIC-changelog.xml", "UTF-8"));

            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/%2e./dbs/redmine-PUBLIC-changelog.xml", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/%2e./dbs/redmine-PUBLIC-changelog.xml", "UTF-8"));

            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/dbs/redmine-PUBLIC-changelog.xml", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/apps/demo-tt/dbs/redmine-PUBLIC-changelog.xml", "ISO-8859-1"));

            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/../../../foo", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/../../../foo", "UTF-8"));

            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/../%2e%2e/../foo", "ISO-8859-1"));
            assertFalse(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/../%2e%2e/../foo", "UTF-8"));

            assertTrue(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/latestProjects.jsp", "UTF-8"));
            assertTrue(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/latestProjects.jsp?foo=..", "UTF-8"));
            assertTrue(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/latestProjects.jsp?foo=%2e%2e", "UTF-8"));
            assertTrue(ApplicationFilter.filterForbiddenUrls("http://localhost:8080/app/web/latestProjects.jsp?foo=%2e%2e", "ISO-8859-1"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
