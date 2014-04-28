<%@ page import="com.manydesigns.portofino.PortofinoProperties"
%><%@ page import="com.manydesigns.portofino.menu.*"
%><%@ page import="com.manydesigns.portofino.modules.AdminModule"
%><%@ page import="com.manydesigns.portofino.modules.ModuleRegistry"
%><%@ page import="net.sourceforge.stripes.controller.ActionResolver"
%><%@ page import="org.apache.commons.lang.StringUtils"
%><%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><stripes:layout-definition><%--
--%><!doctype html>
    <html lang="<%= request.getLocale() %>">
    <jsp:include page="/theme/head.jsp">
        <jsp:param name="pageTitle" value="${pageTitle}" />
    </jsp:include>
    <body>
    <jsp:useBean id="portofinoConfiguration" scope="application" type="org.apache.commons.configuration.Configuration"/>
    <header class="navbar navbar-inverse navbar-static-top">
        <div class="container">
            <h4 class="pull-left">
                <stripes:link href="/">
                    <c:out value="<%= portofinoConfiguration.getString(PortofinoProperties.APP_NAME) %>"/>
                </stripes:link>
            </h4>
        </div>
    </header>
    <div class="container">
        <div class="row">
            <div class="col-md-2">
                <div class="navigation">
                    <ul class="nav nav-list portofino-sidenav">
                        <%
                            //Admin menu
                            MenuBuilder adminMenuBuilder =
                                    (MenuBuilder) application.getAttribute(AdminModule.ADMIN_MENU);
                            Menu adminMenu = adminMenuBuilder.build();
                            for(MenuItem item : adminMenu.items) {
                                if(item instanceof MenuGroup) {
                                    %><li class="nav-header"><c:out value="<%= item.label %>"/></li><%
                                    for(MenuLink link : ((MenuGroup) item).menuLinks) {
                                        String adminLinkClass = getLinkClass(link, request);
                                        %>
                                        <li class="<%= adminLinkClass %>">
                                            <stripes:link href='<%= StringUtils.defaultString(link.link, "#") %>'>
                                                <c:out value="<%= link.label %>"/>
                                            </stripes:link>
                                        </li>
                                        <%
                                    }
                                } else {
                                    MenuLink link = (MenuLink) item;
                                    String adminLinkClass = getLinkClass(link, request);
                                    %>
                                    <li class="<%= adminLinkClass %>">
                                        <stripes:link href='<%= StringUtils.defaultString(link.link, "#") %>'>
                                            <c:out value="<%= link.label %>"/>
                                        </stripes:link>
                                    </li>
                                    <%
                                }
                            }
                        %>
                    </ul>
                </div>
            </div>
            <div class="content col-md-10">
                <div class="row">
                    <mde:sessionMessages/>
                    <stripes:layout-component name="pageHeader">
                        <h3 style="border-bottom: 1px solid #E5E5E5">
                            <stripes:layout-component name="pageTitle" />
                        </h3>
                    </stripes:layout-component>
                    <stripes:layout-component name="pageBody" />
                </div>
            </div>
        </div>
    </div>
    <footer>
        <div class="container">
            Powered by <a href="http://www.manydesigns.com/">Portofino</a>
            <c:out value="<%= ModuleRegistry.getPortofinoVersion() %>"/>
        </div>
    </footer>
    </body>
    </html>
</stripes:layout-definition>
<%!
    protected String getLinkClass(MenuLink link, HttpServletRequest request) {
        String adminLinkClass = "";
        String actionPath = (String) request.getAttribute(ActionResolver.RESOLVED_ACTION);
        if(!"/".equals(link.link) && actionPath.startsWith(link.link)) {
            adminLinkClass = "active";
        }
        return adminLinkClass;
    }
%>