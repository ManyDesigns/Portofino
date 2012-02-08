<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.manydesigns.portofino.navigation.Navigation" %>
<%@ page import="com.manydesigns.portofino.navigation.NavigationItem" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="org.apache.shiro.SecurityUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="dispatch" scope="request" type="com.manydesigns.portofino.dispatcher.Dispatch" />
<jsp:useBean id="app" scope="request" type="com.manydesigns.portofino.application.Application" />
<%
    boolean admin = SecurityLogic.isAdministrator(request);

    Navigation navigation = new Navigation(app, dispatch, SecurityUtils.getSubject(), admin);
    List<NavigationItem> navigationItems;
    NavigationItem rootNavigationItem = navigation.getRootNavigationItem();
    if (rootNavigationItem.isGhost() && !rootNavigationItem.isSelected()) {
        navigationItems = rootNavigationItem.getChildNavigationItems();
    } else {
        navigationItems = new ArrayList<NavigationItem>(1);
        navigationItems.add(rootNavigationItem);
    }
    boolean first = true;
    while (!navigationItems.isEmpty()) {
        NavigationItem nextNavigationItem = null;
        if (first) {
            first = false;
        } else {
            %><hr><%
        }
        %><ul><%
        for (NavigationItem current : navigationItems) {
            XhtmlBuffer xb = new XhtmlBuffer(out);
            xb.openElement("li");
            if (current.isInPath()) {
                if (current.isSelected()) {
                    xb.addAttribute("class", "selected");
                } else {
                    xb.addAttribute("class", "path");
                }
                nextNavigationItem = current;
            }
            xb.writeAnchor(current.getPath(), current.getPage().getTitle());
        }
        %></ul><%
        if (nextNavigationItem != null) {
            navigationItems = nextNavigationItem.getChildNavigationItems();
        } else {
            navigationItems = Collections.EMPTY_LIST;
        }
    }
%>
