<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.dispatcher.Dispatch" %>
<%@ page import="com.manydesigns.portofino.dispatcher.Dispatcher" %>
<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ page import="com.manydesigns.portofino.navigation.Navigation" %>
<%@ page import="com.manydesigns.portofino.navigation.NavigationItem" %>
<%@ page import="org.apache.shiro.SecurityUtils" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="com.manydesigns.portofino.dispatcher.DispatcherUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="app" scope="request" type="com.manydesigns.portofino.application.Application" />
<jsp:useBean id="actionBean" scope="request" type="net.sourceforge.stripes.action.ActionBean" />
<%
    Dispatch dispatch = DispatcherUtil.getDispatch(request, actionBean);
    boolean admin = SecurityLogic.isAdministrator(request);
    int maxLevels = 1;
    String param = request.getParameter("tabsMaxLevels");
    if(param != null) {
        maxLevels = Integer.parseInt(param);
    }
    boolean includeAdminButtons = false;
    param = request.getParameter("tabsIncludeAdminButtons");
    if(param != null) {
        includeAdminButtons = Boolean.valueOf(param);
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
        NavigationItem nextNavigationItem = null;
        String cssClass = "tab-row level" + depth;
        depth++;
        if(first) {
            cssClass += " first";
        }
        for (NavigationItem current : navigationItems) {
            if (current.isInPath()) {
                nextNavigationItem = current;
            }
        }
        boolean last =
                nextNavigationItem == null ||
                nextNavigationItem.getChildNavigationItems().isEmpty() ||
                depth >= maxLevels;
        if(last) {
            cssClass += " last";
        }
        out.print("<div class=\"" + cssClass + "\">");
        if (first) {
            first = false;
            if(includeAdminButtons) { %>
                <stripes:form action="/actions/admin/page" method="post" id="pageAdminForm">
                    <input type="hidden" name="originalPath" value="${actionBean.dispatch.originalPath}" />
                    <!-- Admin buttons -->
                    <div class="contentBarButtons">
                        <portofino:page-layout-button />
                        <portofino:page-children-button />
                        <portofino:page-permissions-button />
                        <portofino:page-copy-button />
                        <portofino:page-new-button />
                        <portofino:page-delete-button />
                        <portofino:page-move-button />
                    </div>
                </stripes:form><%
            }
        }
        for (NavigationItem current : navigationItems) {
            XhtmlBuffer xb = new XhtmlBuffer(out);
            xb.openElement("div");
            if (current.isInPath()) {
                if (current.isSelected() || depth >= maxLevels) {
                    xb.addAttribute("class", "tab selected");
                } else {
                    xb.addAttribute("class", "tab path");
                }
            } else {
                xb.addAttribute("class", "tab");
            }
            xb.writeAnchor(current.getPath(), current.getPage().getTitle());
            xb.closeElement("div");
        }
        out.print("</div>");
        if (last) {
            navigationItems = Collections.EMPTY_LIST;
        } else {
            navigationItems = nextNavigationItem.getChildNavigationItems();
        }
    }
%>