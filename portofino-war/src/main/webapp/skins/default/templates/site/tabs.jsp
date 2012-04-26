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
    int maxLevels = 1;
    String param = request.getParameter("tabs.maxLevels");
    if(param != null) {
        maxLevels = Integer.parseInt(param);
    }

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
    int depth = 0;
    while (!navigationItems.isEmpty()) {
        depth++;
        NavigationItem nextNavigationItem = null;
        if (first) {
            first = false;
            %><div class="tab-row first"><%
        } else {
            %><div class="tab-row"><%
        }
        for (NavigationItem current : navigationItems) {
            XhtmlBuffer xb = new XhtmlBuffer(out);
            xb.openElement("span");
            if (current.isInPath()) {
                if (current.isSelected() || depth >= maxLevels) {
                    xb.addAttribute("class", "tab selected");
                } else {
                    xb.addAttribute("class", "tab path");
                }
                nextNavigationItem = current;
            } else {
                xb.addAttribute("class", "tab");
            }
            xb.writeAnchor(current.getPath(), current.getPage().getTitle());
            xb.closeElement("span");
        }
        %></div><%
        if (nextNavigationItem != null && depth < maxLevels) {
            navigationItems = nextNavigationItem.getChildNavigationItems();
        } else {
            navigationItems = Collections.EMPTY_LIST;
        }
    }
%>
