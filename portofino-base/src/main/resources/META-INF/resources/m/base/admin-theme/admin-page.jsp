<%@ page import="com.manydesigns.portofino.PortofinoProperties"
%><%@ page import="com.manydesigns.portofino.menu.*"
%><%@ page import="com.manydesigns.portofino.modules.BaseModule"
%><%@ page import="net.sourceforge.stripes.controller.ActionResolver"
%><%@ page import="org.apache.commons.lang.StringUtils"
%><%@ page import="com.manydesigns.portofino.modules.ModuleRegistry"
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><stripes:layout-definition><%--
--%><!doctype html>
    <html lang="en">
    <head>
        <jsp:include page="/theme/head.jsp" />
        <title>
            <stripes:layout-component name="pageTitle" />
        </title>
    </head>
    <body>
    <jsp:useBean id="portofinoConfiguration" scope="application" type="org.apache.commons.configuration.Configuration"/>
    <div class="navbar navbar-inverse navbar-fixed-top">
        <div class="navbar-inner">
            <div class="container">
                <h4 id="app-title" class="pull-left">
                    <stripes:link href="/">
                        <c:out value="<%= portofinoConfiguration.getString(PortofinoProperties.APP_NAME) %>"/>
                    </stripes:link>
                </h4>
            </div>
        </div>
    </div>
    <div class="container">
        <div class="row">
            <div class="span2">
                <div id="navigation">
                    <ul class="nav nav-list portofino-sidenav">
                        <%
                            //Admin menu
                            MenuBuilder adminMenuBuilder =
                                    (MenuBuilder) application.getAttribute(BaseModule.ADMIN_MENU);
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
            <div id="content" class="span10">
                <div class="row-fluid">
                    <mde:sessionMessages/>
                    <stripes:layout-component name="portletHeader">
                        <h3 style="border-bottom: 1px solid #E5E5E5">
                            <stripes:layout-component name="portletTitle" />
                        </h3>
                    </stripes:layout-component>
                    <stripes:layout-component name="portletBody" />
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