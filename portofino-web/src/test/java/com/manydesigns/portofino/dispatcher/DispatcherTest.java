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

import com.manydesigns.elements.servlet.MutableHttpServletRequest;
import com.manydesigns.portofino.TestApplication;
import com.manydesigns.portofino.pageactions.crud.CrudAction;
import com.manydesigns.portofino.pageactions.custom.CustomAction;
import junit.framework.TestCase;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla - alessio.stalla@manydesigns.com
*/
public class DispatcherTest extends TestCase {

    Dispatcher dispatcher;
    TestApplication application;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        DispatcherLogic.initConfigurationCache(10, Integer.MAX_VALUE);
        DispatcherLogic.initPageCache(10, Integer.MAX_VALUE);
        application = new TestApplication("test");
        dispatcher = new Dispatcher(application);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if(application != null) {
            application.shutdown();
        }
    }

    public void testJSessionId() throws Exception {
        application.addPage("/", "bar", CustomAction.class);
        Dispatch dispatch = dispatcher.getDispatch("/foo", "/bar;jsessionid=qwertyuiop1234567890");
        assertNotNull(dispatch);

        application.addPage("/bar", "baz", CustomAction.class);
        dispatch = dispatcher.getDispatch("/foo", "/bar/baz;jsessionid=qwertyuiop1234567890");
        assertNotNull(dispatch);
        dispatch = dispatcher.getDispatch("/foo", "/bar;foo=1&quux=2/baz;jsessionid=qwertyuiop1234567890");
        assertNotNull(dispatch);
    }

    public void testProjectSearch() throws Exception {
        application.addPage("/", "projects", CrudAction.class);
        String originalPath = "/projects";
        MutableHttpServletRequest req = new MutableHttpServletRequest();
        req.setRequestURI(originalPath);
        Dispatch dispatch = new Dispatcher(application).getDispatch("", originalPath);
        assertNotNull(dispatch);

        assertEquals(originalPath, dispatch.getOriginalPath());
        assertEquals(originalPath, dispatch.getAbsoluteOriginalPath());
        assertEquals(originalPath, dispatch.getLastPageInstance().getPath());
        assertTrue(CrudAction.class.isAssignableFrom(dispatch.getActionBeanClass()));

        PageInstance[] pageInstancePath =
                dispatch.getPageInstancePath();
        assertEquals(2, pageInstancePath.length);

        dispatch = new Dispatcher(application).getDispatch("/foo", originalPath);
        assertNotNull(dispatch);

        assertEquals(originalPath, dispatch.getOriginalPath());
        assertEquals("/foo" + originalPath, dispatch.getAbsoluteOriginalPath());
        assertEquals(originalPath, dispatch.getLastPageInstance().getPath());
        assertTrue(CrudAction.class.isAssignableFrom(dispatch.getActionBeanClass()));

        pageInstancePath = dispatch.getPageInstancePath();
        assertEquals(2, pageInstancePath.length);
    }

    public void testArguments() throws Exception {
        application.addPage("/", "projects", CrudAction.class);
        String originalPath = "/projects/A%2FB";
        MutableHttpServletRequest req = new MutableHttpServletRequest();
        req.setRequestURI(originalPath);
        Dispatch dispatch = new Dispatcher(application).getDispatch("", originalPath);
        assertNotNull(dispatch);
        assertEquals(1, dispatch.getLastPageInstance().getParameters().size());
        assertEquals("A%2FB", dispatch.getLastPageInstance().getParameters().get(0));
    }

}
//public class DispatcherTest extends AbstractPortofinoTest {
//
//    Dispatcher dispatcher;
//
//    @Override
//    public void setUp() throws Exception {
//        super.setUp();
//        dispatcher = new Dispatcher(application);
//    }
//
//    @Override
//    public void tearDown() throws Exception {
//        super.tearDown();
//
//    }
//
//    @Override
//    public String getTestAppId() {
//        return "test-tt";
//    }
//
//    public void testProjectSearch() {
//        System.out.println("*** testProjectSearch");
//        String originalPath = "/projects";
//        req.setRequestURI(originalPath);
//        Dispatch dispatch = dispatcher.createDispatch(req);
//        assertNotNull(dispatch);
//
//        assertEquals(originalPath, dispatch.getOriginalPath());
//        assertEquals(CrudAction.class, dispatch.getActionBeanClass());
//
//        PageInstance[] pageInstancePath =
//                dispatch.getPageInstancePath();
//        assertEquals(2, pageInstancePath.length);
//
//        CrudPageInstance pageInstance = (CrudPageInstance) pageInstancePath[1];
//        Page page = model.getRootPage().getChildPages().get(1);
//        assertEquals(page, pageInstance.getPage());
//        assertEquals(CrudPage.MODE_SEARCH, pageInstance.getMode());
//        assertNull(pageInstance.getPk());
//
//        Navigation navigation =
//                new Navigation(application, dispatch, Collections.<String>emptyList(), true);
//        /*
//        List<NavigationNode> rootPages = navigation.getRootNodes();
//
//        // Navigation node per /projects
//        assertEquals(1, rootPages.size());
//        NavigationNode navigationNode = rootPages.get(0);
//        assertEquals("/projects", navigationNode.getUrl());
//        assertEquals(page, navigationNode.getPage());
//        assertTrue(navigationNode.isEnabled());
//
//        // Navigation node per /projects/tickets
//        assertEquals(1, navigationNode.getChildNodes().size());
//        navigationNode = navigationNode.getChildNodes().get(0);
//        assertEquals("/projects/tickets", navigationNode.getUrl());
//        page = page.getChildNodes().get(0);
//        assertEquals(page, navigationNode.getPage());
//        assertFalse(navigationNode.isEnabled());
//*/
//        String htmlOutput = Util.elementToString(navigation);
//        assertEquals("<ul><li><a href=\"/welcome\" title=\"Welcome to Ticket Tracker\">Welcome</a></li><li class=\"selected\"><a href=\"/projects\" title=\"Projects\">Projects</a></li><li><a href=\"/people\" title=\"People\">People</a></li></ul><hr /><ul><li><a href=\"/projects/report\" title=\"Issues by status\">report</a></li></ul>",
//                htmlOutput);
//    }
//
//    public void testProjectNew() {
//        try {
//            System.out.println("*** testProjectNew");
//            String originalPath = "/projects/new";
//            req.setRequestURI(originalPath);
//            Dispatch dispatch = dispatcher.createDispatch(req);
//            assertNotNull(dispatch);
//
//            assertEquals(originalPath, dispatch.getOriginalPath());
//            assertEquals(CrudAction.class, dispatch.getActionBeanClass());
//
//            PageInstance[] pageInstancePath =
//                    dispatch.getPageInstancePath();
//            assertEquals(2, pageInstancePath.length);
//
//            CrudPageInstance pageInstance = (CrudPageInstance) pageInstancePath[1];
//            Page page = model.getRootPage().getChildPages().get(1);
//            assertEquals(page, pageInstance.getPage());
//            assertEquals(CrudPage.MODE_NEW, pageInstance.getMode());
//            assertNull(pageInstance.getPk());
//
//            Navigation navigation =
//                    new Navigation(application, dispatch, Collections.<String>emptyList(), true);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//        /*
//        List<NavigationNode> rootPages = navigation.getRootNodes();
//
//        // Navigation node per /projects
//        assertEquals(1, rootPages.size());
//        NavigationNode navigationNode = rootPages.get(0);
//        assertEquals("/projects", navigationNode.getUrl());
//        assertEquals(page, navigationNode.getPage());
//        assertTrue(navigationNode.isEnabled());
//
//        // Navigation node per /projects/tickets
//        assertEquals(1, navigationNode.getChildNodes().size());
//        navigationNode = navigationNode.getChildNodes().get(0);
//        assertEquals("/projects/tickets", navigationNode.getUrl());
//        page = page.getChildNodes().get(0);
//        assertEquals(page, navigationNode.getPage());
//        assertFalse(navigationNode.isEnabled());
//        */
//    }
//
//    public void testProjectReport() {
//        System.out.println("*** testProjectReport");
//        String originalPath = "/projects/report";
//        req.setRequestURI(originalPath);
//        Dispatch dispatch = dispatcher.createDispatch(req);
//        assertNotNull(dispatch);
//
//        assertEquals(originalPath, dispatch.getOriginalPath());
//        assertEquals(ChartAction.class, dispatch.getActionBeanClass());
//
//        PageInstance[] pageInstancePath =
//                dispatch.getPageInstancePath();
//        assertEquals(3, pageInstancePath.length);
//
//        CrudPageInstance pageInstance = (CrudPageInstance) pageInstancePath[1];
//        Page page = model.getRootPage().getChildPages().get(1);
//        assertEquals(page, pageInstance.getPage());
//        assertEquals(CrudPage.MODE_SEARCH, pageInstance.getMode());
//        assertNull(pageInstance.getPk());
//
//        PageInstance reportNodeInstance = pageInstancePath[2];
//        Page reportNode = page.getChildPages().get(0);
//        assertEquals("report", reportNode.getFragment());
//        assertEquals(reportNode, reportNodeInstance.getPage());
//        assertNull(reportNodeInstance.getMode());
//        assertNull(pageInstance.getPk());
//
//        /*
//        List<NavigationNode> rootPages = navigation.getRootNodes();
//
//        // Navigation node per /projects
//        assertEquals(1, rootPages.size());
//        NavigationNode navigationNode = rootPages.get(0);
//        assertEquals("/projects", navigationNode.getUrl());
//        assertEquals(page, navigationNode.getPage());
//        assertTrue(navigationNode.isEnabled());
//
//        // Navigation node per /projects/tickets
//        assertEquals(1, navigationNode.getChildNodes().size());
//        navigationNode = navigationNode.getChildNodes().get(0);
//        assertEquals("/projects/tickets", navigationNode.getUrl());
//        page = page.getChildNodes().get(0);
//        assertEquals(page, navigationNode.getPage());
//        assertFalse(navigationNode.isEnabled());
//        */
//    }
//
//    public void testProjectDetail() {
//        String originalPath = "/projects/10";
//        req.setRequestURI(originalPath);
//        Dispatch dispatch = dispatcher.createDispatch(req);
//        assertNotNull(dispatch);
//
//        assertEquals(originalPath, dispatch.getOriginalPath());
//        assertEquals(CrudAction.class, dispatch.getActionBeanClass());
//
//        PageInstance[] pageInstancePath =
//                dispatch.getPageInstancePath();
//        assertEquals(2, pageInstancePath.length);
//
//        // nodo /project
//        PageInstance rootPageInstance = dispatch.getRootPageInstance();
//        assertEquals(pageInstancePath[0], rootPageInstance);
//
//        List<PageInstance> tree = rootPageInstance.getChildPageInstances();
//        assertNotNull(tree);
//        assertEquals(3, tree.size());
//
//        CrudPageInstance pageInstance = (CrudPageInstance) tree.get(1);
//        assertEquals(pageInstancePath[1], pageInstance);
//        RootPage rootPage = model.getRootPage();
//        CrudPage page = (CrudPage) rootPage.getChildPages().get(1);
//        assertEquals(page, pageInstance.getPage());
//        assertEquals(CrudPage.MODE_DETAIL, pageInstance.getMode());
//        assertEquals("10", pageInstance.getPk());
//
//        // nodo issues
//        tree = pageInstance.getChildPageInstances();
//        assertNotNull(tree);
//        assertEquals(9, tree.size());
//
//        pageInstance = (CrudPageInstance) tree.get(0);
//        page = (CrudPage) page.getDetailChildPages().get(0);
//        assertEquals(page, pageInstance.getPage());
//        assertEquals(CrudPage.MODE_SEARCH, pageInstance.getMode());
//        assertNull(pageInstance.getPk());
//
//        Navigation navigation =
//                new Navigation(application, dispatch, Collections.<String>emptyList(), true);
//        /*
//        List<NavigationNode> rootPages = navigation.getRootNodes();
//
//        // Navigation node per /projects
//        assertEquals(1, rootPages.size());
//        NavigationNode navigationNode = rootPages.get(0);
//        assertEquals("/projects", navigationNode.getUrl());
//        assertEquals(page, navigationNode.getPage());
//        assertTrue(navigationNode.isEnabled());
//        */
//
//        String htmlOutput = Util.elementToString(navigation);
//        assertEquals("<ul><li><a href=\"/welcome\" title=\"Welcome to Ticket Tracker\">Welcome</a></li><li class=\"selected\">" +
//                "<a href=\"/projects\" title=\"Projects\">Projects</a></li>" +
//                "<li><a href=\"/people\" title=\"People\">People</a></li></ul><hr /><ul><li><a href=\"/projects/10/issues\" title=\"Issues\">Issues</a></li><li><a href=\"/projects/10/versions\" title=\"Projects versions\">Version</a></li></ul>",
//                htmlOutput);
//
//        /*
//        // Navigation node per /projects/tickets
//        assertEquals(1, navigationNode.getChildNodes().size());
//        navigationNode = navigationNode.getChildNodes().get(0);
//        assertEquals("/projects/10/tickets", navigationNode.getUrl());
//        page = page.getChildNodes().get(0);
//        assertEquals(page, navigationNode.getPage());
//        assertTrue(navigationNode.isEnabled());
//        */
//    }
//
//
//    public void testTicketSearch() {
//        System.out.println("*** testTicketSearch");
//        String originalPath = "/projects/10/issues";
//        req.setRequestURI(originalPath);
//        Dispatch dispatch = dispatcher.createDispatch(req);
//        assertNotNull(dispatch);
//
//        assertEquals(originalPath, dispatch.getOriginalPath());
//        assertEquals(CrudAction.class, dispatch.getActionBeanClass());
//
//        PageInstance[] pageInstancePath =
//                dispatch.getPageInstancePath();
//        assertEquals(3, pageInstancePath.length);
//
//        Navigation navigation =
//                new Navigation(application, dispatch, Collections.<String>emptyList(), true);
//        /*
//        List<NavigationNode> rootPages = navigation.getRootNodes();
//        */
//
//        // Page e NavigationNode per /projects
//        CrudPageInstance pageInstance = (CrudPageInstance) pageInstancePath[1];
//        Page page = model.getRootPage().getChildPages().get(1);
//        assertEquals(page, pageInstance.getPage());
//        assertEquals(CrudPage.MODE_DETAIL, pageInstance.getMode());
//        assertEquals("10", pageInstance.getPk());
//
//        /*
//        assertEquals(1, rootPages.size());
//        NavigationNode navigationNode = rootPages.get(0);
//        assertEquals("/projects", navigationNode.getUrl());
//        assertEquals(page, navigationNode.getPage());
//        assertTrue(navigationNode.isEnabled());
//        */
//
//        pageInstance = (CrudPageInstance) pageInstancePath[2];
//        page = ((CrudPage) page).getDetailChildPages().get(0);
//        assertEquals(page, pageInstance.getPage());
//        assertEquals(CrudPage.MODE_SEARCH, pageInstance.getMode());
//        assertNull(pageInstance.getPk());
//
//        /*
//        // Page e NavigationNode per /projects/tickets
//        assertEquals(1, navigationNode.getChildNodes().size());
//        navigationNode = navigationNode.getChildNodes().get(0);
//        assertEquals("/projects/10/tickets", navigationNode.getUrl());
//        assertEquals(page, navigationNode.getPage());
//        assertTrue(navigationNode.isEnabled());
//*/
//
//        String htmlOutput = Util.elementToString(navigation);
//        assertEquals("<ul><li><a href=\"/welcome\" title=\"Welcome to Ticket Tracker\">Welcome</a></li><li class=\"path\"><a href=\"/projects\" title=\"Projects\">Projects</a></li><li><a href=\"/people\" title=\"People\">People</a></li></ul><hr /><ul><li class=\"selected\"><a href=\"/projects/10/issues\" title=\"Issues\">Issues</a></li><li><a href=\"/projects/10/versions\" title=\"Projects versions\">Version</a></li></ul>",
//                htmlOutput);
//    }
//
//
//    public void testTicketDetail() {
//        System.out.println("*** testTicketDetail");
//        String originalPath = "/projects/10/issues/20";
//        req.setRequestURI(originalPath);
//        Dispatch dispatch = dispatcher.createDispatch(req);
//        assertNotNull(dispatch);
//
//        assertEquals(originalPath, dispatch.getOriginalPath());
//        assertEquals(CrudAction.class, dispatch.getActionBeanClass());
//
//        PageInstance[] pageInstancePath =
//                dispatch.getPageInstancePath();
//        assertEquals(3, pageInstancePath.length);
//
//        CrudPageInstance pageInstance = (CrudPageInstance) pageInstancePath[1];
//        CrudPage expected = (CrudPage) model.getRootPage().getChildPages().get(1);
//        assertEquals(expected, pageInstance.getPage());
//        assertEquals(CrudPage.MODE_DETAIL, pageInstance.getMode());
//        assertEquals("10", pageInstance.getPk());
//
//        pageInstance = (CrudPageInstance) pageInstancePath[2];
//        expected = (CrudPage) expected.getDetailChildPages().get(0);
//        assertEquals(expected, pageInstance.getPage());
//        assertEquals(CrudPage.MODE_DETAIL, pageInstance.getMode());
//        assertEquals("20", pageInstance.getPk());
//    }
//
//    public void testIllegal1() {
//        System.out.println("*** testIllegal1");
//        String originalPath = "/projects/issues/bla";
//        req.setRequestURI(originalPath);
//        Dispatch dispatch = dispatcher.createDispatch(req);
//        assertNull(dispatch);
//    }
//
//    public void testIllegal2() {
//        System.out.println("*** testIllegal2");
//        String originalPath = "/projects/new/issues/bla";
//        req.setRequestURI(originalPath);
//        Dispatch dispatch = dispatcher.createDispatch(req);
//        assertNull(dispatch);
//    }
//
//    public void testMultipleAndTrailingSlashes() {
//        String originalPath = "/projects///10//";
//        req.setRequestURI(originalPath);
//        Dispatch dispatch = dispatcher.createDispatch(req);
//        assertNotNull(dispatch);
//
//        assertEquals(originalPath, dispatch.getOriginalPath());
//        assertEquals(CrudAction.class, dispatch.getActionBeanClass());
//
//        PageInstance[] pageInstancePath =
//                dispatch.getPageInstancePath();
//        assertEquals(2, pageInstancePath.length);
//
//        // nodo /project
//        PageInstance rootPageInstance = dispatch.getRootPageInstance();
//        assertEquals(pageInstancePath[0], rootPageInstance);
//
//        List<PageInstance> tree = rootPageInstance.getChildPageInstances();
//        assertNotNull(tree);
//        assertEquals(3, tree.size());
//
//        CrudPageInstance pageInstance = (CrudPageInstance) tree.get(1);
//        assertEquals(pageInstancePath[1], pageInstance);
//        RootPage rootPage = model.getRootPage();
//        CrudPage page = (CrudPage) rootPage.getChildPages().get(1);
//        assertEquals(page, pageInstance.getPage());
//        assertEquals(CrudPage.MODE_DETAIL, pageInstance.getMode());
//        assertEquals("10", pageInstance.getPk());
//
//        // nodo issues
//        tree = pageInstance.getChildPageInstances();
//        assertNotNull(tree);
//        assertEquals(9, tree.size());
//
//        pageInstance = (CrudPageInstance) tree.get(0);
//        page = (CrudPage) page.getDetailChildPages().get(0);
//        assertEquals(page, pageInstance.getPage());
//        assertEquals(CrudPage.MODE_SEARCH, pageInstance.getMode());
//        assertNull(pageInstance.getPk());
//
//        Navigation navigation =
//                new Navigation(application, dispatch, Collections.<String>emptyList(), true);
//        /*
//        List<NavigationNode> rootPages = navigation.getRootNodes();
//
//        // Navigation node per /projects
//        assertEquals(1, rootPages.size());
//        NavigationNode navigationNode = rootPages.get(0);
//        assertEquals("/projects", navigationNode.getUrl());
//        assertEquals(page, navigationNode.getPage());
//        assertTrue(navigationNode.isEnabled());
//        */
//
//        String htmlOutput = Util.elementToString(navigation);
//        assertEquals("<ul><li><a href=\"/welcome\" title=\"Welcome to Ticket Tracker\">Welcome</a></li><li class=\"selected\">" +
//                "<a href=\"/projects\" title=\"Projects\">Projects</a></li>" +
//                "<li><a href=\"/people\" title=\"People\">People</a></li></ul><hr /><ul><li><a href=\"/projects/10/issues\" title=\"Issues\">Issues</a></li><li><a href=\"/projects/10/versions\" title=\"Projects versions\">Version</a></li></ul>",
//                htmlOutput);
//
//        /*
//        // Navigation node per /projects/tickets
//        assertEquals(1, navigationNode.getChildNodes().size());
//        navigationNode = navigationNode.getChildNodes().get(0);
//        assertEquals("/projects/10/tickets", navigationNode.getUrl());
//        page = page.getChildNodes().get(0);
//        assertEquals(page, navigationNode.getPage());
//        assertTrue(navigationNode.isEnabled());
//        */
//    }
//}
