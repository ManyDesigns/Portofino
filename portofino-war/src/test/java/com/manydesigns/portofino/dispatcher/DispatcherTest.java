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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.AbstractPortofinoTest;
import com.manydesigns.portofino.model.site.CrudNode;
import com.manydesigns.portofino.model.site.RootNode;
import com.manydesigns.portofino.model.site.SiteNode;
import com.manydesigns.portofino.navigation.Navigation;

import java.util.Collections;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla - alessio.stalla@manydesigns.com
*/
public class DispatcherTest extends AbstractPortofinoTest {

    public static final String CRUD_ACTION = "/crud.action";
    public static final String CHART_ACTION = "/chart.action";

    Dispatcher dispatcher;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dispatcher = new Dispatcher(application);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        application.closeSessions();
    }

    public void testProjectSearch() {
        String originalPath = "/projects";
        req.setServletPath(originalPath);
        Dispatch dispatch = dispatcher.createDispatch(req);
        assertNotNull(dispatch);

        assertEquals(originalPath, dispatch.getOriginalPath());
        assertEquals(CRUD_ACTION, dispatch.getRewrittenPath());

        SiteNodeInstance[] siteNodeInstancePath =
                dispatch.getSiteNodeInstancePath();
        assertEquals(1, siteNodeInstancePath.length);

        CrudNodeInstance siteNodeInstance = (CrudNodeInstance) siteNodeInstancePath[0];
        SiteNode siteNode = model.getRootNode().getChildNodes().get(0);
        assertEquals(siteNode, siteNodeInstance.getSiteNode());
        assertEquals(CrudNode.MODE_SEARCH, siteNodeInstance.getMode());
        assertNull(siteNodeInstance.getPk());

        Navigation navigation =
                new Navigation(application, dispatch, Collections.EMPTY_LIST);
        /*
        List<NavigationNode> rootNodes = navigation.getRootNodes();

        // Navigation node per /projects
        assertEquals(1, rootNodes.size());
        NavigationNode navigationNode = rootNodes.get(0);
        assertEquals("/projects", navigationNode.getUrl());
        assertEquals(siteNode, navigationNode.getSiteNode());
        assertTrue(navigationNode.isEnabled());

        // Navigation node per /projects/tickets
        assertEquals(1, navigationNode.getChildNodes().size());
        navigationNode = navigationNode.getChildNodes().get(0);
        assertEquals("/projects/tickets", navigationNode.getUrl());
        siteNode = siteNode.getChildNodes().get(0);
        assertEquals(siteNode, navigationNode.getSiteNode());
        assertFalse(navigationNode.isEnabled());
*/
        String htmlOutput = elementToString(navigation);
        assertEquals("<ul><li class=\"selected\">" +
                "<a href=\"/projects\" title=\"projects\">projects</a></li>" +
                "</ul><hr /><ul><li><a href=\"/projects/report\" " +
                "title=\"report description\">report title</a></li></ul>",
                htmlOutput);
    }

    public void testProjectNew() {
        String originalPath = "/projects/new";
        req.setServletPath(originalPath);
        Dispatch dispatch = dispatcher.createDispatch(req);
        assertNotNull(dispatch);

        assertEquals(originalPath, dispatch.getOriginalPath());
        assertEquals(CRUD_ACTION, dispatch.getRewrittenPath());

        SiteNodeInstance[] siteNodeInstancePath =
                dispatch.getSiteNodeInstancePath();
        assertEquals(1, siteNodeInstancePath.length);

        CrudNodeInstance siteNodeInstance = (CrudNodeInstance) siteNodeInstancePath[0];
        SiteNode siteNode = model.getRootNode().getChildNodes().get(0);
        assertEquals(siteNode, siteNodeInstance.getSiteNode());
        assertEquals(CrudNode.MODE_NEW, siteNodeInstance.getMode());
        assertNull(siteNodeInstance.getPk());
        
        Navigation navigation =
                new Navigation(application, dispatch, Collections.EMPTY_LIST);
        /*
        List<NavigationNode> rootNodes = navigation.getRootNodes();

        // Navigation node per /projects
        assertEquals(1, rootNodes.size());
        NavigationNode navigationNode = rootNodes.get(0);
        assertEquals("/projects", navigationNode.getUrl());
        assertEquals(siteNode, navigationNode.getSiteNode());
        assertTrue(navigationNode.isEnabled());

        // Navigation node per /projects/tickets
        assertEquals(1, navigationNode.getChildNodes().size());
        navigationNode = navigationNode.getChildNodes().get(0);
        assertEquals("/projects/tickets", navigationNode.getUrl());
        siteNode = siteNode.getChildNodes().get(0);
        assertEquals(siteNode, navigationNode.getSiteNode());
        assertFalse(navigationNode.isEnabled());
        */
    }

    public void testProjectReport() {
        String originalPath = "/projects/report";
        req.setServletPath(originalPath);
        Dispatch dispatch = dispatcher.createDispatch(req);
        assertNotNull(dispatch);

        assertEquals(originalPath, dispatch.getOriginalPath());
        assertEquals(CHART_ACTION, dispatch.getRewrittenPath());

        SiteNodeInstance[] siteNodeInstancePath =
                dispatch.getSiteNodeInstancePath();
        assertEquals(2, siteNodeInstancePath.length);

        CrudNodeInstance siteNodeInstance = (CrudNodeInstance) siteNodeInstancePath[0];
        SiteNode siteNode = model.getRootNode().getChildNodes().get(0);
        assertEquals(siteNode, siteNodeInstance.getSiteNode());
        assertEquals(CrudNode.MODE_SEARCH, siteNodeInstance.getMode());
        assertNull(siteNodeInstance.getPk());

        SiteNodeInstance reportNodeInstance = siteNodeInstancePath[1];
        SiteNode reportNode = siteNode.getChildNodes().get(0);
        assertEquals("report", reportNode.getId());
        assertEquals(reportNode, reportNodeInstance.getSiteNode());
        assertNull(reportNodeInstance.getMode());
        assertNull(siteNodeInstance.getPk());

        /*
        List<NavigationNode> rootNodes = navigation.getRootNodes();

        // Navigation node per /projects
        assertEquals(1, rootNodes.size());
        NavigationNode navigationNode = rootNodes.get(0);
        assertEquals("/projects", navigationNode.getUrl());
        assertEquals(siteNode, navigationNode.getSiteNode());
        assertTrue(navigationNode.isEnabled());

        // Navigation node per /projects/tickets
        assertEquals(1, navigationNode.getChildNodes().size());
        navigationNode = navigationNode.getChildNodes().get(0);
        assertEquals("/projects/tickets", navigationNode.getUrl());
        siteNode = siteNode.getChildNodes().get(0);
        assertEquals(siteNode, navigationNode.getSiteNode());
        assertFalse(navigationNode.isEnabled());
        */
    }

    public void testProjectDetail() {
        String originalPath = "/projects/10";
        req.setServletPath(originalPath);
        Dispatch dispatch = dispatcher.createDispatch(req);
        assertNotNull(dispatch);

        assertEquals(originalPath, dispatch.getOriginalPath());
        assertEquals(CRUD_ACTION, dispatch.getRewrittenPath());

        SiteNodeInstance[] siteNodeInstancePath =
                dispatch.getSiteNodeInstancePath();
        assertEquals(1, siteNodeInstancePath.length);

        // nodo /project
        List<SiteNodeInstance> tree = dispatch.getNavigationNodeInstances();
        assertNotNull(tree);
        assertEquals(1, tree.size());

        CrudNodeInstance siteNodeInstance = (CrudNodeInstance) tree.get(0);
        assertEquals(siteNodeInstancePath[0], siteNodeInstance);
        RootNode rootNode = model.getRootNode();
        CrudNode siteNode = (CrudNode) rootNode.getChildNodes().get(0);
        assertEquals(siteNode, siteNodeInstance.getSiteNode());
        assertEquals(CrudNode.MODE_DETAIL, siteNodeInstance.getMode());
        assertEquals("10", siteNodeInstance.getPk());

        // nodo issues
        tree = siteNodeInstance.getChildNodeInstances();
        assertNotNull(tree);
        assertEquals(1, tree.size());

        siteNodeInstance = (CrudNodeInstance) tree.get(0);
        siteNode = (CrudNode) siteNode.getDetailChildNodes().get(0);
        assertEquals(siteNode, siteNodeInstance.getSiteNode());
        assertEquals(CrudNode.MODE_SEARCH, siteNodeInstance.getMode());
        assertNull(siteNodeInstance.getPk());

        Navigation navigation =
                new Navigation(application, dispatch, Collections.EMPTY_LIST);
        /*
        List<NavigationNode> rootNodes = navigation.getRootNodes();

        // Navigation node per /projects
        assertEquals(1, rootNodes.size());
        NavigationNode navigationNode = rootNodes.get(0);
        assertEquals("/projects", navigationNode.getUrl());
        assertEquals(siteNode, navigationNode.getSiteNode());
        assertTrue(navigationNode.isEnabled());
        */

        String htmlOutput = elementToString(navigation);
        assertEquals("<ul><li class=\"selected\">" +
                "<a href=\"/projects\" title=\"projects\">projects</a></li></ul>" +
                "<hr /><ul><li><a href=\"/projects/10/tickets\" title=\"tickets\">tickets</a></li></ul>",
                htmlOutput);

        /*
        // Navigation node per /projects/tickets
        assertEquals(1, navigationNode.getChildNodes().size());
        navigationNode = navigationNode.getChildNodes().get(0);
        assertEquals("/projects/10/tickets", navigationNode.getUrl());
        siteNode = siteNode.getChildNodes().get(0);
        assertEquals(siteNode, navigationNode.getSiteNode());
        assertTrue(navigationNode.isEnabled());
        */
    }


    public void testTicketSearch() {
        String originalPath = "/projects/10/tickets";
        req.setServletPath(originalPath);
        Dispatch dispatch = dispatcher.createDispatch(req);
        assertNotNull(dispatch);

        assertEquals(originalPath, dispatch.getOriginalPath());
        assertEquals(CRUD_ACTION, dispatch.getRewrittenPath());

        SiteNodeInstance[] siteNodeInstancePath =
                dispatch.getSiteNodeInstancePath();
        assertEquals(2, siteNodeInstancePath.length);

        Navigation navigation =
                new Navigation(application, dispatch, Collections.EMPTY_LIST);
        /*
        List<NavigationNode> rootNodes = navigation.getRootNodes();
        */

        // SiteNode e NavigationNode per /projects
        CrudNodeInstance siteNodeInstance = (CrudNodeInstance) siteNodeInstancePath[0];
        SiteNode siteNode = model.getRootNode().getChildNodes().get(0);
        assertEquals(siteNode, siteNodeInstance.getSiteNode());
        assertEquals(CrudNode.MODE_DETAIL, siteNodeInstance.getMode());
        assertEquals("10", siteNodeInstance.getPk());

        /*
        assertEquals(1, rootNodes.size());
        NavigationNode navigationNode = rootNodes.get(0);
        assertEquals("/projects", navigationNode.getUrl());
        assertEquals(siteNode, navigationNode.getSiteNode());
        assertTrue(navigationNode.isEnabled());
        */

        siteNodeInstance = (CrudNodeInstance) siteNodeInstancePath[1];
        siteNode = ((CrudNode)siteNode).getDetailChildNodes().get(0);
        assertEquals(siteNode, siteNodeInstance.getSiteNode());
        assertEquals(CrudNode.MODE_SEARCH, siteNodeInstance.getMode());
        assertNull(siteNodeInstance.getPk());

        /*
        // SiteNode e NavigationNode per /projects/tickets
        assertEquals(1, navigationNode.getChildNodes().size());
        navigationNode = navigationNode.getChildNodes().get(0);
        assertEquals("/projects/10/tickets", navigationNode.getUrl());
        assertEquals(siteNode, navigationNode.getSiteNode());
        assertTrue(navigationNode.isEnabled());
*/

        String htmlOutput = elementToString(navigation);
        assertEquals("<ul><li class=\"path\">" +
                "<a href=\"/projects\" title=\"projects\">projects</a></li></ul>" +
                "<hr /><ul><li class=\"selected\"><a href=\"/projects/10/tickets\" title=\"tickets\">tickets</a></li></ul>",
                htmlOutput);
    }


    public void testTicketDetail() {
        String originalPath = "/projects/10/tickets/20";
        req.setServletPath(originalPath);
        Dispatch dispatch = dispatcher.createDispatch(req);
        assertNotNull(dispatch);

        assertEquals(originalPath, dispatch.getOriginalPath());
        assertEquals(CRUD_ACTION, dispatch.getRewrittenPath());

        SiteNodeInstance[] siteNodeInstancePath =
                dispatch.getSiteNodeInstancePath();
        assertEquals(2, siteNodeInstancePath.length);

        CrudNodeInstance siteNodeInstance = (CrudNodeInstance) siteNodeInstancePath[0];
        CrudNode expected = (CrudNode) model.getRootNode().getChildNodes().get(0);
        assertEquals(expected, siteNodeInstance.getSiteNode());
        assertEquals(CrudNode.MODE_DETAIL, siteNodeInstance.getMode());
        assertEquals("10", siteNodeInstance.getPk());

        siteNodeInstance = (CrudNodeInstance) siteNodeInstancePath[1];
        expected = (CrudNode) expected.getDetailChildNodes().get(0);
        assertEquals(expected, siteNodeInstance.getSiteNode());
        assertEquals(CrudNode.MODE_DETAIL, siteNodeInstance.getMode());
        assertEquals("20", siteNodeInstance.getPk());
    }

    public void testIllegal1() {
        String originalPath = "/projects/tickets/bla";
        req.setServletPath(originalPath);
        Dispatch dispatch = dispatcher.createDispatch(req);
        assertNull(dispatch);
    }

    public void testIllegal2() {
        String originalPath = "/projects/new/tickets/bla";
        req.setServletPath(originalPath);
        Dispatch dispatch = dispatcher.createDispatch(req);
        assertNull(dispatch);
    }
}
