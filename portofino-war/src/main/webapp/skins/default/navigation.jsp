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
    int startingLevel = 2;
    int maxLevel = 10000;
    Object attribute = request.getAttribute("navigation.startingLevel");
    if(attribute instanceof Number) {
        startingLevel = ((Number) attribute).intValue();
    }
    attribute = request.getAttribute("navigation.maxLevels");
    if(attribute instanceof Number) {
        maxLevel = ((Number) attribute).intValue();
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
    int level = 0;
    while (!navigationItems.isEmpty()) {
        NavigationItem nextNavigationItem = null;
        if(level >= startingLevel) {
            if (first) {
                first = false;
            } else {
                %><hr><%
            }
            %><ul><%
        }
        for (NavigationItem current : navigationItems) {
            XhtmlBuffer xb = new XhtmlBuffer(out);
            if(level >= startingLevel) {
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
                xb.closeElement("li");
            } else if (current.isInPath()) {
                nextNavigationItem = current;
            }
        }
        if(level >= startingLevel) {
            %></ul><%
        }
        if (nextNavigationItem != null && level < maxLevel) {
            navigationItems = nextNavigationItem.getChildNavigationItems();
        } else {
            navigationItems = Collections.EMPTY_LIST;
        }
        level++;
    }
%>
